package com.horsegallop.domain.tbf.repository

import com.horsegallop.domain.tbf.model.TbfEventCard
import com.horsegallop.domain.tbf.model.TbfEventDay

interface TbfRepository {
    suspend fun getEventDay(date: String?, type: String): Result<TbfEventDay>
    suspend fun getEventCard(date: String?, venue: String, type: String): Result<TbfEventCard>
    suspend fun getUpcomingEvents(): Result<List<TbfEventDay>>
}
