package com.horsegallop.data.tbf.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseTbfAthleteDto
import com.horsegallop.data.remote.supabase.SupabaseTbfCompetitionDto
import com.horsegallop.data.remote.supabase.SupabaseTbfEventDto
import com.horsegallop.domain.tbf.model.TbfAthlete
import com.horsegallop.domain.tbf.model.TbfCompetition
import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.domain.tbf.model.TbfEventDay
import com.horsegallop.domain.tbf.model.TbfVenue
import com.horsegallop.domain.tbf.repository.TbfRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TbfRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : TbfRepository {

    override suspend fun getEventDay(date: String?, type: String): Result<TbfEventDay> =
        runCatching {
            val events = supabaseDataSource.getTbfEventDays(date, type)
            events.groupByDate().firstOrNull()
                ?: TbfEventDay(date = date ?: "", type = type, venues = emptyList())
        }

    override suspend fun getEventCard(date: String?, venue: String, type: String): Result<TbfEventCard> =
        runCatching {
            val events = supabaseDataSource.getTbfEventDays(date, type)
            val venueEvent = events.firstOrNull { it.venueCode == venue || it.venueName == venue }
            val competitions = if (venueEvent != null) {
                supabaseDataSource.getTbfCompetitions(venueEvent.id).map { it.toDomain() }
            } else {
                emptyList()
            }
            TbfEventCard(
                venue = venue,
                date = date ?: venueEvent?.date ?: "",
                type = type,
                events = competitions,
                weather = "",
                trackCondition = ""
            )
        }

    override suspend fun getUpcomingEvents(): Result<List<TbfEventDay>> =
        runCatching {
            val events = supabaseDataSource.getTbfEventDays()
            events.groupByDate()
        }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private fun List<SupabaseTbfEventDto>.groupByDate(): List<TbfEventDay> {
        return groupBy { it.date to it.type }.map { (key, rows) ->
            TbfEventDay(
                date = key.first,
                type = key.second,
                venues = rows.map { row ->
                    TbfVenue(
                        code = row.venueCode,
                        name = row.venueName,
                        eventCount = row.eventCount,
                        time = row.time
                    )
                }
            )
        }
    }

    private fun SupabaseTbfCompetitionDto.toDomain() = TbfCompetition(
        no = no,
        name = name,
        distance = distance,
        surface = surface,
        time = time,
        prize = prize,
        athletes = athletes.map { it.toDomain() }
    )

    private fun SupabaseTbfAthleteDto.toDomain() = TbfAthlete(
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
