package com.horsegallop.data.remote.dto

data class HomeStatsFunctionsDto(
    val totalRides: Int,
    val totalDistanceKm: Double,
    val totalDurationMin: Int,
    val totalCalories: Double,
    val favoriteBarn: String?
)

data class HomeRecentActivityFunctionsDto(
    val id: String,
    val title: String,
    val dateLabel: String,
    val timeLabel: String,
    val durationMin: Int,
    val distanceKm: Double
)

data class HomeDashboardFunctionsDto(
    val stats: HomeStatsFunctionsDto,
    val recentActivities: List<HomeRecentActivityFunctionsDto>
)

data class BarnFunctionsDto(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val lat: Double,
    val lng: Double,
    val tags: List<String>,
    val amenities: List<String>,
    val rating: Double,
    val reviewCount: Int
)

data class LessonFunctionsDto(
    val id: String,
    val date: String,
    val title: String,
    val instructorName: String,
    val durationMin: Int,
    val level: String,
    val price: Double
)

data class AppContentFunctionsDto(
    val locale: String,
    val homeHeroTitle: String? = null,
    val homeHeroSubtitle: String? = null,
    val offlineHelp: String? = null
)
