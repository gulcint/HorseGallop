package com.horsegallop.domain.safety.usecase

import com.horsegallop.domain.safety.repository.SafetyRepository
import javax.inject.Inject

class RemoveSafetyContactUseCase @Inject constructor(
    private val repository: SafetyRepository
) {
    suspend operator fun invoke(contactId: String): Result<Unit> =
        repository.removeSafetyContact(contactId)
}
