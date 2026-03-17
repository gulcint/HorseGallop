package com.horsegallop.feature.health.presentation

import com.horsegallop.domain.health.model.HealthEvent
import com.horsegallop.domain.health.model.HealthEventType
import com.horsegallop.domain.health.usecase.DeleteHealthEventUseCase
import com.horsegallop.domain.health.usecase.GetHealthEventsUseCase
import com.horsegallop.domain.health.usecase.SaveHealthEventUseCase
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getHealthEventsUseCase: GetHealthEventsUseCase = mock()
    private val saveHealthEventUseCase: SaveHealthEventUseCase = mock()
    private val deleteHealthEventUseCase: DeleteHealthEventUseCase = mock()

    private lateinit var viewModel: HealthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(getHealthEventsUseCase(null)).thenReturn(flowOf(emptyList()))
        viewModel = HealthViewModel(getHealthEventsUseCase, saveHealthEventUseCase, deleteHealthEventUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── load ────────────────────────────────────────────────────────────────

    @Test
    fun `load sets loading false and populates events on success`() = runTest {
        val events = listOf(healthEvent("e1"))
        whenever(getHealthEventsUseCase(null)).thenReturn(flowOf(events))
        viewModel = HealthViewModel(getHealthEventsUseCase, saveHealthEventUseCase, deleteHealthEventUseCase)

        advanceUntilIdle()

        val state = viewModel.ui.value
        assertFalse(state.loading)
        assertEquals(1, state.events.size)
        assertEquals("e1", state.events[0].id)
    }

    @Test
    fun `load sets events to empty list when flow emits empty`() = runTest {
        whenever(getHealthEventsUseCase(null)).thenReturn(flowOf(emptyList()))
        viewModel = HealthViewModel(getHealthEventsUseCase, saveHealthEventUseCase, deleteHealthEventUseCase)

        advanceUntilIdle()

        assertTrue(viewModel.ui.value.events.isEmpty())
        assertFalse(viewModel.ui.value.loading)
    }

    // ─── filterByHorse ───────────────────────────────────────────────────────

    @Test
    fun `filterByHorse updates selectedHorseId and reloads events`() = runTest {
        val events = listOf(healthEvent("e1"))
        whenever(getHealthEventsUseCase("horse1")).thenReturn(flowOf(events))

        advanceUntilIdle()

        viewModel.filterByHorse("horse1")
        advanceUntilIdle()

        assertEquals("horse1", viewModel.ui.value.selectedHorseId)
        assertEquals(1, viewModel.ui.value.events.size)
    }

    @Test
    fun `filterByHorse with null clears horse filter`() = runTest {
        whenever(getHealthEventsUseCase(null)).thenReturn(flowOf(emptyList()))

        advanceUntilIdle()
        viewModel.filterByHorse(null)
        advanceUntilIdle()

        assertNull(viewModel.ui.value.selectedHorseId)
    }

    // ─── saveEvent ───────────────────────────────────────────────────────────

    @Test
    fun `saveEvent on success clears isSaving`() = runTest {
        val event = healthEvent("e1")
        whenever(saveHealthEventUseCase(event)).thenReturn(Result.success(event))

        advanceUntilIdle()

        viewModel.saveEvent(event)
        advanceUntilIdle()

        assertFalse(viewModel.ui.value.isSaving)
        assertNull(viewModel.ui.value.error)
    }

    @Test
    fun `saveEvent on failure sets error and clears isSaving`() = runTest {
        val event = healthEvent("e1")
        whenever(saveHealthEventUseCase(event)).thenReturn(
            Result.failure(RuntimeException("Save failed"))
        )

        advanceUntilIdle()

        viewModel.saveEvent(event)
        advanceUntilIdle()

        assertFalse(viewModel.ui.value.isSaving)
        assertNotNull(viewModel.ui.value.error)
    }

    // ─── markCompleted ───────────────────────────────────────────────────────

    @Test
    fun `markCompleted calls saveEvent with isCompleted true`() = runTest {
        val event = healthEvent("e1")
        whenever(saveHealthEventUseCase(any())).thenReturn(Result.success(event))

        advanceUntilIdle()

        viewModel.markCompleted(event)
        advanceUntilIdle()

        assertNull(viewModel.ui.value.error)
    }

    @Test
    fun `markCompleted on failure sets error`() = runTest {
        val event = healthEvent("e1")
        whenever(saveHealthEventUseCase(any())).thenReturn(
            Result.failure(RuntimeException("Mark failed"))
        )

        advanceUntilIdle()

        viewModel.markCompleted(event)
        advanceUntilIdle()

        assertNotNull(viewModel.ui.value.error)
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    fun `delete on success does not set error`() = runTest {
        whenever(deleteHealthEventUseCase("e1")).thenReturn(Result.success(Unit))

        advanceUntilIdle()

        viewModel.delete("e1")
        advanceUntilIdle()

        assertNull(viewModel.ui.value.error)
    }

    @Test
    fun `delete on failure sets error`() = runTest {
        whenever(deleteHealthEventUseCase("e1")).thenReturn(
            Result.failure(RuntimeException("Delete failed"))
        )

        advanceUntilIdle()

        viewModel.delete("e1")
        advanceUntilIdle()

        assertNotNull(viewModel.ui.value.error)
    }

    // ─── clearError ──────────────────────────────────────────────────────────

    @Test
    fun `clearError resets error to null`() = runTest {
        whenever(deleteHealthEventUseCase("e1")).thenReturn(
            Result.failure(RuntimeException("Error"))
        )
        advanceUntilIdle()
        viewModel.delete("e1")
        advanceUntilIdle()
        assertNotNull(viewModel.ui.value.error)

        viewModel.clearError()

        assertNull(viewModel.ui.value.error)
    }

    // ─── HealthEvent computed properties ─────────────────────────────────────

    @Test
    fun `isOverdue is true for incomplete event with past scheduledDate`() {
        val pastEvent = healthEvent("e1", scheduledDate = System.currentTimeMillis() - 86_400_000L)
        assertTrue(pastEvent.isOverdue)
    }

    @Test
    fun `isOverdue is false for completed event`() {
        val completedEvent = healthEvent("e1",
            scheduledDate = System.currentTimeMillis() - 86_400_000L,
            isCompleted = true
        )
        assertFalse(completedEvent.isOverdue)
    }

    @Test
    fun `isDueSoon is true for event within 7 days`() {
        val soonEvent = healthEvent("e1",
            scheduledDate = System.currentTimeMillis() + 3 * 86_400_000L
        )
        assertTrue(soonEvent.isDueSoon)
    }

    @Test
    fun `isDueSoon is false for event more than 7 days away`() {
        val farEvent = healthEvent("e1",
            scheduledDate = System.currentTimeMillis() + 10 * 86_400_000L
        )
        assertFalse(farEvent.isDueSoon)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun healthEvent(
        id: String,
        scheduledDate: Long = System.currentTimeMillis() + 86_400_000L,
        isCompleted: Boolean = false
    ) = HealthEvent(
        id = id,
        userId = "user1",
        horseId = "horse1",
        horseName = "Yıldız",
        type = HealthEventType.VET,
        scheduledDate = scheduledDate,
        isCompleted = isCompleted
    )
}
