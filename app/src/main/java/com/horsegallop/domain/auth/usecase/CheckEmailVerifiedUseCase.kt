package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckEmailVerifiedUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<Boolean>> {
        return authRepository.checkEmailVerified()
    }
}
