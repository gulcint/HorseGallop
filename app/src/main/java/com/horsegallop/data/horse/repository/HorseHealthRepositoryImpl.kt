package com.horsegallop.data.horse.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseHorseHealthEventDto
import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType
import com.horsegallop.domain.horse.repository.HorseHealthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HorseHealthRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : HorseHealthRepository {

    override suspend fun getHealthEvents(horseId: String): Result<List<HorseHealthEvent>> =
        runCatching {
            supabaseDataSource.getHorseHealthEvents(horseId).map { dto ->
                HorseHealthEvent(
                    id = dto.id,
                    horseId = dto.horseId,
                    type = HorseHealthEventType.fromString(dto.type),
                    date = dto.eventDate,
                    notes = dto.notes,
                    createdAt = dto.createdAt
                )
            }
        }

    override suspend fun addHealthEvent(
        horseId: String,
        type: HorseHealthEventType,
        date: String,
        notes: String
    ): Result<HorseHealthEvent> = runCatching {
        val dto = SupabaseHorseHealthEventDto(
            horseId = horseId,
            type = type.name,
            eventDate = date,
            notes = notes
        )
        val saved = supabaseDataSource.addHorseHealthEvent(dto)
        HorseHealthEvent(
            id = saved.id,
            horseId = saved.horseId,
            type = HorseHealthEventType.fromString(saved.type),
            date = saved.eventDate,
            notes = saved.notes,
            createdAt = saved.createdAt
        )
    }

    override suspend fun updateHealthEvent(
        id: String,
        horseId: String,
        type: HorseHealthEventType?,
        date: String?,
        notes: String?
    ): Result<Unit> = runCatching {
        val updates = buildMap<String, Any?> {
            if (type != null) put("type", type.name)
            if (date != null) put("event_date", date)
            if (notes != null) put("notes", notes)
        }
        supabaseDataSource.updateHorseHealthEvent(id, updates)
    }

    override suspend fun deleteHealthEvent(id: String, horseId: String): Result<Unit> =
        runCatching { supabaseDataSource.deleteHorseHealthEvent(id) }
}
