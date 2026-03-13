package com.horsegallop.domain.safety.usecase

import com.horsegallop.domain.safety.repository.SafetyRepository
import javax.inject.Inject

class TriggerSafetyAlarmUseCase @Inject constructor(
    private val repository: SafetyRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double): Result<Unit> =
        repository.triggerSafetyAlarm(lat, lng)
}
