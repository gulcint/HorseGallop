package com.horsegallop.domain.health.usecase

import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHealthEventsUseCase @Inject constructor(
    private val repo: HealthRepository
) {
    operator fun invoke(horseId: String? = null): Flow<List<HealthEvent>> =
        repo.getHealthEvents(horseId)
}
