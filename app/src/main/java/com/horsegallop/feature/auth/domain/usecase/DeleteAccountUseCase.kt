package com.horsegallop.feature.auth.domain.usecase

import com.horsegallop.feature.auth.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<Result<Unit>> {
        return repository.deleteAccount()
    }
}
