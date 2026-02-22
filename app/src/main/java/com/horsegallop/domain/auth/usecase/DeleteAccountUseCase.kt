package com.horsegallop.domain.auth.usecase

import com.horsegallop.domain.auth.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<Result<Unit>> {
        return repository.deleteAccount()
    }
}
