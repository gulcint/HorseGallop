package com.horsegallop.domain.ride.model

data class RideSession(
    val id: String,
    val dateMillis: Long,
    val durationSec: Int,
    val distanceKm: Float,
    val calories: Int,
    val pathPoints: List<GeoPoint>,
    val barnName: String? = null,
    val avgSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val rideType: String? = null
)
