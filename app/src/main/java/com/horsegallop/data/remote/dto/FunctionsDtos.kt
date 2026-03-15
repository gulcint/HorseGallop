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

data class UserSettingsFunctionsDto(
    val themeMode: String = "SYSTEM",
    val language: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val weightUnit: String = "kg",
    val distanceUnit: String = "km"
)

data class HorseHealthEventFunctionsDto(
    val id: String,
    val horseId: String,
    val type: String,
    val date: String,
    val notes: String = "",
    val createdAt: String = ""
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

// ─── Safety DTOs ──────────────────────────────────────────────────────────────

data class SafetyContactFunctionsDto(
    val id: String = "",
    val name: String = "",
    val phone: String = ""
)

data class SafetySettingsFunctionsDto(
    val isEnabled: Boolean = false,
    val contacts: List<SafetyContactFunctionsDto> = emptyList(),
    val autoAlarmMinutes: Int = 5
)

data class HealthEventFunctionsDto(
    val id: String,
    val userId: String = "",
    val horseId: String,
    val horseName: String = "",
    val type: String,
    val scheduledDate: Long,
    val completedDate: Long? = null,
    val notes: String = "",
    val isCompleted: Boolean = false
)

data class EquestrianAnnouncementFunctionsDto(
    val id: String = "",
    val title: String = "",
    val summary: String = "",
    val publishedAtLabel: String = "",
    val detailUrl: String = "",
    val imageUrl: String? = null
)

data class EquestrianCompetitionFunctionsDto(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val dateLabel: String = "",
    val detailUrl: String = ""
)

data class FederatedBarnSyncStatusFunctionsDto(
    val status: String = "",
    val syncedAt: String = "",
    val itemCount: Int = 0,
    val errorMessage: String? = null
)

data class FederationManualSyncFunctionsDto(
    val syncedAt: String = "",
    val barnsCount: Int = 0,
    val announcementsCount: Int = 0,
    val competitionsCount: Int = 0,
    val throttled: Boolean = false
)

data class FederationSourceHealthFunctionsDto(
    val source: String = "",
    val status: String = "",
    val itemCount: Int = 0,
    val lastAttemptAt: String = "",
    val lastSuccessAt: String = "",
    val dataAgeMinutes: Int = -1,
    val isStale: Boolean = false,
    val errorMessage: String? = null
)

// ─── Barn Management DTOs ─────────────────────────────────────────────────────

data class BarnStatsDto(
    val totalLessons: Int = 0,
    val totalReservations: Int = 0,
    val uniqueStudents: Int = 0,
    val upcomingLessonsCount: Int = 0
)

data class ManagedLessonDto(
    val id: String,
    val title: String,
    val instructorName: String,
    val startTimeMs: Long,
    val durationMin: Int,
    val level: String,
    val price: Double,
    val spotsTotal: Int,
    val spotsBooked: Int,
    val barnId: String,
    val isCancelled: Boolean = false
)

data class StudentRosterEntryDto(
    val userId: String,
    val displayName: String,
    val email: String,
    val reservationId: String,
    val bookedAtMs: Long
)

// ─── AI Coach DTOs ────────────────────────────────────────────────────────────

data class AiCoachMessageDto(val role: String, val text: String)
data class AiCoachAnswerDto(val answer: String)

// ─── TBF Event DTOs ───────────────────────────────────────────────────────────

data class TbfVenueDto(
    val code: String,
    val name: String,
    val eventCount: Int,
    val time: String = ""
)

data class TbfEventDayDto(
    val date: String,
    val type: String,
    val venues: List<TbfVenueDto>
)

data class TbfAthleteDto(
    val no: String,
    val name: String,
    val jockey: String,
    val trainer: String,
    val owner: String,
    val weight: Int,
    val age: String,
    val last6: String,
    val odds: String,
    val bestTime: String,
    val result: String = "",
    val time: String = "",
    val gap: String = ""
)

data class TbfCompetitionDto(
    val no: String,
    val name: String,
    val distance: Int,
    val surface: String,
    val time: String,
    val prize: Long,
    val athletes: List<TbfAthleteDto>
)

data class TbfEventCardDto(
    val venue: String,
    val date: String,
    val type: String,
    val events: List<TbfCompetitionDto>,
    val weather: String = "",
    val trackCondition: String = ""
)

data class TbfUpcomingEventsDto(val days: List<TbfEventDayDto>)
