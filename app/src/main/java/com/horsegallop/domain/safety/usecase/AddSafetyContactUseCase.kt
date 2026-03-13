package com.horsegallop.domain.safety.usecase

import com.horsegallop.domain.safety.model.SafetyContact
import com.horsegallop.domain.safety.repository.SafetyRepository
import javax.inject.Inject

class AddSafetyContactUseCase @Inject constructor(
    private val repository: SafetyRepository
) {
    suspend operator fun invoke(name: String, phone: String): Result<SafetyContact> =
        repository.addSafetyContact(name, phone)
}
