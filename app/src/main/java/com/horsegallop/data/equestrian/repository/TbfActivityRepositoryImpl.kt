package com.horsegallop.data.equestrian.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.toDomain
import com.horsegallop.domain.equestrian.model.TbfActivity
import com.horsegallop.domain.equestrian.repository.TbfActivityRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TbfActivityRepositoryImpl @Inject constructor(
    private val dataSource: SupabaseDataSource
) : TbfActivityRepository {

    override suspend fun getActivitiesForMonth(month: YearMonth): Result<List<TbfActivity>> =
        runCatching {
            val ym = "${month.year}-${month.monthValue.toString().padStart(2, '0')}"
            dataSource.getTbfActivities(yearMonth = ym).getOrThrow().map { it.toDomain() }
        }

    override suspend fun getActivitiesForDay(date: LocalDate): Result<List<TbfActivity>> =
        runCatching {
            val ym = "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
            dataSource.getTbfActivities(yearMonth = ym).getOrThrow()
                .map { it.toDomain() }
                .filter { !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }
        }
}
