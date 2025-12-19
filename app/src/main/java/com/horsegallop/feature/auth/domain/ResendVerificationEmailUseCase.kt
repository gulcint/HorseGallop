package com.horsegallop.feature.auth.domain

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResendVerificationEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    fun execute(): Flow<Result<Unit>> {
        return repository.resendVerificationEmail()
    }
}
