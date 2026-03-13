package com.horsegallop.domain.tjk.repository

import com.horsegallop.domain.tjk.model.TjkCity
import com.horsegallop.domain.tjk.model.TjkRaceDay

interface TjkRepository {
    suspend fun getRaceDay(date: String, cityId: Int): Result<TjkRaceDay>
    suspend fun getCities(): Result<List<TjkCity>>
}
