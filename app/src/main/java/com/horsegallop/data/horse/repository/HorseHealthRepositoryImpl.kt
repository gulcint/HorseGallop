package com.horsegallop.data.horse.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType
import com.horsegallop.domain.horse.repository.HorseHealthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HorseHealthRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : HorseHealthRepository {

    override suspend fun getHealthEvents(horseId: String): Result<List<HorseHealthEvent>> =
        runCatching {
            functionsDataSource.getHorseHealthEvents(horseId).map { dto ->
                HorseHealthEvent(
                    id = dto.id,
                    horseId = dto.horseId,
                    type = HorseHealthEventType.fromString(dto.type),
                    date = dto.date,
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
        val dto = functionsDataSource.addHorseHealthEvent(horseId, type.name, date, notes)
        HorseHealthEvent(
            id = dto.id,
            horseId = dto.horseId,
            type = HorseHealthEventType.fromString(dto.type),
            date = dto.date,
            notes = dto.notes,
            createdAt = dto.createdAt
        )
    }

    override suspend fun updateHealthEvent(
        id: String,
        horseId: String,
        type: HorseHealthEventType?,
        date: String?,
        notes: String?
    ): Result<Unit> = runCatching {
        functionsDataSource.updateHorseHealthEvent(id, horseId, type?.name, date, notes)
    }

    override suspend fun deleteHealthEvent(id: String, horseId: String): Result<Unit> =
        runCatching { functionsDataSource.deleteHorseHealthEvent(id, horseId) }
}
