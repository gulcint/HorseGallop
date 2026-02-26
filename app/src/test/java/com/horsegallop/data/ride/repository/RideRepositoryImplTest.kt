package com.horsegallop.data.ride.repository

import com.horsegallop.data.remote.dto.StopRideRequestDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class RideRepositoryImplTest {

    @Test
    fun stopSyncFailure_appendsOutboxItem() = runTest {
        val (store, orchestrator) = buildStoreAndOrchestrator()

        val result = orchestrator.syncStopOrQueue(
            rideId = "ride-1",
            request = sampleRequest(),
            stopRemote = { _, _ -> throw IOException("network") }
        )

        assertFalse(result.remoteSynced)
        assertNotNull(result.pendingSyncId)
        assertEquals(1, store.snapshot().size)
        assertEquals(1, store.pendingCount.value)
    }

    @Test
    fun retrySuccess_removesOutboxItem() = runTest {
        val (store, orchestrator) = buildStoreAndOrchestrator()
        var shouldFail = true

        orchestrator.syncStopOrQueue(
            rideId = "ride-1",
            request = sampleRequest(),
            stopRemote = { _, _ ->
                if (shouldFail) throw IOException("network")
            }
        )

        shouldFail = false
        store.snapshot().forEach { pending ->
            store.rescheduleFailed(
                outboxId = pending.id,
                retryCount = pending.retryCount,
                nextRetryAtMillis = 0L,
                errorMessage = pending.lastErrorMessage
            )
        }

        orchestrator.retryDuePendingSync { _, _ -> Unit }

        assertTrue(store.snapshot().isEmpty())
        assertEquals(0, store.pendingCount.value)
    }

    @Test
    fun retryBackoff_followsConfiguredSteps() {
        val (_, orchestrator) = buildStoreAndOrchestrator()

        assertEquals(30_000L, orchestrator.retryDelayMillis(0))
        assertEquals(120_000L, orchestrator.retryDelayMillis(1))
        assertEquals(600_000L, orchestrator.retryDelayMillis(2))
        assertEquals(1_800_000L, orchestrator.retryDelayMillis(3))
        assertEquals(3_600_000L, orchestrator.retryDelayMillis(4))
        assertEquals(3_600_000L, orchestrator.retryDelayMillis(40))
    }

    @Test
    fun retryDuePendingSync_onlyProcessesDueItems() = runTest {
        val (store, orchestrator) = buildStoreAndOrchestrator()
        val now = System.currentTimeMillis()
        val due = pendingEntry(rideId = "ride-due", nextRetryAtMillis = now - 1_000L)
        val notDue = pendingEntry(rideId = "ride-not-due", nextRetryAtMillis = now + 60_000L)
        store.append(due)
        store.append(notDue)

        var called = 0
        orchestrator.retryDuePendingSync { _, _ -> called += 1 }

        assertEquals(1, called)
        val remainingRideIds = store.snapshot().map { it.rideId }
        assertEquals(listOf("ride-not-due"), remainingRideIds)
    }

    @Test
    fun purgeExpired_removesItemsOlderThan24Hours() = runTest {
        val (store, orchestrator) = buildStoreAndOrchestrator()
        val now = System.currentTimeMillis()
        val maxAgeMillis = 24 * 60 * 60 * 1000L
        val expired = pendingEntry(
            rideId = "ride-expired",
            createdAtMillis = now - maxAgeMillis - 1L,
            nextRetryAtMillis = now - 1_000L
        )
        val fresh = pendingEntry(
            rideId = "ride-fresh",
            createdAtMillis = now - 10_000L,
            nextRetryAtMillis = now - 1_000L
        )
        store.append(expired)
        store.append(fresh)

        orchestrator.retryDuePendingSync { _, _ -> throw IOException("network") }

        val remainingRideIds = store.snapshot().map { it.rideId }
        assertEquals(listOf("ride-fresh"), remainingRideIds)
    }

    private fun buildStoreAndOrchestrator(): Pair<RideSyncOutboxStore, RideStopSyncOrchestrator> {
        val tempFile = File.createTempFile("ride_sync_outbox_test", ".json").apply {
            writeText("[]")
            deleteOnExit()
        }
        val store = RideSyncOutboxStore(tempFile)
        val orchestrator = RideStopSyncOrchestrator(store)
        return store to orchestrator
    }

    private fun sampleRequest(): StopRideRequestDto = StopRideRequestDto(
        distanceKm = 2.4,
        durationMin = 12.0,
        calories = 180.0,
        avgSpeedKmh = 12.0,
        maxSpeedKmh = 18.0,
        pathPoints = emptyList()
    )

    private fun pendingEntry(
        rideId: String,
        createdAtMillis: Long = System.currentTimeMillis(),
        nextRetryAtMillis: Long = createdAtMillis
    ): PendingRideStopSync = PendingRideStopSync(
        id = UUID.randomUUID().toString(),
        rideId = rideId,
        request = sampleRequest(),
        retryCount = 0,
        nextRetryAtMillis = nextRetryAtMillis,
        createdAtMillis = createdAtMillis,
        lastErrorMessage = null
    )
}
