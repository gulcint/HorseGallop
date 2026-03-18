package com.horsegallop.data.remote.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseDataSource @Inject constructor(
    private val supabase: SupabaseClient
) {

    // ─── AUTH ────────────────────────────────────────────────

    suspend fun signInWithEmail(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, firstName: String = "", lastName: String = "") {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("first_name", firstName)
                put("last_name", lastName)
            }
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    fun currentUserEmail(): String? = supabase.auth.currentUserOrNull()?.email

    // ─── USER PROFILE ────────────────────────────────────────

    suspend fun getUserProfile(): SupabaseUserProfileDto? {
        val uid = currentUserId() ?: return null
        return supabase.from("user_profiles")
            .select { filter { eq("id", uid) } }
            .decodeSingleOrNull()
    }

    suspend fun updateUserProfile(updates: Map<String, Any?>) {
        val uid = currentUserId() ?: return
        supabase.from("user_profiles")
            .update(updates) { filter { eq("id", uid) } }
    }

    // ─── USER SETTINGS ───────────────────────────────────────

    suspend fun getUserSettings(): SupabaseUserSettingsDto? {
        val uid = currentUserId() ?: return null
        return supabase.from("user_settings")
            .select { filter { eq("user_id", uid) } }
            .decodeSingleOrNull()
    }

    suspend fun updateUserSettings(updates: Map<String, Any?>) {
        val uid = currentUserId() ?: return
        supabase.from("user_settings")
            .update(updates) { filter { eq("user_id", uid) } }
    }

    // ─── HORSES ─────────────────────────────────────────────

    suspend fun getMyHorses(): List<SupabaseHorseDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("horses")
            .select { filter { eq("user_id", uid) } }
            .decodeList()
    }

    suspend fun addHorse(horse: SupabaseHorseDto): SupabaseHorseDto {
        return supabase.from("horses")
            .insert(horse) { select() }
            .decodeSingle()
    }

    suspend fun deleteHorse(horseId: String) {
        supabase.from("horses")
            .delete { filter { eq("id", horseId) } }
    }

    // ─── BREEDS & TIPS ───────────────────────────────────────

    suspend fun getBreeds(): List<SupabaseHorseBreedDto> {
        return supabase.from("horse_breeds")
            .select { order("sort_order", Order.ASCENDING) }
            .decodeList()
    }

    suspend fun getHorseTips(locale: String): List<SupabaseHorseTipDto> {
        return supabase.from("horse_tips")
            .select { filter { eq("locale", locale) } }
            .decodeList()
    }

    // ─── APP CONTENT ─────────────────────────────────────────

    suspend fun getAppContent(locale: String): List<SupabaseAppContentDto> {
        return supabase.from("app_content")
            .select { filter { eq("locale", locale) } }
            .decodeList()
    }

    // ─── BARNS ───────────────────────────────────────────────

    suspend fun getBarns(): List<SupabaseBarnDto> {
        return supabase.from("barns")
            .select()
            .decodeList()
    }

    suspend fun getBarnDetail(id: String): SupabaseBarnDto? {
        return supabase.from("barns")
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()
    }

    // ─── LESSONS ─────────────────────────────────────────────

    suspend fun getLessons(barnId: String? = null): List<SupabaseLessonDto> {
        return supabase.from("lessons")
            .select {
                filter {
                    eq("is_cancelled", false)
                    if (barnId != null) eq("barn_id", barnId)
                }
                order("lesson_date", Order.ASCENDING)
            }
            .decodeList()
    }

    // ─── RESERVATIONS ────────────────────────────────────────

    suspend fun getMyReservations(): List<SupabaseReservationDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("reservations")
            .select { filter { eq("user_id", uid) } }
            .decodeList()
    }

    suspend fun bookLesson(lesson: SupabaseLessonDto): SupabaseReservationDto {
        val uid = currentUserId() ?: error("Not authenticated")
        val reservation = SupabaseReservationDto(
            lessonId = lesson.id,
            lessonTitle = lesson.title,
            lessonDate = lesson.lessonDate ?: "",
            instructorName = lesson.instructorName,
            barnId = lesson.barnId
        )
        val result = supabase.from("reservations")
            .insert(reservation) { select() }
            .decodeSingle<SupabaseReservationDto>()

        // Decrement spots_available
        supabase.from("lessons")
            .update(mapOf("spots_available" to lesson.spotsAvailable - 1)) {
                filter { eq("id", lesson.id) }
            }

        return result
    }

    suspend fun cancelReservation(reservationId: String) {
        supabase.from("reservations")
            .update(mapOf("status" to "cancelled")) {
                filter { eq("id", reservationId) }
            }
    }

    // ─── REVIEWS ─────────────────────────────────────────────

    suspend fun getMyReviews(): List<SupabaseReviewDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("reviews")
            .select { filter { eq("user_id", uid) } }
            .decodeList()
    }

    suspend fun submitReview(review: SupabaseReviewDto): SupabaseReviewDto {
        return supabase.from("reviews")
            .insert(review) { select() }
            .decodeSingle()
    }

    // ─── RIDES ───────────────────────────────────────────────

    suspend fun getMyRides(): List<SupabaseRideDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("rides")
            .select {
                filter { eq("user_id", uid) }
                order("started_at", Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun saveRide(ride: SupabaseRideDto, pathPoints: List<SupabaseRidePathPointDto> = emptyList()): Boolean {
        supabase.from("rides").upsert(ride)
        if (pathPoints.isNotEmpty()) {
            supabase.from("ride_path_points").insert(pathPoints)
        }
        return true
    }

    // ─── HORSE HEALTH EVENTS ─────────────────────────────────

    suspend fun getHorseHealthEvents(horseId: String): List<SupabaseHorseHealthEventDto> {
        return supabase.from("horse_health_events")
            .select { filter { eq("horse_id", horseId) } }
            .decodeList()
    }

    suspend fun addHorseHealthEvent(event: SupabaseHorseHealthEventDto): SupabaseHorseHealthEventDto {
        return supabase.from("horse_health_events")
            .insert(event) { select() }
            .decodeSingle()
    }

    suspend fun updateHorseHealthEvent(id: String, updates: Map<String, Any?>) {
        supabase.from("horse_health_events")
            .update(updates) { filter { eq("id", id) } }
    }

    suspend fun deleteHorseHealthEvent(id: String) {
        supabase.from("horse_health_events")
            .delete { filter { eq("id", id) } }
    }

    // ─── HEALTH EVENTS (rider) ───────────────────────────────

    suspend fun getHealthEvents(horseId: String? = null): List<SupabaseHealthEventDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("health_events")
            .select {
                filter {
                    eq("user_id", uid)
                    if (horseId != null) eq("horse_id", horseId)
                }
                order("scheduled_date", Order.ASCENDING)
            }
            .decodeList()
    }

    suspend fun saveHealthEvent(event: SupabaseHealthEventDto): SupabaseHealthEventDto {
        return supabase.from("health_events")
            .upsert(event) { select() }
            .decodeSingle()
    }

    suspend fun deleteHealthEvent(eventId: String) {
        supabase.from("health_events")
            .delete { filter { eq("id", eventId) } }
    }

    suspend fun markHealthEventCompleted(eventId: String, completedDate: String) {
        supabase.from("health_events")
            .update(mapOf("is_completed" to true, "completed_date" to completedDate)) {
                filter { eq("id", eventId) }
            }
    }

    // ─── CHALLENGES & BADGES ─────────────────────────────────

    suspend fun getActiveChallenges(): List<SupabaseChallengeDto> {
        return supabase.from("challenges")
            .select()
            .decodeList()
    }

    suspend fun getUserChallengeProgress(): List<SupabaseUserChallengeProgressDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("user_challenge_progress")
            .select(Columns.raw("*, challenge:challenges(*)")) {
                filter { eq("user_id", uid) }
            }
            .decodeList()
    }

    suspend fun getEarnedBadges(): List<SupabaseUserBadgeDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("user_badges")
            .select { filter { eq("user_id", uid) } }
            .decodeList()
    }

    suspend fun insertBadge(badge: SupabaseUserBadgeDto): SupabaseUserBadgeDto {
        return supabase.from("user_badges")
            .insert(badge) { select() }
            .decodeSingle()
    }

    // ─── EQUESTRIAN ──────────────────────────────────────────

    suspend fun getEquestrianAnnouncements(): List<SupabaseEquestrianAnnouncementDto> {
        return supabase.from("equestrian_announcements")
            .select { order("cached_at", Order.DESCENDING) }
            .decodeList()
    }

    suspend fun getEquestrianCompetitions(): List<SupabaseEquestrianCompetitionDto> {
        return supabase.from("equestrian_competitions")
            .select { order("cached_at", Order.DESCENDING) }
            .decodeList()
    }

    // ─── BARN DETAIL (instructors & reviews) ─────────────────

    suspend fun getBarnInstructors(barnId: String): List<SupabaseBarnInstructorDto> {
        return supabase.from("barn_instructors")
            .select { filter { eq("barn_id", barnId) } }
            .decodeList()
    }

    suspend fun getBarnReviews(barnId: String): List<SupabaseBarnReviewDto> {
        return supabase.from("barn_reviews")
            .select {
                filter { eq("barn_id", barnId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }

    // ─── HOME DASHBOARD ──────────────────────────────────────

    suspend fun getRecentRides(limit: Int = 10): List<SupabaseRideDto> {
        val uid = currentUserId() ?: return emptyList()
        return supabase.from("rides")
            .select {
                filter { eq("user_id", uid) }
                order("started_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList()
    }

    // ─── USER SETTINGS ───────────────────────────────────────

    suspend fun getUserSettingsById(): SupabaseUserSettingsDto? {
        val uid = currentUserId() ?: return null
        return supabase.from("user_settings")
            .select { filter { eq("user_id", uid) } }
            .decodeSingleOrNull()
    }

    suspend fun upsertUserSettings(settings: SupabaseUserSettingsDto) {
        supabase.from("user_settings").upsert(settings)
    }

    // ─── SUBSCRIPTION ────────────────────────────────────────

    suspend fun getSubscriptionStatus(): SupabaseSubscriptionDto? {
        val uid = currentUserId() ?: return null
        return supabase.from("user_profiles")
            .select(Columns.raw("is_pro, subscription_tier, subscription_expires_at")) {
                filter { eq("id", uid) }
            }
            .decodeSingleOrNull()
    }

    // ─── BARN MANAGEMENT ─────────────────────────────────────

    suspend fun getBarnStats(barnId: String): Map<String, Any?> {
        // Aggregate stats computed client-side from managed_lessons and reservations tables
        val lessons = getManagedLessons(barnId)
        val upcoming = lessons.count { !it.isCancelled && it.startTimeMs > System.currentTimeMillis() }
        val allReservations = supabase.from("reservations")
            .select { filter { eq("barn_id", barnId) } }
            .decodeList<SupabaseReservationDto>()
        val uniqueStudents = allReservations.map { it.userId }.toSet().size
        return mapOf(
            "totalLessons" to lessons.size,
            "totalReservations" to allReservations.size,
            "uniqueStudents" to uniqueStudents,
            "upcomingLessonsCount" to upcoming
        )
    }

    suspend fun getManagedLessons(barnId: String): List<SupabaseManagedLessonDto> {
        return supabase.from("lessons")
            .select {
                filter { eq("barn_id", barnId) }
                order("start_time_ms", Order.ASCENDING)
            }
            .decodeList()
    }

    suspend fun createLesson(lesson: SupabaseManagedLessonDto): SupabaseManagedLessonDto {
        return supabase.from("lessons")
            .insert(lesson) { select() }
            .decodeSingle()
    }

    suspend fun cancelLesson(lessonId: String) {
        supabase.from("lessons")
            .update(mapOf("is_cancelled" to true)) {
                filter { eq("id", lessonId) }
            }
    }

    suspend fun getLessonRoster(lessonId: String): List<SupabaseStudentRosterEntryDto> {
        return supabase.from("reservations")
            .select(Columns.raw("user_id, display_name, email, id as reservation_id, booked_at_ms")) {
                filter {
                    eq("lesson_id", lessonId)
                    neq("status", "cancelled")
                }
            }
            .decodeList()
    }

    // ─── AI COACH ────────────────────────────────────────────

    suspend fun askAiCoach(
        question: String,
        history: List<SupabaseAiCoachMessageDto>
    ): SupabaseAiCoachResponseDto {
        val userId = currentUserId() ?: return SupabaseAiCoachResponseDto(answer = "")
        val body = buildJsonObject {
            put("message", question)
            put("userId", userId)
            put("conversationHistory", buildJsonArray {
                history.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role)
                        put("content", msg.text)
                    })
                }
            })
        }
        val response = supabase.functions.invoke(function = "ai-coach", body = body)
        val json = Json.decodeFromString<JsonObject>(response.bodyAsText())
        val reply = json["reply"]?.jsonPrimitive?.content
            ?: throw Exception("No reply from AI coach")
        return SupabaseAiCoachResponseDto(answer = reply)
    }

    // ─── TBF (Turkey Equestrian Federation) ──────────────────

    suspend fun getTbfEventDays(date: String? = null, type: String = "classic"): List<SupabaseTbfEventDto> {
        return supabase.from("tbf_events")
            .select {
                filter {
                    if (date != null) eq("date", date)
                    eq("type", type)
                }
                order("date", Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun getTbfCompetitions(eventId: String): List<SupabaseTbfCompetitionDto> {
        return supabase.from("tbf_competitions")
            .select { filter { eq("event_id", eventId) } }
            .decodeList()
    }

    // ─── FEDERATION SYNC (status only — sync is backend-side) ─

    suspend fun getFederatedBarnsSyncStatus(): Map<String, Any?> {
        // Returns latest sync log entry
        val row = supabase.from("federation_sync_log")
            .select { order("synced_at", Order.DESCENDING); limit(1) }
            .decodeSingleOrNull<SupabaseFederationSyncLogDto>()
        return mapOf(
            "status" to (row?.status ?: "unknown"),
            "syncedAt" to (row?.syncedAt ?: ""),
            "itemCount" to (row?.itemCount ?: 0),
            "errorMessage" to row?.errorMessage
        )
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────

    suspend fun getNotifications(userId: String): Result<List<SupabaseNotificationDto>> =
        runCatching {
            supabase.from("notifications")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                    limit(50)
                }
                .decodeList()
        }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        supabase.from("notifications")
            .update(mapOf("is_read" to true)) {
                filter { eq("id", notificationId) }
            }
    }

    suspend fun markAllNotificationsRead(userId: String): Result<Unit> = runCatching {
        supabase.from("notifications")
            .update(mapOf("is_read" to true)) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
    }

    fun getNotificationsFlow(userId: String): Flow<List<SupabaseNotificationDto>> = flow {
        val initial = getNotifications(userId).getOrDefault(emptyList())
        emit(initial)

        val channel = supabase.channel("notifications:$userId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()

        emitAll(changeFlow.map {
            getNotifications(userId).getOrDefault(emptyList())
        })
    }

    // ─── TBF ACTIVITY CALENDAR ───────────────────────────────

    suspend fun getTbfActivities(
        yearMonth: String? = null,
        discipline: String? = null
    ): Result<List<SupabaseTbfActivityDto>> = runCatching {
        supabase.from("tbf_activities").select {
            filter {
                if (yearMonth != null) {
                    gte("start_date", "$yearMonth-01")
                    lte("start_date", "$yearMonth-31")
                }
                if (discipline != null) eq("discipline", discipline)
            }
            order("start_date", Order.ASCENDING)
        }.decodeList<SupabaseTbfActivityDto>()
    }

    // ─── PROFILE STORAGE ─────────────────────────────────────

    suspend fun uploadProfilePhoto(userId: String, data: ByteArray): Result<String> =
        runCatching {
            val path = "user/$userId/avatar.jpg"
            supabase.storage["profile-photos"].upload(path, data) { upsert = true }
            supabase.storage["profile-photos"].publicUrl(path)
        }
}
