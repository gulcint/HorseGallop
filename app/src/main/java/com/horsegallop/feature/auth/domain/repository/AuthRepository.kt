package com.horsegallop.feature.auth.domain.repository

import com.horsegallop.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

sealed class AuthState {
  data object Loading : AuthState()
  data class Authenticated(val user: User) : AuthState()
  data class Unauthenticated(val errorMessage: String?) : AuthState()
}

interface AuthRepository {
  fun signInWithGoogle(idToken: String): Flow<Result<User>>
  fun signInWithApple(idToken: String): Flow<Result<User>>
  fun signInWithEmail(email: String, password: String): Flow<Result<User>>
  fun signUpWithEmail(email: String, password: String, firstName: String, lastName: String): Flow<Result<User>>
    fun resendVerificationEmail(): Flow<Result<Unit>>
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>>
    fun refreshToken(): Flow<Result<Unit>>
  fun signOut(): Flow<Result<Unit>>
  fun observeAuthState(): Flow<AuthState>
}
