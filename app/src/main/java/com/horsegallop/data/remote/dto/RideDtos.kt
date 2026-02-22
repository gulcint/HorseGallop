package com.horsegallop.data.remote.dto

data class RideSessionDto(
    val id: String,
    val userId: String,
    val barnId: String?,
    val barnName: String?,
    val startTime: String, // ISO 8601
    val endTime: String?, // ISO 8601
    val durationSeconds: Long,
    val distanceKm: Double,
    val calories: Double,
    val maxSpeedKmh: Double?,
    val avgSpeedKmh: Double?,
    val pathPoints: List<GeoPointDto>?
)

data class GeoPointDto(
    val lat: Double,
    val lng: Double,
    val timestamp: Long?
)

data class CreateRideRequestDto(
    val barnId: String?,
    val startTime: Long, // Epoch millis
    val endTime: Long, // Epoch millis
    val durationSeconds: Long,
    val distanceKm: Double,
    val calories: Double,
    val pathPoints: List<GeoPointDto>
)

data class StartRideRequestDto(
    val rideType: String? = null,
    val startLocation: GeoPointDto? = null
)

data class StartRideResponseDto(
    val ok: Boolean,
    val id: String
)

data class StopRideRequestDto(
    val distanceKm: Double?,
    val durationMin: Double?,
    val calories: Double?,
    val avgSpeedKmh: Double?,
    val maxSpeedKmh: Double?,
    val pathPoints: List<GeoPointDto>?
)

data class StopRideResponseDto(
    val ok: Boolean
)
