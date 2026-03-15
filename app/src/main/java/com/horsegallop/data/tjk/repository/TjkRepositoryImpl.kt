package com.horsegallop.data.tjk.repository

import com.horsegallop.data.remote.dto.TjkHippodromeDto
import com.horsegallop.data.remote.dto.TjkHorseDto
import com.horsegallop.data.remote.dto.TjkRaceCardDto
import com.horsegallop.data.remote.dto.TjkRaceDayDto
import com.horsegallop.data.remote.dto.TjkRaceDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.tjk.model.TjkHippodrome
import com.horsegallop.domain.tjk.model.TjkHorse
import com.horsegallop.domain.tjk.model.TjkRace
import com.horsegallop.domain.tjk.model.TjkRaceCard
import com.horsegallop.domain.tjk.model.TjkRaceDay
import com.horsegallop.domain.tjk.repository.TjkRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TjkRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : TjkRepository {

    override suspend fun getRaceDay(date: String?, type: String): Result<TjkRaceDay> =
        runCatching {
            functionsDataSource.getTjkRaceDay(date, type).toDomain()
        }

    override suspend fun getRaceCard(date: String?, hippodrome: String, type: String): Result<TjkRaceCard> =
        runCatching {
            functionsDataSource.getTjkRaceCard(date, hippodrome, type).toDomain()
        }

    override suspend fun getUpcomingRaces(): Result<List<TjkRaceDay>> =
        runCatching {
            functionsDataSource.getTjkUpcomingRaces().days.map { it.toDomain() }
        }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private fun TjkRaceDayDto.toDomain() = TjkRaceDay(
        date = date,
        type = type,
        hippodromes = hippodromes.map { it.toDomain() }
    )

    private fun TjkHippodromeDto.toDomain() = TjkHippodrome(
        code = code,
        name = name,
        raceCount = raceCount,
        time = time
    )

    private fun TjkRaceCardDto.toDomain() = TjkRaceCard(
        hippodrome = hippodrome,
        date = date,
        type = type,
        races = races.map { it.toDomain() },
        weather = weather,
        trackCondition = trackCondition
    )

    private fun TjkRaceDto.toDomain() = TjkRace(
        no = no,
        name = name,
        distance = distance,
        surface = surface,
        time = time,
        prize = prize,
        horses = horses.map { it.toDomain() }
    )

    private fun TjkHorseDto.toDomain() = TjkHorse(
        no = no,
        name = name,
        jockey = jockey,
        trainer = trainer,
        owner = owner,
        weight = weight,
        age = age,
        last6 = last6,
        odds = odds,
        bestTime = bestTime,
        result = result,
        time = time,
        gap = gap
    )
}
