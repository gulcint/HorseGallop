package com.horsegallop.domain.ride.model

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float = 0f,
    val altitudeM: Float = 0f
)
