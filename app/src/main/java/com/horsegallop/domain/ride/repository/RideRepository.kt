package com.horsegallop.domain.ride.repository

import com.horsegallop.domain.ride.model.RideMetrics
import com.horsegallop.domain.ride.model.StopRideResult
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    val isRiding: Flow<Boolean>
    val rideMetrics: Flow<RideMetrics>
    val pendingSyncCount: Flow<Int>
    /** Emits when 5+ minutes of stillness detected while auto-detect is ON. */
    val autoStopSignal: Flow<Unit>

    suspend fun startRide(weightKg: Float = 70f, rideType: String? = null)
    suspend fun stopRide(barnName: String? = null): StopRideResult
    suspend fun retryPendingRideSync()
    suspend fun setAutoDetect(enabled: Boolean)
}
