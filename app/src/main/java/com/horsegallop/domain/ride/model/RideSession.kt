package com.horsegallop.domain.ride.model

data class RideSession(
    val id: String,
    val dateMillis: Long,
    val durationSec: Int,
    val distanceKm: Float,
    val calories: Int,
    val pathPoints: List<GeoPoint>,
    val barnName: String? = null
)
