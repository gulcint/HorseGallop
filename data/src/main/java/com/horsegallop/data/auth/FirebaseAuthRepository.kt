package com.horsegallop.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.model.User
import com.horsegallop.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    override suspend fun signInWithGoogleIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        val user = authResult.user
        if (user != null) {
            try {
                fetchOrCreateUser(user)
            } catch (e: Exception) {
                // Ignore Firestore errors (e.g. offline) to allow login to proceed
                e.printStackTrace()
            }
        }
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override fun signOut() {
        auth.signOut()
    }

    override fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>> = flow {
        try {
            val result = withTimeout(15000) { auth.createUserWithEmailAndPassword(email, password).await() }
            val createdUser = result.user ?: throw Exception("User creation failed")
            
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $lastName")
                    .build()
                withTimeout(5000) { createdUser.updateProfile(profileUpdates).await() }
            } catch (e: Exception) {
                // Profile update failed, but user exists. Continue.
                e.printStackTrace()
            }

            try {
                // Email doğrulamayı asenkron tetikle; akışı bekletme
                createdUser.sendEmailVerification()
            } catch (e: Exception) {
                // Email gönderimi başlatılamadı; kullanıcı yine de çekmece üzerinden yeniden gönderebilir
                e.printStackTrace()
            }
            
            val domainUser = User(
                id = createdUser.uid,
                role = UserRole.CUSTOMER,
                name = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { createdUser.displayName ?: "" },
                email = createdUser.email ?: "",
                isEmailVerified = createdUser.isEmailVerified,
                locale = null,
                lastVisitIso = null
            )
            emit(Result.success(domainUser))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun signInWithEmail(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign in failed")
            
            if (!user.isEmailVerified) {
                auth.signOut()
                throw Exception("Email not verified")
            }
            
            val domainUser = fetchOrCreateUser(user)
            emit(Result.success(domainUser))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flow {
        try {
            auth.sendPasswordResetEmail(email).await()
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

    override fun resendVerificationEmail(): Flow<Result<Unit>> = flow {
        try {
            val user = auth.currentUser ?: throw Exception("No user signed in")
            user.sendEmailVerification().await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
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
