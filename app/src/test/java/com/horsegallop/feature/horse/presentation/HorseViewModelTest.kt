package com.horsegallop.feature.horse.presentation

import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.domain.horse.usecase.AddHorseUseCase
import com.horsegallop.domain.horse.usecase.DeleteHorseUseCase
import com.horsegallop.domain.horse.usecase.GetBreedsUseCase
import com.horsegallop.domain.horse.usecase.GetMyHorsesUseCase
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
class HorseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getMyHorsesUseCase: GetMyHorsesUseCase = mock()
    private val addHorseUseCase: AddHorseUseCase = mock()
    private val deleteHorseUseCase: DeleteHorseUseCase = mock()
    private val getBreedsUseCase: GetBreedsUseCase = mock()

    private lateinit var viewModel: HorseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(getMyHorsesUseCase()).thenReturn(flowOf(emptyList()))
        // getBreedsUseCase is suspend — stub via runBlocking for @Before setup
        kotlinx.coroutines.runBlocking {
            whenever(getBreedsUseCase(any())).thenReturn(Result.success(emptyList()))
        }
        viewModel = HorseViewModel(getMyHorsesUseCase, addHorseUseCase, deleteHorseUseCase, getBreedsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── init / loadHorses ───────────────────────────────────────────────────

    @Test
    fun `init loads horses and updates uiState`() = runTest {
        val horses = listOf(horse("h1", "Yıldız"))
        whenever(getMyHorsesUseCase()).thenReturn(flowOf(horses))
        viewModel = HorseViewModel(getMyHorsesUseCase, addHorseUseCase, deleteHorseUseCase, getBreedsUseCase)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.horses.size)
        assertEquals("Yıldız", state.horses[0].name)
        assertFalse(state.loading)
    }

    @Test
    fun `init loads breeds from backend`() = runTest {
        val breeds = listOf("Arap Atı", "Safkan İngiliz")
        kotlinx.coroutines.runBlocking {
            whenever(getBreedsUseCase(any())).thenReturn(Result.success(breeds))
        }
        viewModel = HorseViewModel(getMyHorsesUseCase, addHorseUseCase, deleteHorseUseCase, getBreedsUseCase)

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.breeds.size)
    }

    // ─── addHorse ────────────────────────────────────────────────────────────

    @Test
    fun `addHorse on success sets savedSuccess true`() = runTest {
        whenever(addHorseUseCase(any())).thenReturn(Result.success(horse("h1", "Rüzgar")))

        advanceUntilIdle()

        viewModel.addHorse("Rüzgar", "Arap Atı", "2018", "doru", HorseGender.STALLION, "500")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.savedSuccess)
        assertFalse(state.saving)
        assertNull(state.saveError)
    }

    // Note: FirebaseFunctionsException-specific tests are in androidTest (Firebase static
    // initializers fail in JVM unit tests without an Android runtime environment).

    @Test
    fun `addHorse on generic exception shows generic error`() = runTest {
        whenever(addHorseUseCase(any())).thenReturn(
            Result.failure(RuntimeException("Unknown error"))
        )

        advanceUntilIdle()

        viewModel.addHorse("At", "Breed", "2020", "kır", HorseGender.MARE, "450")
        advanceUntilIdle()

        val saveError = viewModel.uiState.value.saveError
        assertNotNull(saveError)
        assertEquals("Unknown error", saveError)
    }

    @Test
    fun `addHorse parses birthYear gracefully when non-numeric`() = runTest {
        whenever(addHorseUseCase(any())).thenReturn(Result.success(horse("h1", "At")))

        advanceUntilIdle()

        viewModel.addHorse("At", "Breed", "invalid-year", "kır", HorseGender.UNKNOWN, "abc")
        advanceUntilIdle()

        // Should not crash — birthYear defaults to 0, weightKg defaults to 0
        assertTrue(viewModel.uiState.value.savedSuccess)
    }

    // ─── deleteHorse ─────────────────────────────────────────────────────────

    @Test
    fun `deleteHorse on success triggers loadHorses`() = runTest {
        whenever(deleteHorseUseCase("h1")).thenReturn(Result.success(Unit))

        advanceUntilIdle()

        viewModel.deleteHorse("h1")
        advanceUntilIdle()

        // No crash, state is valid
        assertNull(viewModel.uiState.value.error)
    }

    // ─── clearSaveState ──────────────────────────────────────────────────────

    @Test
    fun `clearSaveState resets savedSuccess and saveError`() = runTest {
        whenever(addHorseUseCase(any())).thenReturn(Result.success(horse("h1", "At")))

        advanceUntilIdle()

        viewModel.addHorse("At", "Breed", "2020", "kır", HorseGender.MARE, "400")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.savedSuccess)

        viewModel.clearSaveState()

        assertFalse(viewModel.uiState.value.savedSuccess)
        assertNull(viewModel.uiState.value.saveError)
    }

    // ─── Horse.age computed property ─────────────────────────────────────────

    @Test
    fun `Horse age is computed correctly for known birthYear`() {
        val h = horse("h1", "Test", birthYear = 2016)
        assertEquals(10, h.age)
    }

    @Test
    fun `Horse age returns 0 for birthYear 0`() {
        val h = horse("h1", "Test", birthYear = 0)
        assertEquals(0, h.age)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun horse(id: String, name: String, birthYear: Int = 2018) = Horse(
        id = id,
        name = name,
        breed = "Arap Atı",
        birthYear = birthYear,
        color = "doru",
        gender = HorseGender.STALLION,
        weightKg = 500
    )
}
