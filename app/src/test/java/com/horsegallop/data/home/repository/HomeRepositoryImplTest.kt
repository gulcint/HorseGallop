package com.horsegallop.data.home.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseRideDto
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

    private val dataSource: SupabaseDataSource = mock()
    private lateinit var repository: HomeRepositoryImpl

    @Before
    fun setUp() {
        repository = HomeRepositoryImpl(dataSource)
    }

    // ─── getRecentActivities ─────────────────────────────────────────────────

    @Test
    fun `getRecentActivities emits success with mapped RideSessions`() = runTest {
        whenever(dataSource.getRecentRides(5)).thenReturn(
            listOf(
                rideDto("r1", barnName = "Sabah Turu", startedAt = "2026-04-01T08:30:00", durationSec = 2700, distanceKm = 6.2)
            )
        )

        val result = repository.getRecentActivities("uid1", 5).toList().first()

        assertTrue(result.isSuccess)
        val sessions = result.getOrNull()!!
        assertEquals(1, sessions.size)
        assertEquals("r1", sessions[0].id)
        assertEquals(45, sessions[0].durationMin)
        assertEquals(6.2, sessions[0].distanceKm, 0.01)
    }

    @Test
    fun `getRecentActivities parses valid ISO timestamp`() = runTest {
        whenever(dataSource.getRecentRides(5)).thenReturn(
            listOf(rideDto("r1", startedAt = "2026-04-01T09:00:00", durationSec = 1800, distanceKm = 5.0))
        )

        val session = repository.getRecentActivities("uid1", 5).toList().first().getOrNull()!!.first()

        assertNotNull(session.timestamp)
    }

    @Test
    fun `getRecentActivities returns null timestamp for invalid date string`() = runTest {
        whenever(dataSource.getRecentRides(5)).thenReturn(
            listOf(rideDto("r1", startedAt = "invalid-date", durationSec = 1800, distanceKm = 5.0))
        )

        val session = repository.getRecentActivities("uid1", 5).toList().first().getOrNull()!!.first()

        assertNull(session.timestamp)
    }

    @Test
    fun `getRecentActivities emits failure when dataSource throws`() = runTest {
        whenever(dataSource.getRecentRides(5)).thenThrow(RuntimeException("Network error"))

        val result = repository.getRecentActivities("uid1", 5).toList().first()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getRecentActivities emits empty list when no rides`() = runTest {
        whenever(dataSource.getRecentRides(5)).thenReturn(emptyList())

        val result = repository.getRecentActivities("uid1", 5).toList().first()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    // ─── getUserStats ────────────────────────────────────────────────────────

    @Test
    fun `getUserStats emits success with aggregated stats`() = runTest {
        whenever(dataSource.getMyRides()).thenReturn(
            listOf(
                rideDto("r1", durationSec = 3600, distanceKm = 10.0, calories = 500.0, barnName = "Yıldız Ahırı", startedAt = "2026-04-01T08:00:00"),
                rideDto("r2", durationSec = 1800, distanceKm = 5.0, calories = 250.0, barnName = "Yıldız Ahırı", startedAt = "2026-04-02T09:00:00")
            )
        )

        val result = repository.getUserStats("uid1").toList().first()

        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(2, stats.totalRides)
        assertEquals(15.0, stats.totalDistance, 0.01)
        assertEquals(90, stats.totalDurationMin)
        assertEquals(750.0, stats.totalCalories, 0.01)
        assertEquals("Yıldız Ahırı", stats.favoriteBarn)
    }

    @Test
    fun `getUserStats sets lastRideAt from most recent ride`() = runTest {
        whenever(dataSource.getMyRides()).thenReturn(
            listOf(rideDto("r1", startedAt = "2026-04-01T08:00:00"))
        )

        val result = repository.getUserStats("uid1").toList().first()

        assertNotNull(result.getOrNull()?.lastRideAt)
    }

    @Test
    fun `getUserStats sets lastRideAt null when no rides`() = runTest {
        whenever(dataSource.getMyRides()).thenReturn(emptyList())

        val result = repository.getUserStats("uid1").toList().first()

        assertNull(result.getOrNull()?.lastRideAt)
    }

    @Test
    fun `getUserStats emits failure when dataSource throws`() = runTest {
        whenever(dataSource.getMyRides()).thenThrow(RuntimeException("error"))

        val result = repository.getUserStats("uid1").toList().first()

        assertTrue(result.isFailure)
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun rideDto(
        id: String,
        barnName: String? = null,
        startedAt: String = "2026-04-01T08:00:00",
        durationSec: Int = 1800,
        distanceKm: Double = 5.0,
        calories: Double = 200.0
    ) = SupabaseRideDto(
        id = id,
        userId = "uid1",
        durationSec = durationSec,
        distanceKm = distanceKm,
        calories = calories,
        avgSpeedKmh = 10.0,
        barnName = barnName,
        startedAt = startedAt,
        savedAt = startedAt
    )
}
