package com.horsegallop.domain.tjk.repository

import com.horsegallop.domain.tjk.model.TjkRaceCard
import com.horsegallop.domain.tjk.model.TjkRaceDay

interface TjkRepository {
    suspend fun getRaceDay(date: String?, type: String): Result<TjkRaceDay>
    suspend fun getRaceCard(date: String?, hippodrome: String, type: String): Result<TjkRaceCard>
    suspend fun getUpcomingRaces(): Result<List<TjkRaceDay>>
}
