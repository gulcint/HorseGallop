package com.horsegallop.data.equestrian.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseTbfActivityDto
import com.horsegallop.domain.equestrian.model.TbfDiscipline
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.YearMonth

class TbfActivityRepositoryImplTest {
    private val dataSource: SupabaseDataSource = mock()
    private val repo = TbfActivityRepositoryImpl(dataSource)

    private fun sampleDto() = SupabaseTbfActivityDto(
        id = "1", startDate = "2026-03-19", endDate = "2026-03-22",
        title = "Test", organization = "TBF", city = "Ankara",
        discipline = "show_jumping", activityType = "incentive"
    )

    @Test
    fun `getActivitiesForMonth maps DTO to domain`() = runTest {
        whenever(dataSource.getTbfActivities(anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(listOf(sampleDto())))
        val result = repo.getActivitiesForMonth(YearMonth.of(2026, 3))
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(TbfDiscipline.SHOW_JUMPING, result.getOrNull()?.first()?.discipline)
    }

    @Test
    fun `getActivitiesForMonth returns failure on error`() = runTest {
        whenever(dataSource.getTbfActivities(anyOrNull(), anyOrNull()))
            .thenReturn(Result.failure(RuntimeException("network")))
        val result = repo.getActivitiesForMonth(YearMonth.of(2026, 3))
        assertTrue(result.isFailure)
    }

    @Test
    fun `getActivitiesForDay filters by date range`() = runTest {
        whenever(dataSource.getTbfActivities(anyOrNull(), anyOrNull()))
            .thenReturn(Result.success(listOf(sampleDto())))
        val result = repo.getActivitiesForDay(java.time.LocalDate.of(2026, 3, 20))
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size) // 20 Mart, 19-22 aralığında
    }
}
