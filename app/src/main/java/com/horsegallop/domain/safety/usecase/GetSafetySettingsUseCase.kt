package com.horsegallop.domain.safety.usecase

import com.horsegallop.domain.safety.model.SafetySettings
import com.horsegallop.domain.safety.repository.SafetyRepository
import javax.inject.Inject

class GetSafetySettingsUseCase @Inject constructor(
    private val repository: SafetyRepository
) {
    suspend operator fun invoke(): Result<SafetySettings> = repository.getSafetySettings()
}
