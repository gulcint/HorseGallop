package com.horsegallop.domain.health.repository

import com.horsegallop.domain.health.model.HealthEvent
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    fun getHealthEvents(horseId: String?): Flow<List<HealthEvent>>
    suspend fun saveHealthEvent(event: HealthEvent): Result<HealthEvent>
    suspend fun deleteHealthEvent(eventId: String): Result<Unit>
    suspend fun markCompleted(eventId: String, completedDate: Long): Result<Unit>
}
