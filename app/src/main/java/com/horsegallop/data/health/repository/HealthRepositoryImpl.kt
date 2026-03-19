package com.horsegallop.data.health.repository

import com.horsegallop.core.debug.AppLog
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseHealthEventDto
import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.model.HealthEventType
import com.horsegallop.domain.health.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : HealthRepository {

    override fun getHealthEvents(horseId: String?): Flow<List<HealthEvent>> {
        val userId = supabaseDataSource.currentUserId() ?: return flowOf(emptyList())

        return flow {
            val dtos = supabaseDataSource.getHealthEvents(horseId)
            val events = dtos.mapNotNull { dto ->
                runCatching { dto.toDomain() }
                    .onFailure { AppLog.e("HealthRepo", "Mapping error: ${it.message}") }
                    .getOrNull()
            }
            emit(events)
        }.catch {
            AppLog.e("HealthRepo", "getHealthEvents error: ${it.message}")
            emit(emptyList())
        }
    }

    override suspend fun saveHealthEvent(event: HealthEvent): Result<HealthEvent> = runCatching {
        val userId = supabaseDataSource.currentUserId()
            ?: throw IllegalStateException("Not authenticated")

        val dto = SupabaseHealthEventDto(
            id = event.id,
            userId = userId,
            horseId = event.horseId.ifBlank { null },
            horseName = event.horseName,
            type = event.type.name,
            scheduledDate = isoFromEpoch(event.scheduledDate),
            completedDate = event.completedDate?.let { isoFromEpoch(it) },
            notes = event.notes,
            isCompleted = event.isCompleted
        )
        val saved = supabaseDataSource.saveHealthEvent(dto)
        saved.toDomain()
    }

    override suspend fun deleteHealthEvent(eventId: String): Result<Unit> = runCatching {
        supabaseDataSource.deleteHealthEvent(eventId)
    }

    override suspend fun markCompleted(eventId: String, completedDate: Long): Result<Unit> =
        runCatching {
            supabaseDataSource.markHealthEventCompleted(eventId, isoFromEpoch(completedDate))
        }

    // ─── helpers ─────────────────────────────────────────────

    private fun isoFromEpoch(epochMs: Long): String =
        Instant.ofEpochMilli(epochMs).toString()

    private fun epochFromIso(iso: String): Long =
        runCatching { Instant.parse(iso).toEpochMilli() }.getOrDefault(0L)

    private fun SupabaseHealthEventDto.toDomain(): HealthEvent {
        val type = try {
            HealthEventType.valueOf(this.type)
        } catch (_: Exception) {
            HealthEventType.VET
        }
        return HealthEvent(
            id = this.id,
            userId = this.userId,
            horseId = this.horseId.orEmpty(),
            horseName = this.horseName,
            type = type,
            scheduledDate = epochFromIso(this.scheduledDate),
            completedDate = this.completedDate?.let { epochFromIso(it) },
            notes = this.notes,
            isCompleted = this.isCompleted
        )
    }
}
