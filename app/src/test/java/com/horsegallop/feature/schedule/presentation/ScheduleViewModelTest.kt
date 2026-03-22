package com.horsegallop.feature.schedule.presentation

import com.horsegallop.domain.schedule.model.Lesson
import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.model.ReservationStatus
import com.horsegallop.domain.schedule.usecase.BookLessonUseCase
import com.horsegallop.domain.schedule.usecase.CancelReservationUseCase
import com.horsegallop.domain.schedule.usecase.GetLessonsUseCase
import com.horsegallop.domain.schedule.usecase.GetMyReservationsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getLessonsUseCase: GetLessonsUseCase = mock()
    private val bookLessonUseCase: BookLessonUseCase = mock()
    private val cancelReservationUseCase: CancelReservationUseCase = mock()
    private val getMyReservationsUseCase: GetMyReservationsUseCase = mock()

    private lateinit var viewModel: ScheduleViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): ScheduleViewModel {
        return ScheduleViewModel(
            getLessonsUseCase,
            bookLessonUseCase,
            cancelReservationUseCase,
            getMyReservationsUseCase
        )
    }

    // ─── refresh ─────────────────────────────────────────────────────────────

    @Test
    fun `refresh loads lessons and updates uiState`() = runTest {
        val lessons = listOf(lesson("l1", "Sabah Dersi"))
        whenever(getLessonsUseCase()).thenReturn(flowOf(lessons))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))

        viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals(1, state.lessons.size)
        assertEquals("Sabah Dersi", state.lessons[0].title)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `refresh sets isEmpty true when lessons are empty`() = runTest {
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))

        viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `refresh sets error when getLessonsUseCase throws`() = runTest {
        whenever(getLessonsUseCase()).thenThrow(RuntimeException("Network error"))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))

        viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertNotNull(state.error)
    }

    // ─── bookLesson ──────────────────────────────────────────────────────────

    @Test
    fun `bookLesson sets bookingSuccess true on success`() = runTest {
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(bookLessonUseCase("lesson1")).thenReturn(Result.success(reservation("res1")))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.bookLesson("lesson1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.bookingSuccess)
        assertFalse(viewModel.uiState.value.bookingInProgress)
    }

    @Test
    fun `bookLesson sets bookingError on failure`() = runTest {
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(bookLessonUseCase("lesson1")).thenReturn(
            Result.failure(RuntimeException("Spot dolu"))
        )

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.bookLesson("lesson1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.bookingInProgress)
        assertFalse(state.bookingSuccess)
        assertEquals("Rezervasyon başarısız", state.bookingError)
    }

    @Test
    fun `bookLesson resets bookingInProgress to false after completion`() = runTest {
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(bookLessonUseCase("lesson1")).thenReturn(Result.success(reservation("r1")))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.bookLesson("lesson1")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.bookingInProgress)
        assertTrue(viewModel.uiState.value.bookingSuccess)
    }

    // ─── cancelReservation ───────────────────────────────────────────────────

    @Test
    fun `cancelReservation on success triggers loadReservations refresh`() = runTest {
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(cancelReservationUseCase("res1")).thenReturn(Result.success(Unit))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.cancelReservation("res1")
        advanceUntilIdle()

        // Verify no crash + state is still valid
        assertNull(viewModel.uiState.value.error)
    }

    // ─── clearBookingState ───────────────────────────────────────────────────

    @Test
    fun `clearBookingState resets bookingSuccess and bookingError`() = runTest {
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(bookLessonUseCase("lesson1")).thenReturn(Result.success(reservation("r1")))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.bookLesson("lesson1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.bookingSuccess)

        viewModel.clearBookingState()

        assertFalse(viewModel.uiState.value.bookingSuccess)
        assertNull(viewModel.uiState.value.bookingError)
    }

    // ─── loadReservations ────────────────────────────────────────────────────

    @Test
    fun `reservations are loaded on init`() = runTest {
        val reservations = listOf(reservation("r1"))
        whenever(getLessonsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getMyReservationsUseCase()).thenReturn(flowOf(reservations))

        viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.reservations.size)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun lesson(id: String, title: String) = Lesson(
        id = id, date = "2026-04-01", title = title,
        instructorName = "Hoca", durationMin = 60,
        level = "beginner", price = 150.0,
        spotsTotal = 5, spotsAvailable = 3, isBookedByMe = false
    )

    private fun reservation(id: String) = Reservation(
        id = id, lessonId = "l1", lessonTitle = "Test",
        lessonDate = "2026-04-01", instructorName = "Hoca",
        status = ReservationStatus.CONFIRMED
    )
}
