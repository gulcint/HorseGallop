package com.horsegallop.domain.ride.model

data class RideMetrics(
    val speedKmh: Float = 0f,
    val distanceKm: Float = 0f,
    val durationSec: Int = 0,
    val calories: Int = 0,
    val horseCalories: Int = 0,
    val altitudeM: Float = 0f,
    val pathPoints: List<GeoPoint> = emptyList()
)
