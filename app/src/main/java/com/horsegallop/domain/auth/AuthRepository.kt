package com.horsegallop.domain.auth

import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithGoogleIdToken(idToken: String)
    fun isSignedIn(): Boolean
    suspend fun signOut()

    fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>>
    fun signInWithEmail(email: String, password: String): Flow<Result<User>>
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    fun confirmPasswordReset(email: String, code: String, newPassword: String): Flow<Result<Unit>>
    fun resendVerificationEmail(email: String? = null, password: String? = null): Flow<Result<Unit>>
    fun checkEmailVerified(): Flow<Result<Boolean>>
    fun saveUserToRemote(user: UserProfile): Flow<Result<Unit>>
    fun getLottieConfig(): Flow<Result<Pair<String, String>>>
    fun getSplashTexts(locale: String): Flow<Result<Pair<String, String>>>
    fun getCurrentUserId(): String?
}
