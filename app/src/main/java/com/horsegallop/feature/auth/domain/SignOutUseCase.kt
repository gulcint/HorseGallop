package com.horsegallop.feature.auth.domain

import com.horsegallop.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(): Flow<Result<Unit>> {
        return repository.signOut()
    }
}
