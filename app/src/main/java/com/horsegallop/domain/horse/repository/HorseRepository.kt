package com.horsegallop.domain.horse.repository

import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.HorseTip
import kotlinx.coroutines.flow.Flow

interface HorseRepository {
    fun getMyHorses(): Flow<List<Horse>>
    suspend fun addHorse(horse: Horse): Result<Horse>
    suspend fun deleteHorse(horseId: String): Result<Unit>
    suspend fun getBreeds(locale: String): Result<List<String>>
    suspend fun getHorseTips(locale: String): Result<List<HorseTip>>
}
