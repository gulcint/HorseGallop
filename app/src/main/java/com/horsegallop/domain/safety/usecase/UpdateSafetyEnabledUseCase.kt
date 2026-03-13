package com.horsegallop.domain.safety.usecase

import com.horsegallop.domain.safety.repository.SafetyRepository
import javax.inject.Inject

class UpdateSafetyEnabledUseCase @Inject constructor(
    private val repository: SafetyRepository
) {
    suspend operator fun invoke(isEnabled: Boolean): Result<Unit> =
        repository.updateSafetyEnabled(isEnabled)
}
