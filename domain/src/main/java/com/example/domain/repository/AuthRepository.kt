package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

sealed class AuthState {
  data object Loading : AuthState()
  data class Authenticated(val user: User) : AuthState()
  data class Unauthenticated(val errorMessage: String?) : AuthState()
}

interface AuthRepository {
  fun signInWithGoogle(idToken: String): Flow<Result<User>>
  fun signInWithApple(idToken: String): Flow<Result<User>>
  fun refreshToken(): Flow<Result<Unit>>
  fun signOut(): Flow<Result<Unit>>
  fun observeAuthState(): Flow<AuthState>
}
