package com.horsegallop.data.remote.functions

import com.google.firebase.functions.FirebaseFunctions
import com.horsegallop.data.remote.dto.BarnFunctionsDto
import com.horsegallop.data.remote.dto.BarnInstructorFunctionsDto
import com.horsegallop.data.remote.dto.BarnReviewFunctionsDto
import com.horsegallop.data.remote.dto.AppContentFunctionsDto
import com.horsegallop.data.remote.dto.BreedFunctionsDto
import com.horsegallop.data.remote.dto.FederationManualSyncFunctionsDto
import com.horsegallop.data.remote.dto.FederatedBarnSyncStatusFunctionsDto
import com.horsegallop.data.remote.dto.HomeDashboardFunctionsDto
import com.horsegallop.data.remote.dto.HomeRecentActivityFunctionsDto
import com.horsegallop.data.remote.dto.HomeStatsFunctionsDto
import com.horsegallop.data.remote.dto.HorseFunctionsDto
import com.horsegallop.data.remote.dto.HorseHealthEventFunctionsDto
import com.horsegallop.data.remote.dto.HorseTipFunctionsDto
import com.horsegallop.data.remote.dto.LessonFunctionsDto
import com.horsegallop.data.remote.dto.ReservationFunctionsDto
import com.horsegallop.data.remote.dto.ReviewFunctionsDto
import com.horsegallop.data.remote.dto.UserSettingsFunctionsDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class AppFunctionsDataSource @Inject constructor(
    private val functions: FirebaseFunctions
) {

    suspend fun getHomeDashboard(limit: Int = 5): HomeDashboardFunctionsDto {
        val result = functions
            .getHttpsCallable("getHomeDashboard")
            .call(hashMapOf("limit" to limit))
            .await()

        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val stats = payload["stats"] as? Map<*, *> ?: emptyMap<String, Any?>()
        val recent = payload["recentActivities"] as? List<*> ?: emptyList<Any?>()

        return HomeDashboardFunctionsDto(
            stats = HomeStatsFunctionsDto(
                totalRides = (stats["totalRides"] as? Number)?.toInt() ?: 0,
                totalDistanceKm = (stats["totalDistanceKm"] as? Number)?.toDouble() ?: 0.0,
                totalDurationMin = (stats["totalDurationMin"] as? Number)?.toInt() ?: 0,
                totalCalories = (stats["totalCalories"] as? Number)?.toDouble() ?: 0.0,
                favoriteBarn = (stats["favoriteBarn"] as? String)?.ifBlank { null }
            ),
            recentActivities = recent.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                HomeRecentActivityFunctionsDto(
                    id = map["id"] as? String ?: return@mapNotNull null,
                    title = (map["title"] as? String).orEmpty(),
                    dateLabel = (map["dateLabel"] as? String).orEmpty(),
                    timeLabel = (map["timeLabel"] as? String).orEmpty(),
                    durationMin = (map["durationMin"] as? Number)?.toInt() ?: 0,
                    distanceKm = (map["distanceKm"] as? Number)?.toDouble() ?: 0.0
                )
            }
        )
    }

    suspend fun getBarns(lat: Double? = null, lng: Double? = null): List<BarnFunctionsDto> {
        val params = if (lat != null && lng != null) hashMapOf("lat" to lat, "lng" to lng) else null
        val result = functions.getHttpsCallable("getFederatedBarns").call(params).await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            mapBarn(map)
        }
    }

    suspend fun getBarnDetail(id: String): BarnFunctionsDto {
        val result = functions.getHttpsCallable("getFederatedBarnDetail").call(hashMapOf("id" to id)).await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        return mapBarn(payload) ?: throw IllegalStateException("Invalid barn payload")
    }

    suspend fun getFederatedBarnsSyncStatus(): FederatedBarnSyncStatusFunctionsDto {
        val result = functions.getHttpsCallable("getFederatedBarnsSyncStatus").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        return FederatedBarnSyncStatusFunctionsDto(
            status = (payload["status"] as? String).orEmpty(),
            syncedAt = (payload["syncedAt"] as? String).orEmpty(),
            itemCount = (payload["itemCount"] as? Number)?.toInt() ?: 0,
            errorMessage = payload["errorMessage"] as? String
        )
    }

    suspend fun triggerFederationManualSync(): FederationManualSyncFunctionsDto {
        val result = functions.getHttpsCallable("triggerFederationManualSync").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        return FederationManualSyncFunctionsDto(
            syncedAt = (payload["syncedAt"] as? String).orEmpty(),
            barnsCount = (payload["barnsCount"] as? Number)?.toInt() ?: 0,
            announcementsCount = (payload["announcementsCount"] as? Number)?.toInt() ?: 0,
            competitionsCount = (payload["competitionsCount"] as? Number)?.toInt() ?: 0,
            throttled = (payload["throttled"] as? Boolean) ?: false
        )
    }

    suspend fun getLessons(from: String? = null, to: String? = null): List<LessonFunctionsDto> {
        val request = hashMapOf<String, Any>()
        from?.let { request["from"] = it }
        to?.let { request["to"] = it }

        val result = functions.getHttpsCallable("getLessons").call(request).await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()

        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            LessonFunctionsDto(
                id = map["id"] as? String ?: return@mapNotNull null,
                date = (map["date"] as? String).orEmpty(),
                title = (map["title"] as? String).orEmpty(),
                instructorName = (map["instructorName"] as? String).orEmpty(),
                durationMin = (map["durationMin"] as? Number)?.toInt() ?: 0,
                level = (map["level"] as? String).orEmpty(),
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                spotsTotal = (map["spotsTotal"] as? Number)?.toInt() ?: 0,
                spotsAvailable = (map["spotsAvailable"] as? Number)?.toInt() ?: 0,
                isBookedByMe = (map["isBookedByMe"] as? Boolean) ?: false
            )
        }
    }

    suspend fun getHorseTips(locale: String): List<HorseTipFunctionsDto> {
        val result = functions.getHttpsCallable("getHorseTips")
            .call(hashMapOf("locale" to locale))
            .await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            HorseTipFunctionsDto(
                id = map["id"] as? String ?: return@mapNotNull null,
                title = (map["title"] as? String).orEmpty(),
                body = (map["body"] as? String).orEmpty(),
                category = (map["category"] as? String).orEmpty(),
                locale = (map["locale"] as? String).orEmpty()
            )
        }
    }

    suspend fun getBreeds(locale: String): List<BreedFunctionsDto> {
        val result = functions.getHttpsCallable("getBreeds")
            .call(hashMapOf("locale" to locale))
            .await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            BreedFunctionsDto(
                id = map["id"] as? String ?: return@mapNotNull null,
                nameEn = (map["nameEn"] as? String).orEmpty(),
                nameTr = (map["nameTr"] as? String).orEmpty(),
                sortOrder = (map["sortOrder"] as? Number)?.toInt() ?: 99
            )
        }
    }

    suspend fun getAppContent(locale: String): AppContentFunctionsDto {
        val result = functions.getHttpsCallable("getAppContent")
            .call(hashMapOf("locale" to locale))
            .await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val home = payload["home"] as? Map<*, *> ?: emptyMap<String, Any?>()
        val common = payload["common"] as? Map<*, *> ?: emptyMap<String, Any?>()
        val auth = payload["auth"] as? Map<*, *> ?: emptyMap<String, Any?>()
        val onboarding = payload["onboarding"] as? Map<*, *> ?: emptyMap<String, Any?>()
        val ride = payload["ride"] as? Map<*, *> ?: emptyMap<String, Any?>()
        val settings = payload["settings"] as? Map<*, *> ?: emptyMap<String, Any?>()

        return AppContentFunctionsDto(
            locale = (payload["locale"] as? String) ?: locale,
            homeHeroTitle = home["heroTitle"] as? String,
            homeHeroSubtitle = home["heroSubtitle"] as? String,
            offlineHelp = common["offlineHelp"] as? String,
            loginTitle = auth["loginTitle"] as? String,
            loginSubtitle = auth["loginSubtitle"] as? String,
            emailLoginTitle = auth["emailLoginTitle"] as? String,
            emailLoginSubtitle = auth["emailLoginSubtitle"] as? String,
            enrollTitle = auth["enrollTitle"] as? String,
            enrollSubtitle = auth["enrollSubtitle"] as? String,
            forgotPasswordSubtitle = auth["forgotPasswordSubtitle"] as? String,
            onboardingHeroTitle = onboarding["heroTitle"] as? String,
            onboardingHeroSubtitle = onboarding["heroSubtitle"] as? String,
            onboardingHelpText = onboarding["helpText"] as? String,
            rideLiveTitle = ride["liveTitle"] as? String,
            rideLiveSubtitleIdle = ride["liveSubtitleIdle"] as? String,
            rideLiveSubtitleActive = ride["liveSubtitleActive"] as? String,
            ridePermissionTitle = ride["permissionTitle"] as? String,
            ridePermissionHint = ride["permissionHint"] as? String,
            rideGrantLocationCta = ride["grantLocationCta"] as? String,
            settingsThemeSubtitle = settings["themeSubtitle"] as? String,
            settingsLanguageSubtitle = settings["languageSubtitle"] as? String,
            settingsNotificationsSubtitle = settings["notificationsSubtitle"] as? String,
            settingsPrivacySubtitle = settings["privacySubtitle"] as? String
        )
    }

    suspend fun submitReview(
        targetId: String, targetType: String, targetName: String,
        rating: Int, comment: String
    ): ReviewFunctionsDto {
        val result = functions.getHttpsCallable("submitReview").call(
            hashMapOf(
                "targetId" to targetId, "targetType" to targetType,
                "targetName" to targetName, "rating" to rating, "comment" to comment
            )
        ).await()
        val map = result.data as? Map<*, *> ?: error("Invalid response")
        return ReviewFunctionsDto(
            id = map["id"] as? String ?: error("No id"),
            targetId = (map["targetId"] as? String).orEmpty(),
            targetType = (map["targetType"] as? String).orEmpty(),
            targetName = (map["targetName"] as? String).orEmpty(),
            rating = (map["rating"] as? Number)?.toInt() ?: rating,
            comment = (map["comment"] as? String).orEmpty(),
            createdAt = (map["createdAt"] as? String).orEmpty(),
            authorName = (map["authorName"] as? String).orEmpty()
        )
    }

    suspend fun getMyReviews(): List<ReviewFunctionsDto> {
        val result = functions.getHttpsCallable("getMyReviews").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            ReviewFunctionsDto(
                id = map["id"] as? String ?: return@mapNotNull null,
                targetId = (map["targetId"] as? String).orEmpty(),
                targetType = (map["targetType"] as? String).orEmpty(),
                targetName = (map["targetName"] as? String).orEmpty(),
                rating = (map["rating"] as? Number)?.toInt() ?: 0,
                comment = (map["comment"] as? String).orEmpty(),
                createdAt = (map["createdAt"] as? String).orEmpty(),
                authorName = (map["authorName"] as? String).orEmpty()
            )
        }
    }

    suspend fun getMyHorses(): List<HorseFunctionsDto> {
        val result = functions.getHttpsCallable("getMyHorses").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            HorseFunctionsDto(
                id = map["id"] as? String ?: return@mapNotNull null,
                name = (map["name"] as? String).orEmpty(),
                breed = (map["breed"] as? String).orEmpty(),
                birthYear = (map["birthYear"] as? Number)?.toInt() ?: 0,
                color = (map["color"] as? String).orEmpty(),
                gender = (map["gender"] as? String).orEmpty(),
                weightKg = (map["weightKg"] as? Number)?.toInt() ?: 0,
                imageUrl = (map["imageUrl"] as? String).orEmpty()
            )
        }
    }

    suspend fun addHorse(
        name: String, breed: String, birthYear: Int,
        color: String, gender: String, weightKg: Int
    ): HorseFunctionsDto {
        val result = functions.getHttpsCallable("addHorse").call(
            hashMapOf(
                "name" to name, "breed" to breed, "birthYear" to birthYear,
                "color" to color, "gender" to gender, "weightKg" to weightKg
            )
        ).await()
        val map = result.data as? Map<*, *> ?: error("Invalid response")
        return HorseFunctionsDto(
            id = map["id"] as? String ?: error("No id"),
            name = (map["name"] as? String).orEmpty(),
            breed = (map["breed"] as? String).orEmpty(),
            birthYear = (map["birthYear"] as? Number)?.toInt() ?: 0,
            color = (map["color"] as? String).orEmpty(),
            gender = (map["gender"] as? String).orEmpty(),
            weightKg = (map["weightKg"] as? Number)?.toInt() ?: 0,
            imageUrl = (map["imageUrl"] as? String).orEmpty()
        )
    }

    suspend fun deleteHorse(horseId: String) {
        functions.getHttpsCallable("deleteHorse")
            .call(hashMapOf("horseId" to horseId))
            .await()
    }

    suspend fun bookLesson(lessonId: String): ReservationFunctionsDto {
        val result = functions.getHttpsCallable("bookLesson")
            .call(hashMapOf("lessonId" to lessonId))
            .await()
        val map = result.data as? Map<*, *> ?: error("Invalid response")
        return mapReservation(map) ?: error("Invalid reservation payload")
    }

    suspend fun cancelReservation(reservationId: String) {
        functions.getHttpsCallable("cancelReservation")
            .call(hashMapOf("reservationId" to reservationId))
            .await()
    }

    suspend fun getMyReservations(): List<ReservationFunctionsDto> {
        val result = functions.getHttpsCallable("getMyReservations").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            mapReservation(map)
        }
    }

    private fun mapReservation(map: Map<*, *>): ReservationFunctionsDto? {
        return ReservationFunctionsDto(
            id = map["id"] as? String ?: return null,
            lessonId = (map["lessonId"] as? String).orEmpty(),
            lessonTitle = (map["lessonTitle"] as? String).orEmpty(),
            lessonDate = (map["lessonDate"] as? String).orEmpty(),
            instructorName = (map["instructorName"] as? String).orEmpty(),
            status = (map["status"] as? String).orEmpty(),
            createdAt = (map["createdAt"] as? String).orEmpty()
        )
    }

    private fun mapBarn(map: Map<*, *>): BarnFunctionsDto? {
        val instructors = (map["instructors"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            BarnInstructorFunctionsDto(
                id = m["id"] as? String ?: return@mapNotNull null,
                name = (m["name"] as? String).orEmpty(),
                photoUrl = (m["photoUrl"] as? String).orEmpty(),
                specialty = (m["specialty"] as? String).orEmpty(),
                rating = (m["rating"] as? Number)?.toDouble() ?: 0.0
            )
        } ?: emptyList()

        val reviews = (map["reviews"] as? List<*>)?.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            BarnReviewFunctionsDto(
                id = m["id"] as? String ?: return@mapNotNull null,
                authorName = (m["authorName"] as? String).orEmpty(),
                rating = (m["rating"] as? Number)?.toInt() ?: 5,
                comment = (m["comment"] as? String).orEmpty(),
                dateLabel = (m["dateLabel"] as? String).orEmpty()
            )
        } ?: emptyList()

        return BarnFunctionsDto(
            id = map["id"] as? String ?: return null,
            name = (map["name"] as? String).orEmpty(),
            description = (map["description"] as? String).orEmpty(),
            location = (map["location"] as? String).orEmpty(),
            lat = (map["lat"] as? Number)?.toDouble() ?: 0.0,
            lng = (map["lng"] as? Number)?.toDouble() ?: 0.0,
            tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            amenities = (map["amenities"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
            reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 0,
            heroImageUrl = map["heroImageUrl"] as? String,
            capacity = (map["capacity"] as? Number)?.toInt() ?: 0,
            phone = map["phone"] as? String,
            instructors = instructors,
            reviews = reviews
        )
    }

    // ─── User Settings ────────────────────────────────────────────────────

    suspend fun getUserSettings(): UserSettingsFunctionsDto {
        val result = functions.getHttpsCallable("getUserSettings").call().await()
        val map = result.data as? Map<*, *> ?: return UserSettingsFunctionsDto()
        return UserSettingsFunctionsDto(
            themeMode = (map["themeMode"] as? String) ?: "SYSTEM",
            language = (map["language"] as? String) ?: "SYSTEM",
            notificationsEnabled = (map["notificationsEnabled"] as? Boolean) ?: true,
            weightUnit = (map["weightUnit"] as? String) ?: "kg",
            distanceUnit = (map["distanceUnit"] as? String) ?: "km"
        )
    }

    suspend fun updateUserSettings(
        themeMode: String? = null,
        language: String? = null,
        notificationsEnabled: Boolean? = null,
        weightUnit: String? = null,
        distanceUnit: String? = null
    ) {
        val payload = hashMapOf<String, Any?>()
        if (themeMode != null) payload["themeMode"] = themeMode
        if (language != null) payload["language"] = language
        if (notificationsEnabled != null) payload["notificationsEnabled"] = notificationsEnabled
        if (weightUnit != null) payload["weightUnit"] = weightUnit
        if (distanceUnit != null) payload["distanceUnit"] = distanceUnit
        if (payload.isNotEmpty()) {
            functions.getHttpsCallable("updateUserSettings").call(payload).await()
        }
    }

    // ─── Horse Health Events ──────────────────────────────────────────────

    suspend fun getHorseHealthEvents(horseId: String): List<HorseHealthEventFunctionsDto> {
        val result = functions.getHttpsCallable("getHorseHealthEvents")
            .call(hashMapOf("horseId" to horseId)).await()
        val payload = result.data as? Map<*, *> ?: return emptyList()
        val items = payload["items"] as? List<*> ?: return emptyList()
        return items.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            HorseHealthEventFunctionsDto(
                id = m["id"] as? String ?: return@mapNotNull null,
                horseId = horseId,
                type = (m["type"] as? String) ?: "OTHER",
                date = (m["date"] as? String).orEmpty(),
                notes = (m["notes"] as? String).orEmpty(),
                createdAt = (m["createdAt"] as? String).orEmpty()
            )
        }
    }

    suspend fun addHorseHealthEvent(
        horseId: String,
        type: String,
        date: String,
        notes: String
    ): HorseHealthEventFunctionsDto {
        val result = functions.getHttpsCallable("addHorseHealthEvent")
            .call(hashMapOf("horseId" to horseId, "type" to type, "date" to date, "notes" to notes))
            .await()
        val map = result.data as? Map<*, *> ?: throw IllegalStateException("Invalid response")
        return HorseHealthEventFunctionsDto(
            id = map["id"] as? String ?: throw IllegalStateException("Missing id"),
            horseId = horseId,
            type = (map["type"] as? String) ?: type,
            date = (map["date"] as? String) ?: date,
            notes = (map["notes"] as? String) ?: notes,
            createdAt = (map["createdAt"] as? String).orEmpty()
        )
    }

    suspend fun updateHorseHealthEvent(
        id: String,
        horseId: String,
        type: String? = null,
        date: String? = null,
        notes: String? = null
    ) {
        val payload = hashMapOf<String, Any?>("id" to id, "horseId" to horseId)
        if (type != null) payload["type"] = type
        if (date != null) payload["date"] = date
        if (notes != null) payload["notes"] = notes
        functions.getHttpsCallable("updateHorseHealthEvent").call(payload).await()
    }

    suspend fun deleteHorseHealthEvent(id: String, horseId: String) {
        functions.getHttpsCallable("deleteHorseHealthEvent")
            .call(hashMapOf("id" to id, "horseId" to horseId)).await()
    }

    // ─── Safety ──────────────────────────────────────────────────────────────

    suspend fun getSafetySettings(): com.horsegallop.data.remote.dto.SafetySettingsFunctionsDto {
        val result = functions.getHttpsCallable("getSafetySettings").call().await()
        val map = result.data as? Map<*, *>
            ?: return com.horsegallop.data.remote.dto.SafetySettingsFunctionsDto()
        val rawContacts = map["contacts"] as? List<*> ?: emptyList<Any>()
        val contacts = rawContacts.mapNotNull { item ->
            (item as? Map<*, *>)?.let { m ->
                com.horsegallop.data.remote.dto.SafetyContactFunctionsDto(
                    id = (m["id"] as? String).orEmpty(),
                    name = (m["name"] as? String).orEmpty(),
                    phone = (m["phone"] as? String).orEmpty()
                )
            }
        }
        return com.horsegallop.data.remote.dto.SafetySettingsFunctionsDto(
            isEnabled = (map["isEnabled"] as? Boolean) ?: false,
            contacts = contacts,
            autoAlarmMinutes = (map["autoAlarmMinutes"] as? Number)?.toInt() ?: 5
        )
    }

    suspend fun updateSafetyEnabled(isEnabled: Boolean) {
        functions.getHttpsCallable("updateSafetySettings")
            .call(hashMapOf("isEnabled" to isEnabled)).await()
    }

    suspend fun addSafetyContact(
        name: String,
        phone: String
    ): com.horsegallop.data.remote.dto.SafetyContactFunctionsDto {
        val result = functions.getHttpsCallable("addSafetyContact")
            .call(hashMapOf("name" to name, "phone" to phone)).await()
        val map = result.data as? Map<*, *> ?: throw IllegalStateException("Invalid response")
        return com.horsegallop.data.remote.dto.SafetyContactFunctionsDto(
            id = (map["id"] as? String) ?: throw IllegalStateException("Missing id"),
            name = (map["name"] as? String) ?: name,
            phone = (map["phone"] as? String) ?: phone
        )
    }

    suspend fun removeSafetyContact(contactId: String) {
        functions.getHttpsCallable("removeSafetyContact")
            .call(hashMapOf("contactId" to contactId)).await()
    }

    suspend fun triggerSafetyAlarm(lat: Double, lng: Double) {
        functions.getHttpsCallable("triggerSafetyAlarm")
            .call(hashMapOf("lat" to lat, "lng" to lng)).await()
    }

    suspend fun getEquestrianAnnouncements(): List<com.horsegallop.data.remote.dto.EquestrianAnnouncementFunctionsDto> {
        val result = functions.getHttpsCallable("getEquestrianAnnouncements").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            com.horsegallop.data.remote.dto.EquestrianAnnouncementFunctionsDto(
                id = (m["id"] as? String).orEmpty(),
                title = (m["title"] as? String).orEmpty(),
                summary = (m["summary"] as? String).orEmpty(),
                publishedAtLabel = (m["publishedAtLabel"] as? String).orEmpty(),
                detailUrl = (m["detailUrl"] as? String).orEmpty(),
                imageUrl = m["imageUrl"] as? String
            )
        }
    }

    suspend fun getEquestrianCompetitions(): List<com.horsegallop.data.remote.dto.EquestrianCompetitionFunctionsDto> {
        val result = functions.getHttpsCallable("getEquestrianCompetitions").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val m = item as? Map<*, *> ?: return@mapNotNull null
            com.horsegallop.data.remote.dto.EquestrianCompetitionFunctionsDto(
                id = (m["id"] as? String).orEmpty(),
                title = (m["title"] as? String).orEmpty(),
                location = (m["location"] as? String).orEmpty(),
                dateLabel = (m["dateLabel"] as? String).orEmpty(),
                detailUrl = (m["detailUrl"] as? String).orEmpty()
            )
        }
    }
}
