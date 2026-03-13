package com.horsegallop.domain.horse.repository

import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType

interface HorseHealthRepository {
    suspend fun getHealthEvents(horseId: String): Result<List<HorseHealthEvent>>
    suspend fun addHealthEvent(
        horseId: String,
        type: HorseHealthEventType,
        date: String,
        notes: String
    ): Result<HorseHealthEvent>
    suspend fun updateHealthEvent(
        id: String,
        horseId: String,
        type: HorseHealthEventType? = null,
        date: String? = null,
        notes: String? = null
    ): Result<Unit>
    suspend fun deleteHealthEvent(id: String, horseId: String): Result<Unit>
}
