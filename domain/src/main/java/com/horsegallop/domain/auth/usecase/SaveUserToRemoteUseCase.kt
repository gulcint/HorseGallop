package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.auth.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveUserToRemoteUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(user: UserProfile): Flow<Result<Unit>> {
        return authRepository.saveUserToRemote(user)
    }
}
