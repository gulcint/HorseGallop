package com.horsegallop.data.ride.repository

import com.horsegallop.data.remote.dto.StopRideRequestDto

data class PendingRideStopSync(
    val id: String,
    val rideId: String,
    val request: StopRideRequestDto,
    val retryCount: Int,
    val nextRetryAtMillis: Long,
    val createdAtMillis: Long,
    val lastErrorMessage: String? = null
)
