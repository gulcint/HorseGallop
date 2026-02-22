package com.horsegallop.data.remote.dto

data class BackendBarnDto(
    val id: String,
    val name: String,
    val description: String?,
    val location: String?,
    val lat: Double?,
    val lng: Double?,
    val tags: List<String>?,
    val amenities: List<String>?,
    val rating: Double?,
    val reviewCount: Int?
)

data class BackendLessonDto(
    val id: String,
    val date: String,
    val title: String,
    val instructorName: String,
    val durationMin: Int?,
    val level: String?,
    val price: Double?
)

data class BackendRideDto(
    val id: String,
    val startedAt: Map<String, Any>?,
    val distanceKm: Double?,
    val durationMin: Double?,
    val calories: Double?,
    val status: String?,
    val barnName: String?
)
