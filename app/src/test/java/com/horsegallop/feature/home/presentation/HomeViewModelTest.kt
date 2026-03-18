package com.horsegallop.feature.home.presentation

import com.horsegallop.domain.auth.usecase.GetCurrentUserIdUseCase
import com.horsegallop.domain.content.usecase.GetAppContentUseCase
import com.horsegallop.domain.home.model.RideSession
import com.horsegallop.domain.home.model.UserStats
import com.horsegallop.domain.home.usecase.GetRecentActivitiesUseCase
import com.horsegallop.domain.home.usecase.GetUserStatsUseCase
import com.horsegallop.domain.horse.usecase.GetHorseTipsUseCase
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase = mock()
    private val getRecentActivitiesUseCase: GetRecentActivitiesUseCase = mock()
    private val getUserStatsUseCase: GetUserStatsUseCase = mock()
    private val getAppContentUseCase: GetAppContentUseCase = mock()
    private val getHorseTipsUseCase: GetHorseTipsUseCase = mock()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Reset mocks to clear stubs from previous tests
        org.mockito.Mockito.reset(
            getCurrentUserIdUseCase, getRecentActivitiesUseCase,
            getUserStatsUseCase, getAppContentUseCase, getHorseTipsUseCase
        )
        // Default stubs — no user session
        whenever(getCurrentUserIdUseCase()).thenReturn(null)
        whenever(getAppContentUseCase(any())).thenReturn(flowOf())
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(flowOf())
        whenever(getUserStatsUseCase(any())).thenReturn(flowOf())
        kotlinx.coroutines.runBlocking {
            whenever(getHorseTipsUseCase(any())).thenReturn(Result.success(emptyList()))
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = HomeViewModel(
        getCurrentUserIdUseCase,
        getRecentActivitiesUseCase,
        getUserStatsUseCase,
        getAppContentUseCase,
        getHorseTipsUseCase
    )

    // ─── refresh — no user ───────────────────────────────────────────────────

    @Test
    fun `refresh with null userId sets error and isEmpty true`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn(null)
        viewModel = buildViewModel()

        advanceUntilIdle()

        val state = viewModel.ui.value
        assertFalse(state.loading)
        assertNotNull(state.error)
        assertTrue(state.isEmpty)
    }

    // ─── refresh — with user ─────────────────────────────────────────────────

    @Test
    fun `refresh with valid userId loads activities and sets isEmpty false`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn("uid1")
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(
            flowOf(Result.success(listOf(rideSession("r1"))))
        )
        whenever(getUserStatsUseCase(any())).thenReturn(
            flowOf(Result.success(stats()))
        )

        viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertFalse(state.loading)
        assertFalse(state.isEmpty)
        assertEquals(1, state.activities.size)
    }

    @Test
    fun `refresh with empty activities sets isEmpty true`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn("uid1")
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(
            flowOf(Result.success(emptyList()))
        )
        whenever(getUserStatsUseCase(any())).thenReturn(
            flowOf(Result.success(stats()))
        )

        viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.ui.value.isEmpty)
    }

    @Test
    fun `refresh with failed activities sets isEmpty true without error card`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn("uid1")
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(
            flowOf(Result.failure(RuntimeException("Backend unavailable")))
        )
        whenever(getUserStatsUseCase(any())).thenReturn(
            flowOf(Result.success(stats()))
        )

        viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.ui.value
        assertTrue(state.isEmpty)
        assertNull(state.error)
    }

    // ─── stats ───────────────────────────────────────────────────────────────

    @Test
    fun `refresh with stats updates totalRides and totalDistance`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn("uid1")
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(
            flowOf(Result.success(emptyList()))
        )
        whenever(getUserStatsUseCase(any())).thenReturn(
            flowOf(Result.success(stats(totalRides = 7, totalDistance = 42.5)))
        )

        viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("7", viewModel.ui.value.totalRides)
        assertEquals("42.5", viewModel.ui.value.totalDistance)
    }

    @Test
    fun `refresh with stats formats totalDuration correctly`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn("uid1")
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(
            flowOf(Result.success(emptyList()))
        )
        whenever(getUserStatsUseCase(any())).thenReturn(
            flowOf(Result.success(stats(totalDurationMin = 135)))
        )

        viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("2h 15m", viewModel.ui.value.totalDuration)
    }

    // ─── activityDistribution ─────────────────────────────────────────────────

    @Test
    fun `activities with same type are grouped in distribution`() = runTest {
        whenever(getCurrentUserIdUseCase()).thenReturn("uid1")
        whenever(getRecentActivitiesUseCase(any(), any())).thenReturn(
            flowOf(Result.success(listOf(
                rideSession("r1", "Dressaj"),
                rideSession("r2", "Dressaj"),
                rideSession("r3", "Atlama")
            )))
        )
        whenever(getUserStatsUseCase(any())).thenReturn(flowOf(Result.success(stats())))

        viewModel = buildViewModel()
        advanceUntilIdle()

        val dist = viewModel.ui.value.activityDistribution
        val dressajEntry = dist.find { it.first == "Dressaj" }
        assertNotNull(dressajEntry)
        assertEquals(2f / 3f, dressajEntry!!.second, 0.01f)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun rideSession(id: String, title: String = "Test") = RideSession(
        id = id, title = title, timestamp = null, durationMin = 30, distanceKm = 5.0
    )

    private fun stats(
        totalRides: Int = 0,
        totalDistance: Double = 0.0,
        totalDurationMin: Int = 0
    ) = UserStats(
        totalRides = totalRides,
        totalDistance = totalDistance,
        totalDurationMin = totalDurationMin
    )
}
