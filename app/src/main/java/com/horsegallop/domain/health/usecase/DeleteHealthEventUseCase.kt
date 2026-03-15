package com.horsegallop.domain.health.usecase

import com.horsegallop.domain.health.repository.HealthRepository
import javax.inject.Inject

class DeleteHealthEventUseCase @Inject constructor(
    private val repo: HealthRepository
) {
    suspend operator fun invoke(eventId: String): Result<Unit> =
        repo.deleteHealthEvent(eventId)
}
