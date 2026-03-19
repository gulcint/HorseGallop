package com.horsegallop.domain.equestrian.repository

import com.horsegallop.domain.equestrian.model.TbfActivity
import java.time.LocalDate
import java.time.YearMonth

interface TbfActivityRepository {
    suspend fun getActivitiesForMonth(month: YearMonth): Result<List<TbfActivity>>
    suspend fun getActivitiesForDay(date: LocalDate): Result<List<TbfActivity>>
}
