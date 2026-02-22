package com.horsegallop.data.remote.dto

data class UserStatsDto(
    val userId: String,
    val totalRides: Int,
    val totalDistanceKm: Double,
    val totalDurationMinutes: Long,
    val totalCalories: Double,
    val favoriteBarnName: String?,
    val lastRideDate: String? // ISO 8601
)

data class UpdateProfileRequestDto(
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val weightKg: Float?
)

data class UserProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val weightKg: Float?,
    val role: String
)
