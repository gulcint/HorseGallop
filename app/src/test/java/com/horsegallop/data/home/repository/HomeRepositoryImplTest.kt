package com.horsegallop.data.home.repository

import com.horsegallop.data.remote.dto.HomeDashboardFunctionsDto
import com.horsegallop.data.remote.dto.HomeRecentActivityFunctionsDto
import com.horsegallop.data.remote.dto.HomeStatsFunctionsDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HomeRepositoryImplTest {

    private val dataSource: AppFunctionsDataSource = mock()
    private lateinit var repository: HomeRepositoryImpl

    @Before
    fun setUp() {
        repository = HomeRepositoryImpl(dataSource)
    }

    // ─── getRecentActivities ─────────────────────────────────────────────────

    @Test
    fun `getRecentActivities emits success with mapped RideSessions`() = runTest {
        whenever(dataSource.getHomeDashboard(5)).thenReturn(
            dashboardDto(
                activities = listOf(
                    activityDto("r1", "Sabah Turu", "2026-04-01", "08:30", 45, 6.2)
                )
            )
        )

        val result = repository.getRecentActivities("uid1", 5).toList().first()

        assertTrue(result.isSuccess)
        val sessions = result.getOrNull()!!
        assertEquals(1, sessions.size)
        assertEquals("r1", sessions[0].id)
        assertEquals("Sabah Turu", sessions[0].title)
        assertEquals(45, sessions[0].durationMin)
        assertEquals(6.2, sessions[0].distanceKm, 0.01)
    }

    @Test
    fun `getRecentActivities parses valid dateLabel and timeLabel into timestamp`() = runTest {
        whenever(dataSource.getHomeDashboard(5)).thenReturn(
            dashboardDto(
                activities = listOf(activityDto("r1", "Test", "2026-04-01", "09:00", 30, 5.0))
            )
        )

        val session = repository.getRecentActivities("uid1", 5).toList().first().getOrNull()!!.first()

        assertNotNull(session.timestamp)
    }

    @Test
    fun `getRecentActivities returns null timestamp for invalid date string`() = runTest {
        whenever(dataSource.getHomeDashboard(5)).thenReturn(
            dashboardDto(
                activities = listOf(activityDto("r1", "Test", "invalid-date", "", 30, 5.0))
            )
        )

        val session = repository.getRecentActivities("uid1", 5).toList().first().getOrNull()!!.first()

        assertNull(session.timestamp)
    }

    @Test
    fun `getRecentActivities emits failure when dataSource throws`() = runTest {
        whenever(dataSource.getHomeDashboard(5)).thenThrow(RuntimeException("Network error"))

        val result = repository.getRecentActivities("uid1", 5).toList().first()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getRecentActivities emits empty list when dashboard has no activities`() = runTest {
        whenever(dataSource.getHomeDashboard(5)).thenReturn(dashboardDto(activities = emptyList()))

        val result = repository.getRecentActivities("uid1", 5).toList().first()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    // ─── getUserStats ────────────────────────────────────────────────────────

    @Test
    fun `getUserStats emits success with mapped UserStats`() = runTest {
        whenever(dataSource.getHomeDashboard(20)).thenReturn(
            dashboardDto(
                stats = statsDto(totalRides = 10, totalDistanceKm = 85.5, totalDurationMin = 600, totalCalories = 3200.0, favoriteBarn = "Yıldız Ahırı")
            )
        )

        val result = repository.getUserStats("uid1").toList().first()

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(10, stats.totalRides)
        assertEquals(85.5, stats.totalDistance, 0.01)
        assertEquals(600, stats.totalDurationMin)
        assertEquals(3200.0, stats.totalCalories, 0.01)
        assertEquals("Yıldız Ahırı", stats.favoriteBarn)
    }

    @Test
    fun `getUserStats sets lastRideAt from first activity`() = runTest {
        whenever(dataSource.getHomeDashboard(20)).thenReturn(
            dashboardDto(
                activities = listOf(activityDto("r1", "Ride", "2026-04-01", "08:00", 30, 5.0))
            )
        )

        val result = repository.getUserStats("uid1").toList().first()

        assertNotNull(result.getOrNull()?.lastRideAt)
    }

    @Test
    fun `getUserStats sets lastRideAt null when no activities`() = runTest {
        whenever(dataSource.getHomeDashboard(20)).thenReturn(
            dashboardDto(activities = emptyList())
        )

        val result = repository.getUserStats("uid1").toList().first()

        assertNull(result.getOrNull()?.lastRideAt)
    }

    @Test
    fun `getUserStats emits failure when dataSource throws`() = runTest {
        whenever(dataSource.getHomeDashboard(20)).thenThrow(RuntimeException("error"))

        val result = repository.getUserStats("uid1").toList().first()

        assertTrue(result.isFailure)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun statsDto(
        totalRides: Int = 0,
        totalDistanceKm: Double = 0.0,
        totalDurationMin: Int = 0,
        totalCalories: Double = 0.0,
        favoriteBarn: String? = null
    ) = HomeStatsFunctionsDto(totalRides, totalDistanceKm, totalDurationMin, totalCalories, favoriteBarn)

    private fun activityDto(
        id: String, title: String,
        dateLabel: String, timeLabel: String,
        durationMin: Int, distanceKm: Double
    ) = HomeRecentActivityFunctionsDto(id, title, dateLabel, timeLabel, durationMin, distanceKm)

    private fun dashboardDto(
        stats: HomeStatsFunctionsDto = statsDto(),
        activities: List<HomeRecentActivityFunctionsDto> = emptyList()
    ) = HomeDashboardFunctionsDto(stats = stats, recentActivities = activities)
}
