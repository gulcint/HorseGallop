package com.horsegallop.data.di

import com.horsegallop.data.auth.FirebaseAuthRepository
import com.horsegallop.data.barnmanagement.repository.BarnManagementRepositoryImpl
import com.horsegallop.domain.barnmanagement.repository.BarnManagementRepository
import com.horsegallop.data.challenge.repository.ChallengeRepositoryImpl
import com.horsegallop.data.health.repository.HealthRepositoryImpl
import com.horsegallop.domain.challenge.repository.ChallengeRepository
import com.horsegallop.domain.health.repository.HealthRepository
import com.horsegallop.data.horse.repository.HorseHealthRepositoryImpl
import com.horsegallop.data.horse.repository.HorseRepositoryImpl
import com.horsegallop.data.safety.repository.SafetyRepositoryImpl
import com.horsegallop.domain.safety.repository.SafetyRepository
import com.horsegallop.data.notification.repository.NotificationRepositoryImpl
import com.horsegallop.data.settings.repository.UserSettingsRepositoryImpl
import com.horsegallop.data.review.repository.ReviewRepositoryImpl
import com.horsegallop.domain.horse.repository.HorseHealthRepository
import com.horsegallop.domain.horse.repository.HorseRepository
import com.horsegallop.domain.notification.repository.NotificationRepository
import com.horsegallop.domain.settings.repository.UserSettingsRepository
import com.horsegallop.domain.review.repository.ReviewRepository
import com.horsegallop.data.auth.repository.ProfileRepositoryImpl
import com.horsegallop.data.barn.repository.BarnRepositoryImpl
import com.horsegallop.data.equestrian.repository.EquestrianAgendaRepositoryImpl
import com.horsegallop.data.ride.repository.RideRepositoryImpl
import com.horsegallop.data.ride.repository.RideHistoryRepositoryImpl
import com.horsegallop.data.schedule.repository.ScheduleRepositoryImpl
import com.horsegallop.data.subscription.repository.SubscriptionRepositoryImpl
import com.horsegallop.data.training.repository.TrainingRepositoryImpl
import com.horsegallop.data.privacy.repository.PrivacyRepositoryImpl
import com.horsegallop.domain.barn.repository.BarnRepository
import com.horsegallop.domain.equestrian.repository.EquestrianAgendaRepository
import com.horsegallop.domain.ride.repository.RideRepository
import com.horsegallop.domain.ride.repository.RideHistoryRepository
import com.horsegallop.domain.schedule.repository.ScheduleRepository
import com.horsegallop.data.home.repository.HomeRepositoryImpl
import com.horsegallop.data.content.repository.ContentRepositoryImpl
import com.horsegallop.domain.auth.repository.ProfileRepository
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.home.repository.HomeRepository
import com.horsegallop.domain.content.repository.ContentRepository
import com.horsegallop.domain.privacy.repository.PrivacyRepository
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import com.horsegallop.domain.training.repository.TrainingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository

    @Binds
    @Singleton
    abstract fun bindBarnRepository(
        barnRepositoryImpl: BarnRepositoryImpl
    ): BarnRepository

    @Binds
    @Singleton
    abstract fun bindRideRepository(
        rideRepositoryImpl: RideRepositoryImpl
    ): RideRepository

    @Binds
    @Singleton
    abstract fun bindRideHistoryRepository(
        rideHistoryRepositoryImpl: RideHistoryRepositoryImpl
    ): RideHistoryRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        scheduleRepositoryImpl: ScheduleRepositoryImpl
    ): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindPrivacyRepository(
        privacyRepositoryImpl: PrivacyRepositoryImpl
    ): PrivacyRepository

    @Binds
    @Singleton
    abstract fun bindTrainingRepository(
        trainingRepositoryImpl: TrainingRepositoryImpl
    ): TrainingRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindHorseRepository(
        horseRepositoryImpl: HorseRepositoryImpl
    ): HorseRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(
        userSettingsRepositoryImpl: UserSettingsRepositoryImpl
    ): UserSettingsRepository

    @Binds
    @Singleton
    abstract fun bindHorseHealthRepository(
        horseHealthRepositoryImpl: HorseHealthRepositoryImpl
    ): HorseHealthRepository

    @Binds
    @Singleton
    abstract fun bindSafetyRepository(
        safetyRepositoryImpl: SafetyRepositoryImpl
    ): SafetyRepository

    @Binds
    @Singleton
    abstract fun bindEquestrianAgendaRepository(
        equestrianAgendaRepositoryImpl: EquestrianAgendaRepositoryImpl
    ): EquestrianAgendaRepository

    @Binds
    @Singleton
    abstract fun bindHealthRepository(
        healthRepositoryImpl: HealthRepositoryImpl
    ): HealthRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository

    @Binds
    @Singleton
    abstract fun bindBarnManagementRepository(
        barnManagementRepositoryImpl: BarnManagementRepositoryImpl
    ): BarnManagementRepository
}
