package com.horsegallop.data.tjk.repository

import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.tjk.model.TjkCity
import com.horsegallop.domain.tjk.model.TjkRace
import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.model.TjkRaceResult
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TjkRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : TjkRepository {

    override suspend fun getRaceDay(date: String, cityId: Int): Result<TjkRaceDay> = runCatching {
        val dto = functionsDataSource.getTjkRaceDay(date, cityId)
        TjkRaceDay(
            date = dto.date,
            cityId = dto.cityId,
            cityName = dto.cityName,
            races = dto.races.map { r ->
                TjkRace(
                    raceNo = r.raceNo,
                    raceTitle = r.raceTitle,
                    distance = r.distance,
                    surface = r.surface,
                    startTime = r.startTime,
                    results = r.results.map { res ->
                        TjkRaceResult(
                            position = res.position,
                            horseName = res.horseName,
                            jockey = res.jockey,
                            trainer = res.trainer,
                            weight = res.weight,
                            time = res.time
                        )
                    }
                )
            }
        )
    }

    override suspend fun getCities(): Result<List<TjkCity>> = runCatching {
        functionsDataSource.getTjkCities().map { TjkCity(id = it.id, name = it.name) }
    }
}
