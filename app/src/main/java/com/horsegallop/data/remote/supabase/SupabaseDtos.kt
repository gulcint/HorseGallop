package com.horsegallop.data.remote.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// ─── USER ───────────────────────────────────────────────────

@Serializable
data class SupabaseUserProfileDto(
    val id: String,
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val email: String = "",
    val phone: String? = null,
    val city: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("country_code") val countryCode: String = "TR",
    @SerialName("weight_kg") val weightKg: Double? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("subscription_tier") val subscriptionTier: String = "FREE",
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class SupabaseUserSettingsDto(
    val id: String = "",
    @SerialName("user_id") val userId: String,
    @SerialName("theme_mode") val themeMode: String = "SYSTEM",
    val language: String = "SYSTEM",
    @SerialName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerialName("weight_unit") val weightUnit: String = "kg",
    @SerialName("distance_unit") val distanceUnit: String = "km"
)

// ─── HORSE ──────────────────────────────────────────────────

@Serializable
data class SupabaseHorseDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String,
    val breed: String = "",
    @SerialName("birth_year") val birthYear: Int? = null,
    val color: String = "",
    val gender: String = "unknown",
    @SerialName("weight_kg") val weightKg: Int? = null,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class SupabaseHorseBreedDto(
    val id: String,
    @SerialName("name_en") val nameEn: String = "",
    @SerialName("name_tr") val nameTr: String = "",
    @SerialName("sort_order") val sortOrder: Int = 99
)

@Serializable
data class SupabaseHorseTipDto(
    val id: String,
    val locale: String = "en",
    val category: String = "",
    val title: String = "",
    val body: String = ""
)

// ─── BARN ───────────────────────────────────────────────────

@Serializable
data class SupabaseBarnDto(
    val id: String,
    val name: String,
    val description: String = "",
    val location: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val tags: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val rating: Double = 0.0,
    @SerialName("review_count") val reviewCount: Int = 0,
    @SerialName("hero_image_url") val heroImageUrl: String? = null,
    val capacity: Int = 0,
    val phone: String? = null,
    @SerialName("is_federated") val isFederated: Boolean = false,
    @SerialName("owner_user_id") val ownerUserId: String? = null
)

@Serializable
data class SupabaseLessonDto(
    val id: String,
    @SerialName("barn_id") val barnId: String? = null,
    val title: String,
    @SerialName("instructor_name") val instructorName: String = "",
    @SerialName("lesson_date") val lessonDate: String? = null,
    @SerialName("duration_min") val durationMin: Int = 60,
    val level: String = "Beginner",
    val price: Double = 0.0,
    @SerialName("spots_total") val spotsTotal: Int = 10,
    @SerialName("spots_available") val spotsAvailable: Int = 10,
    @SerialName("is_cancelled") val isCancelled: Boolean = false
)

// ─── RESERVATION ────────────────────────────────────────────

@Serializable
data class SupabaseReservationDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("lesson_title") val lessonTitle: String = "",
    @SerialName("lesson_date") val lessonDate: String = "",
    @SerialName("instructor_name") val instructorName: String = "",
    @SerialName("barn_id") val barnId: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String = ""
)

// ─── REVIEW ─────────────────────────────────────────────────

@Serializable
data class SupabaseReviewDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("author_name") val authorName: String = "",
    @SerialName("target_id") val targetId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_name") val targetName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

// ─── RIDE ───────────────────────────────────────────────────

@Serializable
data class SupabaseRideDto(
    val id: String,
    @SerialName("user_id") val userId: String = "",
    @SerialName("duration_sec") val durationSec: Int = 0,
    @SerialName("distance_km") val distanceKm: Double = 0.0,
    val calories: Double = 0.0,
    @SerialName("avg_speed_kmh") val avgSpeedKmh: Double = 0.0,
    @SerialName("max_speed_kmh") val maxSpeedKmh: Double = 0.0,
    @SerialName("ride_type") val rideType: String = "FREE",
    @SerialName("barn_name") val barnName: String? = null,
    @SerialName("started_at") val startedAt: String = "",
    @SerialName("saved_at") val savedAt: String = ""
)

@Serializable
data class SupabaseRidePathPointDto(
    @SerialName("ride_id") val rideId: String,
    @SerialName("user_id") val userId: String = "",
    val lat: Double,
    val lng: Double,
    @SerialName("alt_m") val altM: Double? = null,
    @SerialName("speed_kmh") val speedKmh: Double? = null,
    @SerialName("timestamp_ms") val timestampMs: Long? = null,
    @SerialName("sort_order") val sortOrder: Int = 0
)

// ─── HEALTH ─────────────────────────────────────────────────

@Serializable
data class SupabaseHorseHealthEventDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("horse_id") val horseId: String,
    val type: String,
    @SerialName("event_date") val eventDate: String,
    val notes: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class SupabaseHealthEventDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("horse_id") val horseId: String? = null,
    @SerialName("horse_name") val horseName: String = "",
    val type: String,
    @SerialName("scheduled_date") val scheduledDate: String,
    @SerialName("completed_date") val completedDate: String? = null,
    val notes: String = "",
    @SerialName("is_completed") val isCompleted: Boolean = false
)

// ─── CHALLENGE & BADGE ───────────────────────────────────────

@Serializable
data class SupabaseChallengeDto(
    val id: String,
    val title: String = "",
    @SerialName("title_en") val titleEn: String = "",
    val description: String = "",
    @SerialName("description_en") val descriptionEn: String = "",
    @SerialName("target_value") val targetValue: Double = 1.0,
    val unit: String = "rides",
    val icon: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = ""
)

@Serializable
data class SupabaseUserChallengeProgressDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("current_value") val currentValue: Double = 0.0,
    @SerialName("updated_at") val updatedAt: String = "",
    val challenge: SupabaseChallengeDto? = null
)

