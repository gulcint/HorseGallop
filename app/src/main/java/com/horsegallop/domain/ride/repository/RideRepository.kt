package com.horsegallop.domain.ride.repository

import com.horsegallop.domain.ride.model.RideMetrics
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    val isRiding: Flow<Boolean>
    val rideMetrics: Flow<RideMetrics>
    
    suspend fun startRide(weightKg: Float = 70f, rideType: String? = null)
    suspend fun stopRide(barnName: String? = null)
    suspend fun setAutoDetect(enabled: Boolean)
}
