package com.horsegallop.domain.health.usecase

import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.repository.HealthRepository
import javax.inject.Inject

class SaveHealthEventUseCase @Inject constructor(
    private val repo: HealthRepository
) {
    suspend operator fun invoke(event: HealthEvent): Result<HealthEvent> =
        repo.saveHealthEvent(event)
}