@Serializable
data class SupabaseUserBadgeDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val type: String,
    @SerialName("earned_date") val earnedDate: String = ""
)

// ─── CONTENT ────────────────────────────────────────────────

@Serializable
data class SupabaseAppContentDto(
    val id: String = "",
    val locale: String,
    val key: String,
    val value: String = ""
)

@Serializable
data class SupabaseEquestrianAnnouncementDto(
    val id: String,
    val title: String = "",
    val summary: String = "",
    @SerialName("published_at_label") val publishedAtLabel: String = "",
    @SerialName("detail_url") val detailUrl: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("cached_at") val cachedAt: String = ""
)

@Serializable
data class SupabaseEquestrianCompetitionDto(
    val id: String,
    val title: String = "",
    val location: String = "",
    @SerialName("date_label") val dateLabel: String = "",
    @SerialName("detail_url") val detailUrl: String = "",
    @SerialName("cached_at") val cachedAt: String = ""
)

// ─── BARN DETAIL (instructors & reviews) ────────────────────

@Serializable
data class SupabaseBarnInstructorDto(
    val id: String,
    @SerialName("barn_id") val barnId: String = "",
    val name: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    val specialty: String = "",
    val rating: Double = 0.0
)

@Serializable
data class SupabaseBarnReviewDto(
    val id: String,
    @SerialName("barn_id") val barnId: String = "",
    @SerialName("author_name") val authorName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    @SerialName("date_label") val dateLabel: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

// ─── SUBSCRIPTION ────────────────────────────────────────────

@Serializable
data class SupabaseSubscriptionDto(
    @SerialName("is_pro") val isPro: Boolean = false,
    @SerialName("subscription_tier") val subscriptionTier: String = "FREE",
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null
)

// ─── VERIFY PURCHASE ─────────────────────────────────────────

@Serializable
data class VerifyPurchaseResponseDto(
    val verified: Boolean = false,
    val tier: String = "FREE",
    @SerialName("expires_at") val expiresAt: String? = null
)

// ─── BARN MANAGEMENT ─────────────────────────────────────────

@Serializable
data class SupabaseManagedLessonDto(
    val id: String,
    @SerialName("barn_id") val barnId: String = "",
    val title: String = "",
    @SerialName("instructor_name") val instructorName: String = "",
    @SerialName("start_time_ms") val startTimeMs: Long = 0L,
    @SerialName("duration_min") val durationMin: Int = 60,
    val level: String = "Beginner",
    val price: Double = 0.0,
    @SerialName("spots_total") val spotsTotal: Int = 10,
    @SerialName("spots_booked") val spotsBooked: Int = 0,
    @SerialName("is_cancelled") val isCancelled: Boolean = false
)

@Serializable
data class SupabaseStudentRosterEntryDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String = "",
    val email: String = "",
    @SerialName("reservation_id") val reservationId: String,
    @SerialName("booked_at_ms") val bookedAtMs: Long = 0L
)

// ─── TBF ─────────────────────────────────────────────────────

@Serializable
data class SupabaseTbfEventDto(
    val id: String,
    val date: String = "",
    val type: String = "",
    @SerialName("venue_code") val venueCode: String = "",
    @SerialName("venue_name") val venueName: String = "",
    @SerialName("event_count") val eventCount: Int = 0,
    val time: String = "",
    @SerialName("cached_at") val cachedAt: String = ""
)

@Serializable
data class SupabaseTbfAthleteDto(
    val no: String = "",
    val name: String = "",
    val jockey: String = "",
    val trainer: String = "",
    val owner: String = "",
    val weight: Int = 0,
    val age: String = "",
    val last6: String = "",
    val odds: String = "",
    @SerialName("best_time") val bestTime: String = "",
    val result: String = "",
    val time: String = "",
    val gap: String = ""
)

@Serializable
data class SupabaseTbfCompetitionDto(
    val id: String,
    @SerialName("event_id") val eventId: String = "",
    val no: String = "",
    val name: String = "",
    val distance: Int = 0,
    val surface: String = "",
    val time: String = "",
    val prize: Long = 0L,
    val athletes: List<SupabaseTbfAthleteDto> = emptyList()
)

// ─── FEDERATION SYNC LOG ─────────────────────────────────────

@Serializable
data class SupabaseFederationSyncLogDto(
    val id: String = "",
    val status: String = "unknown",
    @SerialName("synced_at") val syncedAt: String = "",
    @SerialName("item_count") val itemCount: Int = 0,
    @SerialName("error_message") val errorMessage: String? = null
)

// ─── NOTIFICATION ─────────────────────────────────────────────

@Serializable
data class SupabaseNotificationDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "general",
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    val data: JsonObject? = null
)

// ─── TBF ACTIVITY CALENDAR ────────────────────────────────────

@Serializable
data class SupabaseTbfActivityDto(
    val id: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val title: String,
    val organization: String,
    val city: String,
    val discipline: String,
    @SerialName("activity_type") val activityType: String,
    @SerialName("detail_url") val detailUrl: String = "",
    @SerialName("cached_at") val cachedAt: String = ""
)

fun SupabaseTbfActivityDto.toDomain(): com.horsegallop.domain.equestrian.model.TbfActivity =
    com.horsegallop.domain.equestrian.model.TbfActivity(
        id = id,
        startDate = java.time.LocalDate.parse(startDate),
        endDate = java.time.LocalDate.parse(endDate),
        title = title,
        organization = organization,
        city = city,
        discipline = com.horsegallop.domain.equestrian.model.TbfDiscipline.fromString(discipline),
        type = com.horsegallop.domain.equestrian.model.TbfActivityType.fromString(activityType),
        detailUrl = detailUrl
    )
