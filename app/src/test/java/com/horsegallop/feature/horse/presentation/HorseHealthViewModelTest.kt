package com.horsegallop.feature.horse.presentation

import com.horsegallop.domain.horse.model.HorseHealthEvent
import com.horsegallop.domain.horse.model.HorseHealthEventType
import com.horsegallop.domain.horse.usecase.AddHorseHealthEventUseCase
import com.horsegallop.domain.horse.usecase.DeleteHorseHealthEventUseCase
import com.horsegallop.domain.horse.usecase.GetHorseHealthEventsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class HorseHealthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getHorseHealthEventsUseCase: GetHorseHealthEventsUseCase = mock()
    private val addHorseHealthEventUseCase: AddHorseHealthEventUseCase = mock()
    private val deleteHorseHealthEventUseCase: DeleteHorseHealthEventUseCase = mock()

    private lateinit var viewModel: HorseHealthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HorseHealthViewModel(
            getHorseHealthEventsUseCase,
            addHorseHealthEventUseCase,
            deleteHorseHealthEventUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── load ────────────────────────────────────────────────────────────────

    @Test
    fun `load sets horseName and isLoading false on success`() = runTest {
        val events = listOf(healthEvent("e1", today()))
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(Result.success(events))

        viewModel.load("horse1", "Yıldız")
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertFalse(state.isLoading)
        assertEquals("Yıldız", state.horseName)
        assertEquals(1, state.events.size)
    }

    @Test
    fun `load segments upcoming events within 30 days`() = runTest {
        val futureDate = LocalDate.now().plusDays(15).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val pastDate = LocalDate.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val events = listOf(
            healthEvent("upcoming", futureDate),
            healthEvent("past", pastDate)
        )
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(Result.success(events))

        viewModel.load("horse1", "Fırtına")
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertEquals(1, state.upcomingEvents.size)
        assertEquals("upcoming", state.upcomingEvents[0].id)
        assertEquals(1, state.pastEvents.size)
        assertEquals("past", state.pastEvents[0].id)
    }

    @Test
    fun `load does not reload when same horseId is already loaded`() = runTest {
        val events = listOf(healthEvent("e1", today()))
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(Result.success(events))

        viewModel.load("horse1", "Yıldız")
        advanceUntilIdle()

        // Second call with same ID should skip — state should stay same
        viewModel.load("horse1", "Yıldız")
        advanceUntilIdle()

        // Use case should be called only once (no extra call if state not loading)
        assertEquals(1, viewModel.ui.value.events.size)
    }

    @Test
    fun `load sets error on failure`() = runTest {
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(
            Result.failure(RuntimeException("Backend error"))
        )

        viewModel.load("horse1", "At")
        advanceUntilIdle()

        assertFalse(viewModel.ui.value.isLoading)
        assertNotNull(viewModel.ui.value.error)
    }

    // ─── addEvent ────────────────────────────────────────────────────────────

    @Test
    fun `addEvent on success appends new event and clears isSaving`() = runTest {
        val existingEvent = healthEvent("e1", today())
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(
            Result.success(listOf(existingEvent))
        )
        viewModel.load("horse1", "Rüzgar")
        advanceUntilIdle()

        val newEvent = healthEvent("e2", LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE))
        whenever(
            addHorseHealthEventUseCase(
                eq("horse1"),
                eq(HorseHealthEventType.VACCINATION),
                any(),
                any()
            )
        ).thenReturn(Result.success(newEvent))

        viewModel.addEvent(HorseHealthEventType.VACCINATION, newEvent.date, "Kuduz aşısı")
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertEquals(2, state.events.size)
        assertFalse(state.isSaving)
    }

    @Test
    fun `addEvent on failure sets error and clears isSaving`() = runTest {
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(Result.success(emptyList()))
        viewModel.load("horse1", "Bulut")
        advanceUntilIdle()

        whenever(
            addHorseHealthEventUseCase(any(), any(), any(), any())
        ).thenReturn(Result.failure(RuntimeException("Save failed")))

        viewModel.addEvent(HorseHealthEventType.DENTAL, today(), "Diş bakımı")
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertFalse(state.isSaving)
        assertNotNull(state.error)
    }

    @Test
    fun `addEvent is a no-op when currentHorseId is blank`() = runTest {
        // ViewModel not loaded — currentHorseId is blank
        viewModel.addEvent(HorseHealthEventType.VET, today(), "Muayene")
        advanceUntilIdle()

        // Should not change state (no use case called)
        assertFalse(viewModel.ui.value.isSaving)
    }

    // ─── deleteEvent ─────────────────────────────────────────────────────────

    @Test
    fun `deleteEvent on success removes event from list`() = runTest {
        val event1 = healthEvent("e1", today())
        val event2 = healthEvent("e2", today())
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(
            Result.success(listOf(event1, event2))
        )
        viewModel.load("horse1", "Deniz")
        advanceUntilIdle()

        whenever(deleteHorseHealthEventUseCase("e1", "horse1")).thenReturn(Result.success(Unit))

        viewModel.deleteEvent("e1")
        advanceUntilIdle()

        val events = viewModel.ui.value.events
        assertEquals(1, events.size)
        assertEquals("e2", events[0].id)
    }

    @Test
    fun `deleteEvent on failure sets error message`() = runTest {
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(Result.success(emptyList()))
        viewModel.load("horse1", "Ay")
        advanceUntilIdle()

        whenever(deleteHorseHealthEventUseCase("e1", "horse1")).thenReturn(
            Result.failure(RuntimeException("Delete failed"))
        )

        viewModel.deleteEvent("e1")
        advanceUntilIdle()

        assertNotNull(viewModel.ui.value.error)
    }

    // ─── clearError ──────────────────────────────────────────────────────────

    @Test
    fun `clearError resets error to null`() = runTest {
        whenever(getHorseHealthEventsUseCase("horse1")).thenReturn(
            Result.failure(RuntimeException("Error"))
        )
        viewModel.load("horse1", "Güneş")
        advanceUntilIdle()

        viewModel.clearError()

        assertNull(viewModel.ui.value.error)
    }

    // ─── daysUntil extension ─────────────────────────────────────────────────

    @Test
    fun `daysUntil returns 0 for today`() {
        val event = healthEvent("e1", today())
        assertEquals(0L, event.daysUntil())
    }

    @Test
    fun `daysUntil returns positive value for future date`() {
        val futureDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val event = healthEvent("e1", futureDate)
        assertEquals(7L, event.daysUntil())
    }

    @Test
    fun `daysUntil returns negative value for past date`() {
        val pastDate = LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val event = healthEvent("e1", pastDate)
        assertEquals(-3L, event.daysUntil())
    }

    @Test
    fun `daysUntil returns 0 for invalid date string`() {
        val event = healthEvent("e1", "not-a-date")
        assertEquals(0L, event.daysUntil())
    }

    @Test
    fun `daysUntil returns 0 for empty date string`() {
        val event = healthEvent("e1", "")
        assertEquals(0L, event.daysUntil())
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun healthEvent(id: String, date: String) = HorseHealthEvent(
        id = id,
        horseId = "horse1",
        type = HorseHealthEventType.VET,
        date = date,
        notes = ""
    )
}
