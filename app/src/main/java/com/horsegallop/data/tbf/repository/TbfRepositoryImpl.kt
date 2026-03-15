package com.horsegallop.data.tbf.repository

import com.horsegallop.data.remote.dto.TbfVenueDto
import com.horsegallop.data.remote.dto.TbfAthleteDto
import com.horsegallop.data.remote.dto.TbfEventCardDto
import com.horsegallop.data.remote.dto.TbfEventDayDto
import com.horsegallop.data.remote.dto.TbfCompetitionDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.tbf.model.TbfVenue
import com.horsegallop.domain.tbf.model.TbfAthlete
import com.horsegallop.domain.tbf.model.TbfCompetition
import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.domain.tbf.model.TbfEventDay
import com.horsegallop.domain.tbf.repository.TbfRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TbfRepositoryImpl @Inject constructor(
    private val functionsDataSource: AppFunctionsDataSource
) : TbfRepository {

    override suspend fun getEventDay(date: String?, type: String): Result<TbfEventDay> =
        runCatching {
            functionsDataSource.getTbfEventDay(date, type).toDomain()
        }

    override suspend fun getEventCard(date: String?, venue: String, type: String): Result<TbfEventCard> =
        runCatching {
            functionsDataSource.getTbfEventCard(date, venue, type).toDomain()
        }

    override suspend fun getUpcomingEvents(): Result<List<TbfEventDay>> =
        runCatching {
            functionsDataSource.getTbfUpcomingEvents().days.map { it.toDomain() }
        }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private fun TbfEventDayDto.toDomain() = TbfEventDay(
        date = date,
        type = type,
        venues = venues.map { it.toDomain() }
    )

    private fun TbfVenueDto.toDomain() = TbfVenue(
        code = code,
        name = name,
        eventCount = eventCount,
        time = time
    )

    private fun TbfEventCardDto.toDomain() = TbfEventCard(
        venue = venue,
        date = date,
        type = type,
        events = events.map { it.toDomain() },
        weather = weather,
        trackCondition = trackCondition
    )

    private fun TbfCompetitionDto.toDomain() = TbfCompetition(
        no = no,
        name = name,
        distance = distance,
        surface = surface,
        time = time,
        prize = prize,
        athletes = athletes.map { it.toDomain() }
    )

    private fun TbfAthleteDto.toDomain() = TbfAthlete(
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
