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

data class BarnInstructorFunctionsDto(
    val id: String,
    val name: String,
    val photoUrl: String = "",
    val specialty: String = "",
    val rating: Double = 0.0
)

data class BarnReviewFunctionsDto(
    val id: String,
    val authorName: String,
    val rating: Int = 5,
    val comment: String = "",
    val dateLabel: String = ""
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
    val reviewCount: Int,
    val heroImageUrl: String? = null,
    val capacity: Int = 0,
    val phone: String? = null,
    val instructors: List<BarnInstructorFunctionsDto> = emptyList(),
    val reviews: List<BarnReviewFunctionsDto> = emptyList()
)

data class LessonFunctionsDto(
    val id: String,
    val date: String,
    val title: String,
    val instructorName: String,
    val durationMin: Int,
    val level: String,
    val price: Double,
    val spotsTotal: Int = 0,
    val spotsAvailable: Int = 0,
    val isBookedByMe: Boolean = false
)

data class ReviewFunctionsDto(
    val id: String,
    val targetId: String,
    val targetType: String,
    val targetName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String = "",
    val authorName: String = ""
)

data class HorseFunctionsDto(
    val id: String,
    val name: String,
    val breed: String = "",
    val birthYear: Int = 0,
    val color: String = "",
    val gender: String = "",
    val weightKg: Int = 0,
    val imageUrl: String = ""
)

data class ReservationFunctionsDto(
    val id: String,
    val lessonId: String,
    val lessonTitle: String,
    val lessonDate: String,
    val instructorName: String,
    val status: String,
    val createdAt: String = ""
)

data class HorseTipFunctionsDto(
    val id: String,
    val title: String,
    val body: String,
    val category: String = "",
    val locale: String = "en"
)

data class BreedFunctionsDto(
    val id: String,
    val nameEn: String,
    val nameTr: String,
    val sortOrder: Int = 99
)

data class AppContentFunctionsDto(
    val locale: String,
    val homeHeroTitle: String? = null,
    val homeHeroSubtitle: String? = null,
    val offlineHelp: String? = null,
    val loginTitle: String? = null,
    val loginSubtitle: String? = null,
    val emailLoginTitle: String? = null,
    val emailLoginSubtitle: String? = null,
    val enrollTitle: String? = null,
    val enrollSubtitle: String? = null,
    val forgotPasswordSubtitle: String? = null,
    val onboardingHeroTitle: String? = null,
    val onboardingHeroSubtitle: String? = null,
    val onboardingHelpText: String? = null,
    val rideLiveTitle: String? = null,
    val rideLiveSubtitleIdle: String? = null,
    val rideLiveSubtitleActive: String? = null,
    val ridePermissionTitle: String? = null,
    val ridePermissionHint: String? = null,
    val rideGrantLocationCta: String? = null,
    val settingsThemeSubtitle: String? = null,
    val settingsLanguageSubtitle: String? = null,
    val settingsNotificationsSubtitle: String? = null,
    val settingsPrivacySubtitle: String? = null
)
