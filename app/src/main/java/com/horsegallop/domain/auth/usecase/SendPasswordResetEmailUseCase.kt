package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(email: String): Flow<Result<Unit>> {
        return repository.sendPasswordResetEmail(email)
    }
}
