package com.horsegallop.feature.equestrian.presentation

import com.horsegallop.domain.equestrian.model.TbfActivity
import com.horsegallop.domain.equestrian.model.TbfActivityType
import com.horsegallop.domain.equestrian.model.TbfDiscipline
import com.horsegallop.domain.equestrian.usecase.GetTbfActivitiesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TbfActivityViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val useCase: GetTbfActivitiesUseCase = mock()
    private lateinit var viewModel: TbfActivityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has current month and loading false after load`() = runTest {
        whenever(useCase(any())).thenReturn(Result.success(emptyList()))
        viewModel = TbfActivityViewModel(useCase)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(YearMonth.now(), state.currentMonth)
    }

    @Test
    fun `nextMonth advances currentMonth by one`() = runTest {
        whenever(useCase(any())).thenReturn(Result.success(emptyList()))
        viewModel = TbfActivityViewModel(useCase)
        advanceUntilIdle()
        val initial = viewModel.uiState.value.currentMonth
        viewModel.nextMonth()
        advanceUntilIdle()
        assertEquals(initial.plusMonths(1), viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `previousMonth decrements currentMonth by one`() = runTest {
        whenever(useCase(any())).thenReturn(Result.success(emptyList()))
        viewModel = TbfActivityViewModel(useCase)
        advanceUntilIdle()
        val initial = viewModel.uiState.value.currentMonth
        viewModel.previousMonth()
        advanceUntilIdle()
        assertEquals(initial.minusMonths(1), viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `selectDay updates selectedDay and filters activities`() = runTest {
        val date = LocalDate.of(2026, 3, 19)
        val activity = TbfActivity(
            id = "1",
            startDate = date,
            endDate = date,
            title = "Test",
            organization = "TBF",
            city = "Ankara",
            discipline = TbfDiscipline.SHOW_JUMPING,
            type = TbfActivityType.INCENTIVE
        )
        whenever(useCase(any())).thenReturn(Result.success(listOf(activity)))
        viewModel = TbfActivityViewModel(useCase)
        advanceUntilIdle()

        viewModel.selectDay(date)

        assertEquals(date, viewModel.uiState.value.selectedDay)
        assertEquals(1, viewModel.uiState.value.activitiesForSelectedDay.size)
    }

    @Test
    fun `toggleDisciplineFilter adds and removes discipline`() = runTest {
        whenever(useCase(any())).thenReturn(Result.success(emptyList()))
        viewModel = TbfActivityViewModel(useCase)
        advanceUntilIdle()
        viewModel.toggleDisciplineFilter(TbfDiscipline.SHOW_JUMPING)
        assertTrue(viewModel.uiState.value.disciplineFilters.contains(TbfDiscipline.SHOW_JUMPING))
        viewModel.toggleDisciplineFilter(TbfDiscipline.SHOW_JUMPING)
        assertFalse(viewModel.uiState.value.disciplineFilters.contains(TbfDiscipline.SHOW_JUMPING))
    }
}
