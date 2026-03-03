package com.horsegallop.data.remote.functions

import com.google.firebase.functions.FirebaseFunctions
import com.horsegallop.data.remote.dto.BarnFunctionsDto
import com.horsegallop.data.remote.dto.AppContentFunctionsDto
import com.horsegallop.data.remote.dto.HomeDashboardFunctionsDto
import com.horsegallop.data.remote.dto.HomeRecentActivityFunctionsDto
import com.horsegallop.data.remote.dto.HomeStatsFunctionsDto
import com.horsegallop.data.remote.dto.LessonFunctionsDto
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

    suspend fun getBarns(): List<BarnFunctionsDto> {
        val result = functions.getHttpsCallable("getBarns").call().await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        val items = payload["items"] as? List<*> ?: emptyList<Any?>()
        return items.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            mapBarn(map)
        }
    }

    suspend fun getBarnDetail(id: String): BarnFunctionsDto {
        val result = functions.getHttpsCallable("getBarnDetail").call(hashMapOf("id" to id)).await()
        val payload = result.data as? Map<*, *> ?: emptyMap<String, Any?>()
        return mapBarn(payload) ?: throw IllegalStateException("Invalid barn payload")
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
                price = (map["price"] as? Number)?.toDouble() ?: 0.0
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

    private fun mapBarn(map: Map<*, *>): BarnFunctionsDto? {
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
            reviewCount = (map["reviewCount"] as? Number)?.toInt() ?: 0
        )
    }
}
