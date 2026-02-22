package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResendVerificationEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(email: String? = null, password: String? = null): Flow<Result<Unit>> {
        return repository.resendVerificationEmail(email, password)
    }
}
