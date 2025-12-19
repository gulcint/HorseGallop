package com.horsegallop.domain.auth

import com.horsegallop.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogleIdToken(idToken: String)
    fun isSignedIn(): Boolean
    fun signOut()

    fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>>
    fun signInWithEmail(email: String, password: String): Flow<Result<User>>
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>>
    fun resendVerificationEmail(): Flow<Result<Unit>>
}
