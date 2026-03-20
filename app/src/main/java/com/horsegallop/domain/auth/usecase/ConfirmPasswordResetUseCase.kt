package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConfirmPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(email: String, code: String, newPassword: String): Flow<Result<Unit>> {
        return repository.confirmPasswordReset(email, code, newPassword)
    }
}
