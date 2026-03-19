package com.horsegallop.data.content.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.domain.content.model.AppContent
import com.horsegallop.domain.content.repository.ContentRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ContentRepositoryImpl @Inject constructor(
    private val supabaseDataSource: SupabaseDataSource
) : ContentRepository {

    override fun getAppContent(locale: String): Flow<Result<AppContent>> = flow {
        try {
            val rows = supabaseDataSource.getAppContent(locale)
            val map = rows.associate { it.key to it.value }
            emit(
                Result.success(
                    AppContent(
                        locale = locale,
                        homeHeroTitle = map["home_hero_title"],
                        homeHeroSubtitle = map["home_hero_subtitle"],
                        offlineHelp = map["offline_help"],
                        loginTitle = map["login_title"],
                        loginSubtitle = map["login_subtitle"],
                        emailLoginTitle = map["email_login_title"],
                        emailLoginSubtitle = map["email_login_subtitle"],
                        enrollTitle = map["enroll_title"],
                        enrollSubtitle = map["enroll_subtitle"],
                        forgotPasswordSubtitle = map["forgot_password_subtitle"],
                        onboardingHeroTitle = map["onboarding_hero_title"],
                        onboardingHeroSubtitle = map["onboarding_hero_subtitle"],
                        onboardingHelpText = map["onboarding_help_text"],
                        rideLiveTitle = map["ride_live_title"],
                        rideLiveSubtitleIdle = map["ride_live_subtitle_idle"],
                        rideLiveSubtitleActive = map["ride_live_subtitle_active"],
                        ridePermissionTitle = map["ride_permission_title"],
                        ridePermissionHint = map["ride_permission_hint"],
                        rideGrantLocationCta = map["ride_grant_location_cta"],
                        settingsThemeSubtitle = map["settings_theme_subtitle"],
                        settingsLanguageSubtitle = map["settings_language_subtitle"],
                        settingsNotificationsSubtitle = map["settings_notifications_subtitle"],
                        settingsPrivacySubtitle = map["settings_privacy_subtitle"]
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
