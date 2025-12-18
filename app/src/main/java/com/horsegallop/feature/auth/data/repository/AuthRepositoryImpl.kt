package com.horsegallop.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.horsegallop.feature.auth.domain.model.User
import com.horsegallop.feature.auth.domain.model.UserRole
import com.horsegallop.feature.auth.domain.repository.AuthRepository
import com.horsegallop.feature.auth.domain.repository.AuthState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.horsegallop.core.util.Constants

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun signInWithGoogle(idToken: String): Flow<Result<User>> = flow {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("User is null")
            val user = fetchOrCreateUser(firebaseUser)
            emit(Result.success(user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signInWithApple(idToken: String): Flow<Result<User>> = flow {
        emit(Result.failure(Exception("Not implemented")))
    }

    override fun signInWithEmail(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User is null")
            val user = fetchUserFromFirestore(firebaseUser.uid, firebaseUser.isEmailVerified) ?: createDefaultUser(firebaseUser)
            emit(Result.success(user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>> = flow {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User is null")

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName("$firstName $lastName")
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            firebaseUser.sendEmailVerification().await()

            val user = createFirestoreUser(firebaseUser, firstName, lastName)
            emit(Result.success(user))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun resendVerificationEmail(): Flow<Result<Unit>> = flow {
        try {
            val user = auth.currentUser ?: throw Exception("No user signed in")
            user.sendEmailVerification().await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flow {
        try {
            val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                .setUrl("https://horsegallop.page.link/reset-password") // Fallback URL, but deep link is handled
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    "com.horsegallop",
                    true, /* installIfNotAvailable */
                    "21"    /* minimumVersion */
                )
                .build()
                
            auth.sendPasswordResetEmail(email, actionCodeSettings).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>> = flow {
        try {
            auth.confirmPasswordReset(code, newPassword).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun refreshToken(): Flow<Result<Unit>> = flow {
        try {
            auth.currentUser?.getIdToken(true)?.await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signOut(): Flow<Result<Unit>> = flow {
        try {
            auth.signOut()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(AuthState.Unauthenticated(null))
            } else {
                trySend(AuthState.Authenticated(
                    User(
                        id = user.uid,
                        role = UserRole.CUSTOMER,
                        name = user.displayName ?: "",
                        email = user.email ?: "",
                        isEmailVerified = user.isEmailVerified,
                        locale = null,
                        lastVisitIso = null
                    )
                ))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private suspend fun fetchOrCreateUser(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        return fetchUserFromFirestore(firebaseUser.uid, firebaseUser.isEmailVerified) ?: createDefaultUser(firebaseUser)
    }

    private suspend fun fetchUserFromFirestore(uid: String, isEmailVerified: Boolean): User? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (snapshot.exists()) {
             return User(
                id = uid,
                role = try { UserRole.valueOf(snapshot.getString("role") ?: "CUSTOMER") } catch(e:Exception) { UserRole.CUSTOMER },
                name = snapshot.getString("name") ?: "",
                email = snapshot.getString("email") ?: "",
                isEmailVerified = isEmailVerified,
                locale = snapshot.getString("locale"),
                lastVisitIso = snapshot.getString("lastVisit")
            )
        }
        return null
    }

    private suspend fun createDefaultUser(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        val nameParts = (firebaseUser.displayName ?: "").split(" ")
        val first = nameParts.firstOrNull() ?: ""
        val last = nameParts.drop(1).joinToString(" ")
        return createFirestoreUser(firebaseUser, first, last)
    }

    private suspend fun createFirestoreUser(firebaseUser: com.google.firebase.auth.FirebaseUser, firstName: String, lastName: String): User {
        val displayName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) "$firstName $lastName".trim() else firebaseUser.displayName ?: ""
        val user = User(
            id = firebaseUser.uid,
            role = UserRole.CUSTOMER,
            name = displayName,
            email = firebaseUser.email ?: "",
            isEmailVerified = firebaseUser.isEmailVerified,
            locale = null,
            lastVisitIso = null
        )
        // Save to Firestore
        firestore.collection("users").document(user.id).set(
            mapOf(
                "id" to user.id,
                "role" to user.role.name,
                "firstName" to firstName,
                "lastName" to lastName,
                "name" to user.name,
                "email" to user.email,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
        ).await()
        return user
    }
}
