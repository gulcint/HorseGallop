package com.horsegallop.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.model.User
import com.horsegallop.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun signInWithGoogleIdToken(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override fun signOut() {
        auth.signOut()
    }

    override fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>> = flow {
        var createdUser: com.google.firebase.auth.FirebaseUser? = null
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            createdUser = result.user ?: throw Exception("User creation failed")
            
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("$firstName $lastName")
                    .build()
                createdUser.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                // Profile update failed, but user exists. Continue.
            }

            try {
                // Use standard email verification to avoid ActionCodeSettings errors
                createdUser.sendEmailVerification().await()
            } catch (e: Exception) {
                // Email sending failed. Do NOT delete the user.
                // Let the UI handle the verification step (user can resend).
            }
            
            val domainUser = User(
                id = createdUser.uid,
                role = UserRole.CUSTOMER,
                name = createdUser.displayName ?: "$firstName $lastName",
                email = createdUser.email ?: email,
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
            
            val domainUser = User(
                id = user.uid,
                role = UserRole.CUSTOMER,
                name = user.displayName ?: "",
                email = user.email ?: email,
                isEmailVerified = user.isEmailVerified,
                locale = null,
                lastVisitIso = null
            )
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
}
