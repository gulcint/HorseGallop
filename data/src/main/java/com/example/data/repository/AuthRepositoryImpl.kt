package com.example.data.repository

import com.example.data.remote.ApiService
import com.example.data.remote.dto.AuthRequestDto
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.AuthState
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class AuthRepositoryImpl @Inject constructor(
  private val api: ApiService
) : AuthRepository {

  private val authChannel: Channel<AuthState> = Channel(capacity = Channel.BUFFERED)

  override fun signInWithGoogle(idToken: String): Flow<Result<User>> {
    return flow {
      authChannel.trySend(AuthState.Loading)
      try {
        val response = api.postAuthGoogle(AuthRequestDto(idToken))
        val user = mapToUser(response.user)
        authChannel.trySend(AuthState.Authenticated(user))
        emit(Result.success(user))
      } catch (e: Exception) {
        authChannel.trySend(AuthState.Unauthenticated(e.message))
        emit(Result.failure(e))
      }
    }
  }

  override fun signInWithApple(idToken: String): Flow<Result<User>> {
    return flow {
      authChannel.trySend(AuthState.Loading)
      try {
        val response = api.postAuthApple(AuthRequestDto(idToken))
        val user = mapToUser(response.user)
        authChannel.trySend(AuthState.Authenticated(user))
        emit(Result.success(user))
      } catch (e: Exception) {
        authChannel.trySend(AuthState.Unauthenticated(e.message))
        emit(Result.failure(e))
      }
    }
  }

  override fun refreshToken(): Flow<Result<Unit>> {
    return flow { emit(Result.success(Unit)) } // TODO implement with backend
  }

  override fun signOut(): Flow<Result<Unit>> {
    return flow {
      authChannel.trySend(AuthState.Unauthenticated(null))
      emit(Result.success(Unit))
    }
  }

  override fun observeAuthState(): Flow<AuthState> {
    return authChannel.receiveAsFlow()
  }

  private fun mapToUser(userDto: com.example.data.remote.dto.UserDto): User {
    return User(
      id = userDto.id,
      role = when (userDto.role) {
        "ADMIN" -> UserRole.ADMIN
        "INSTRUCTOR" -> UserRole.INSTRUCTOR
        else -> UserRole.CUSTOMER
      },
      name = userDto.name,
      email = userDto.email,
      locale = userDto.locale,
      lastVisitIso = userDto.lastVisit
    )
  }
}
