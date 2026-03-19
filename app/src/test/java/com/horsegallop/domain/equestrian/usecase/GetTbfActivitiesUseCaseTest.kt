package com.horsegallop.domain.equestrian.usecase

import com.horsegallop.domain.equestrian.repository.TbfActivityRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.YearMonth

class GetTbfActivitiesUseCaseTest {
    private val repo: TbfActivityRepository = mock()
    private val useCase = GetTbfActivitiesUseCase(repo)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val month = YearMonth.of(2026, 3)
        whenever(repo.getActivitiesForMonth(month)).thenReturn(Result.success(emptyList()))
        val result = useCase(month)
        verify(repo).getActivitiesForMonth(month)
        assertTrue(result.isSuccess)
    }
}
