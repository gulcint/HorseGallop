package com.horsegallop.feature.auth.domain

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConfirmPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(code: String, newPassword: String): Flow<Result<Unit>> {
        return repository.confirmPasswordReset(code, newPassword)
    }
}
