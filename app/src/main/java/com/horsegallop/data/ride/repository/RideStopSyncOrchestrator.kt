package com.horsegallop.data.ride.repository

import com.horsegallop.data.remote.dto.StopRideRequestDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

data class StopSyncAttemptResult(
    val remoteSynced: Boolean,
    val pendingSyncId: String? = null
)

@Singleton
class RideStopSyncOrchestrator @Inject constructor(
    private val outboxStore: RideSyncOutboxStore
) {

    val pendingSyncCount = outboxStore.pendingCount

    suspend fun syncStopOrQueue(
        rideId: String?,
        request: StopRideRequestDto,
        stopRemote: suspend (String, StopRideRequestDto) -> Unit
    ): StopSyncAttemptResult {
        if (rideId.isNullOrBlank()) {
            return StopSyncAttemptResult(remoteSynced = false)
        }

        return try {
            stopRemote(rideId, request)
            StopSyncAttemptResult(remoteSynced = true)
        } catch (e: Exception) {
            val now = System.currentTimeMillis()
            val pending = PendingRideStopSync(
                id = java.util.UUID.randomUUID().toString(),
                rideId = rideId,
                request = request,
                retryCount = 0,
                nextRetryAtMillis = now + retryDelayMillis(0),
                createdAtMillis = now,
                lastErrorMessage = e.message
            )
            outboxStore.append(pending)
            StopSyncAttemptResult(
                remoteSynced = false,
                pendingSyncId = pending.id
            )
        }
    }

    suspend fun retryDuePendingSync(stopRemote: suspend (String, StopRideRequestDto) -> Unit) {
        val now = System.currentTimeMillis()
        outboxStore.purgeExpired(now, MAX_AGE_MILLIS)
        val dueItems = outboxStore.listDue(now)
        dueItems.forEach { pending ->
            try {
                stopRemote(pending.rideId, pending.request)
                outboxStore.markSynced(pending.id)
            } catch (e: Exception) {
                val nextRetryCount = pending.retryCount + 1
                val nextRetryAt = now + retryDelayMillis(nextRetryCount)
                outboxStore.rescheduleFailed(
                    outboxId = pending.id,
                    retryCount = nextRetryCount,
                    nextRetryAtMillis = nextRetryAt,
                    errorMessage = e.message
                )
            }
        }
    }

    internal fun retryDelayMillis(retryCount: Int): Long {
        val safeRetry = min(retryCount, BACKOFF_SECONDS.lastIndex)
        return BACKOFF_SECONDS[safeRetry] * 1000L
    }

    private companion object {
        val BACKOFF_SECONDS = listOf(30L, 120L, 600L, 1800L, 3600L)
        const val MAX_AGE_MILLIS = 24 * 60 * 60 * 1000L
    }
}
