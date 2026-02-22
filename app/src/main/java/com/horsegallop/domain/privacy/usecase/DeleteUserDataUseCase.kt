package com.horsegallop.domain.privacy.usecase

import com.horsegallop.domain.privacy.repository.PrivacyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteUserDataUseCase @Inject constructor(
    private val repository: PrivacyRepository
) {
    fun execute(): Flow<Result<Unit>> = repository.deleteUserData()
}
