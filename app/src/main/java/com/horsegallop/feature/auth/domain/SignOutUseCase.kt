package com.horsegallop.feature.auth.domain

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(): Flow<Result<Unit>> = flow {
        try {
            repository.signOut()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
