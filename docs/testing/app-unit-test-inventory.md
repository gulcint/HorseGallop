# App Unit Test Method Inventory

Bu dosya, `horsegallop/app` tarafinda unit test yazilmasi gereken concrete logic method envanteridir.

## Ozet

- Toplam method: `346`
- Domain: `83`
- Data: `145`
- ViewModel: `116`
- Utility: `2`
- 2026-03-17 audit sonucu: `app/src/test` ana davranis katmani olarak korundu, `app/src/androidTest` ince smoke katmanina indirildi
- Dusuk sinyalli temizleme:
  - silindi: `ProfileViewModelTest.privateWeightHelpers_parseFormatAndMapErrors_behaveAsExpected`
  - azaltildi: `AuthSmokeTest`, `ProfileScreenTest`, `RideTrackingScreenTest`
  - tekillestirildi: test icindeki kopya `MainDispatcherRule` implementasyonlari, ortak `TestMainDispatcherRule` kullanimina cekildi

## Disarida Birakilanlar

- `ApiService.kt`
- `data/di/*`
- interface-only repository dosyalari
- `*Screen.kt`
- saf UI component/helper dosyalari

## Yuksek Degerli Ilk Dalga

Bu bolum, tum envanter icinden ilk once unit test yazilmasi gereken methodlari ayirir. Secim kriteri:
- auth, ride, health, schedule gibi cekirdek akislar
- branch/fallback/state transition iceren methodlar
- mapping ve remote boundary kirilmalarini erken yakalayan methodlar
- retry, timeout, validation ve orchestration davranisi olan methodlar

## 2026-03-17 Karar Tablosu

Bu tablo post-audit durumunu ozetler. Status anlamlari:
- `covered well`: davranis odakli test var ve korunuyor
- `covered weakly`: test var ama daha cok implementation detail veya UI callback seviyesinde
- `intentionally deferred`: bu dalgada daha derin test eklenmeyecek

| Alan | Status | Not |
| --- | --- | --- |
| `domain/auth/AuthValidator.kt` | covered well | JVM unit testleri korunuyor |
| `data/auth/FirebaseAuthRepository.kt` | covered well | cekirdek auth akislarini kapsiyor |
| `feature/auth/presentation/LoginViewModel.kt` | covered well | durum gecisleri ve effectler kapsaniyor |
| `feature/auth/presentation/ProfileViewModel.kt` | covered well | public davranis testleri korunuyor; private helper reflection testi kaldirildi |
| `feature/settings/presentation/SettingsViewModel.kt` | covered well | hata/sync akis testleri korunuyor |
| `data/ride/repository/RideRepositoryImpl.kt` | covered well | ride baslat/bitir/sync davranislari korunuyor |
| `data/ride/repository/RideStopSyncOrchestrator.kt` | covered well | retry ve queue davranisi korunuyor |
| `data/ride/repository/RideSyncOutboxStore.kt` | covered well | outbox yasam dongusu korunuyor |
| `feature/ride/presentation/RideTrackingViewModel.kt` | covered well | cekirdek state transition testleri korunuyor |
| `feature/ride/presentation/RideRouteSegments.kt` | covered well | route presentation mantigi korunuyor |
| `feature/ride/presentation/GaitUtils.kt` | intentionally deferred | bu envanterde yuksek degerli olarak listeli, fakat app test suite icinde ayrica audit edilmedi |
| `data/health/repository/HealthRepositoryImpl.kt` | covered well | repository davranisi korunuyor |
| `data/horse/repository/HorseRepositoryImpl.kt` | covered well | add/delete/map akis testi korunuyor |
| `data/horse/repository/HorseHealthRepositoryImpl.kt` | covered well | event CRUD davranisi korunuyor |
| `data/schedule/repository/ScheduleRepositoryImpl.kt` | covered well | booking/cancel akis testi korunuyor |
| `feature/health/presentation/HealthViewModel.kt` | covered well | state ve mutation akis testi korunuyor |
| `feature/horse/presentation/HorseViewModel.kt` | covered well | ana davranis korunuyor |
| `feature/horse/presentation/HorseHealthViewModel.kt` | covered well | event ve hesaplama akis testi korunuyor |
| `feature/schedule/presentation/ScheduleViewModel.kt` | covered well | refresh/book/cancel akis testi korunuyor |
| `data/remote/functions/AppFunctionsDataSource.kt` | covered well | mapping ve functions boundary testleri korunuyor |
| `data/auth/repository/ProfileRepositoryImpl.kt` | covered well | profile payload/map davranisi korunuyor |
| `data/home/repository/HomeRepositoryImpl.kt` | covered well | parse ve dashboard mapping korunuyor |
| `data/barn/repository/BarnRepositoryImpl.kt` | covered well | barn fetch/favorite akis testi korunuyor |
| `data/review/repository/ReviewRepositoryImpl.kt` | covered well | review submit/fetch korunuyor |
| `data/subscription/repository/SubscriptionRepositoryImpl.kt` | covered well | entitlement restore/refresh korunuyor |
| `data/notification/repository/NotificationRepositoryImpl.kt` | covered well | read / markAll davranisi korunuyor |
| `data/settings/repository/UserSettingsRepositoryImpl.kt` | covered well | settings read/write korunuyor |
| `data/training/repository/TrainingRepositoryImpl.kt` | covered well | training plan ve task complete korunuyor |
| `app/src/androidTest` smoke layer | covered weakly | callback matrisleri budandi; sadece kritik ekran ulasilabilirlik ve ana aksiyon smoke testleri tutuldu |

### `domain/auth/AuthValidator.kt`

- Yuksek degerli methods: `validateName, validateEmail, calculatePasswordStrength, isPasswordStrongEnough`
- ✅ testi yazildi: `validateName, validateEmail, calculatePasswordStrength, isPasswordStrongEnough`

### `data/auth/FirebaseAuthRepository.kt`

- Yuksek degerli methods: `signInWithGoogleIdToken, signUpWithEmail, signInWithEmail, resendVerificationEmail, checkEmailVerified, saveUserToRemote, getSplashTexts, fetchOrCreateUser, fetchUserFromFirestore, createDefaultUser, createFirestoreUser`
- ✅ testi yazildi: `signInWithGoogleIdToken, signUpWithEmail, signInWithEmail, resendVerificationEmail, checkEmailVerified, saveUserToRemote, getSplashTexts, fetchOrCreateUser, fetchUserFromFirestore, createDefaultUser, createFirestoreUser`

### `feature/auth/presentation/LoginViewModel.kt`

- Yuksek degerli methods: `login, resendVerification, loginWithGoogle, validateForm, onSignInCancelled, onGoogleSignInError`
- ✅ testi yazildi: `login, resendVerification, loginWithGoogle, validateForm, onSignInCancelled, onGoogleSignInError`

### `feature/auth/presentation/ProfileViewModel.kt`

- Yuksek degerli methods: `loadProfile, startEditSession, discardEditSession, updateDraft, saveProfile, updateProfileImage, validateDraft, parseWeightInput, formatWeightInput, mapProfileErrorRes`
- ✅ testi yazildi: `loadProfile, startEditSession, discardEditSession, updateDraft, saveProfile, updateProfileImage, validateDraft, parseWeightInput, formatWeightInput, mapProfileErrorRes`

### `feature/settings/presentation/SettingsViewModel.kt`

- Yuksek degerli methods: `onThemeSelected, onLanguageSelected, onNotificationsChanged, syncSettingsFromBackend, syncSettingToBackend, requestDataExport, requestAccountDeletion, loadContent`
- ✅ testi yazildi: `onThemeSelected, onLanguageSelected, onNotificationsChanged, syncSettingsFromBackend, syncSettingToBackend, requestDataExport, requestAccountDeletion, loadContent`

### `data/ride/repository/RideRepositoryImpl.kt`

- Yuksek degerli methods: `startRide, stopRide, retryPendingRideSync, setAutoDetect, normalizeRideType, downsamplePath`
- ✅ testi yazildi: `startRide, stopRide, retryPendingRideSync, setAutoDetect, normalizeRideType, downsamplePath`

### `data/ride/repository/RideStopSyncOrchestrator.kt`

- Yuksek degerli methods: `syncStopOrQueue, retryDuePendingSync, retryDelayMillis`
- ✅ testi yazildi: `syncStopOrQueue, retryDuePendingSync, retryDelayMillis`

### `data/ride/repository/RideSyncOutboxStore.kt`

- Yuksek degerli methods: `append, listDue, markSynced, rescheduleFailed, purgeExpired`
- ✅ testi yazildi: `append, listDue, markSynced, rescheduleFailed, purgeExpired`

### `feature/ride/presentation/RideTrackingViewModel.kt`

- Yuksek degerli methods: `confirmAutoStop, onRideTypeSelected, onBarnSelected, onToggleRide, onSetAutoDetect, onRetryPendingSync, startRide, finishRide, calculateAverageSpeed`
- ✅ testi yazildi: `confirmAutoStop, onRideTypeSelected, onBarnSelected, onToggleRide, onSetAutoDetect, onRetryPendingSync, startRide, finishRide, calculateAverageSpeed`

### `feature/ride/presentation/RideRouteSegments.kt`

- Yuksek degerli methods: `buildRideRoutePresentation`
- ✅ testi yazildi: `buildRideRoutePresentation`

### `feature/ride/presentation/GaitUtils.kt`

- Yuksek degerli methods: `buildGaitSegments`
- ✅ testi yazildi: `buildGaitSegments`

### `data/health/repository/HealthRepositoryImpl.kt`

- Yuksek degerli methods: `getHealthEvents, saveHealthEvent, deleteHealthEvent, markCompleted`
- ✅ testi yazildi: `getHealthEvents, saveHealthEvent, deleteHealthEvent, markCompleted`

### `data/horse/repository/HorseRepositoryImpl.kt`

- Yuksek degerli methods: `getMyHorses, addHorse, deleteHorse, getBreeds, getHorseTips, toDomain`
- ✅ testi yazildi: `getMyHorses, addHorse, deleteHorse, getBreeds, getHorseTips, toDomain`

### `data/horse/repository/HorseHealthRepositoryImpl.kt`

- Yuksek degerli methods: `getHealthEvents, addHealthEvent, updateHealthEvent, deleteHealthEvent`
- ✅ testi yazildi: `getHealthEvents, addHealthEvent, updateHealthEvent, deleteHealthEvent`

### `data/schedule/repository/ScheduleRepositoryImpl.kt`

- Yuksek degerli methods: `getLessons, bookLesson, cancelReservation, getMyReservations, toDomain`
- ✅ testi yazildi: `getLessons, bookLesson, cancelReservation, getMyReservations, toDomain`

### `feature/health/presentation/HealthViewModel.kt`

- Yuksek degerli methods: `load, filterByHorse, saveEvent, markCompleted, delete`
- ✅ testi yazildi: `load, filterByHorse, saveEvent, markCompleted, delete`

### `feature/horse/presentation/HorseViewModel.kt`

- Yuksek degerli methods: `addHorse, deleteHorse`
- ✅ testi yazildi: `addHorse, deleteHorse`

### `feature/horse/presentation/HorseHealthViewModel.kt`

- Yuksek degerli methods: `load, addEvent, deleteEvent, updateEvents, daysUntil`
- ✅ testi yazildi: `load, addEvent, deleteEvent, updateEvents, daysUntil`

### `feature/schedule/presentation/ScheduleViewModel.kt`

- Yuksek degerli methods: `refresh, loadReservations, bookLesson, cancelReservation`
- ✅ testi yazildi: `refresh, loadReservations, bookLesson, cancelReservation`

### `data/remote/functions/AppFunctionsDataSource.kt`

- Yuksek degerli methods: `mapReservation, mapBarn, mapManagedLesson, getMyReservations, getLessons, getMyHorses, getHealthEvents, getHorseHealthEvents, getBarnStats, getManagedLessons, getLessonRoster, getMyRides, verifyPurchase, getSubscriptionStatus`
- ✅ testi yazildi: `mapReservation, mapBarn, mapManagedLesson, getMyReservations, getLessons, getMyHorses, getHealthEvents, getHorseHealthEvents, getBarnStats, getManagedLessons, getLessonRoster, getMyRides, verifyPurchase, getSubscriptionStatus`

### `data/auth/repository/ProfileRepositoryImpl.kt`

- Yuksek degerli methods: `getUserProfile, updateUserProfile, updateProfileImage, mapPayloadToProfile`
- ✅ testi yazildi: `getUserProfile, updateUserProfile, updateProfileImage, mapPayloadToProfile`

### `data/home/repository/HomeRepositoryImpl.kt`

- Yuksek degerli methods: `getRecentActivities, getUserStats, parseDate`
- ✅ testi yazildi: `getRecentActivities, getUserStats, parseDate`

### `data/barn/repository/BarnRepositoryImpl.kt`

- Yuksek degerli methods: `getBarns, getBarnById, toggleFavorite`
- ✅ testi yazildi: `getBarns, getBarnById, toggleFavorite`

### `data/review/repository/ReviewRepositoryImpl.kt`

- Yuksek degerli methods: `getMyReviews, submitReview`
- ✅ testi yazildi: `getMyReviews, submitReview`

### `data/subscription/repository/SubscriptionRepositoryImpl.kt`

- Yuksek degerli methods: `refreshEntitlements, restorePurchases`
- ✅ testi yazildi: `refreshEntitlements, restorePurchases`

### `data/notification/repository/NotificationRepositoryImpl.kt`

- Yuksek degerli methods: `getNotifications, markAsRead, markAllAsRead`
- ✅ testi yazildi: `getNotifications, markAsRead, markAllAsRead`

### `data/settings/repository/UserSettingsRepositoryImpl.kt`

- Yuksek degerli methods: `getUserSettings, updateUserSettings`
- ✅ testi yazildi: `getUserSettings, updateUserSettings`

### `data/training/repository/TrainingRepositoryImpl.kt`

- Yuksek degerli methods: `observeTrainingPlans, getTrainingPlans, completeTrainingTask`
- ✅ testi yazildi: `observeTrainingPlans, getTrainingPlans, completeTrainingTask`

## Domain

### `domain/aicoach/usecase/AskAiCoachUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/AuthValidator.kt`

- Method count: `4`
- Methods: `validateName, validateEmail, calculatePasswordStrength, isPasswordStrongEnough`

### `domain/auth/usecase/CheckEmailVerifiedUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/ConfirmPasswordResetUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/DeleteAccountUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/GetCurrentUserIdUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/GetLottieConfigUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/GetUserProfileUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/ResendVerificationEmailUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/ResetPasswordUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/SaveUserToRemoteUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/SendPasswordResetEmailUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/SignInWithEmailUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/SignInWithGoogleUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/SignOutUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/SignUpWithEmailUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/auth/usecase/UpdateProfileImageUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/auth/usecase/UpdateUserProfileUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barn/usecase/GetBarnDetailUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barn/usecase/GetBarnsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barn/usecase/ToggleBarnFavoriteUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barnmanagement/usecase/CancelLessonUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barnmanagement/usecase/CreateLessonUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barnmanagement/usecase/GetBarnStatsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barnmanagement/usecase/GetLessonRosterUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/barnmanagement/usecase/GetManagedLessonsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/challenge/usecase/ClaimBadgeUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/challenge/usecase/GetActiveChallengesUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/challenge/usecase/GetEarnedBadgesUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/content/usecase/GetAppContentUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/equestrian/usecase/GetEquestrianAnnouncementsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/equestrian/usecase/GetEquestrianCompetitionsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/equestrian/usecase/GetFederatedBarnSyncStatusUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/equestrian/usecase/GetFederationSourceHealthUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/equestrian/usecase/TriggerFederationManualSyncUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/health/usecase/DeleteHealthEventUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/health/usecase/GetHealthEventsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/health/usecase/SaveHealthEventUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/home/usecase/GetRecentActivitiesUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/home/usecase/GetUserStatsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/model/HorseHealthEvent.kt`

- Method count: `1`
- Methods: `fromString`

### `domain/horse/usecase/AddHorseHealthEventUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/AddHorseUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/DeleteHorseHealthEventUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/DeleteHorseUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/GetBreedsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/GetHorseHealthEventsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/GetHorseTipsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/horse/usecase/GetMyHorsesUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/notification/usecase/GetNotificationsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/notification/usecase/MarkAllNotificationsReadUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/notification/usecase/MarkNotificationReadUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/privacy/usecase/DeleteUserDataUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/privacy/usecase/RequestDataExportUseCase.kt`

- Method count: `1`
- Methods: `execute`

### `domain/review/usecase/GetMyReviewsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/review/usecase/SubmitReviewUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/ObserveAutoStopSignalUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/ObserveIsRidingUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/ObservePendingRideSyncCountUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/ObserveRideMetricsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/RetryPendingRideSyncUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/SetAutoDetectUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/StartRideUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/usecase/StopRideUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/ride/util/GaitThresholds.kt`

- Method count: `1`
- Methods: `gaitOf`

### `domain/schedule/usecase/BookLessonUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/schedule/usecase/CancelReservationUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/schedule/usecase/GetLessonsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/schedule/usecase/GetMyReservationsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/settings/usecase/GetUserSettingsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/settings/usecase/UpdateUserSettingsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/subscription/model/SubscriptionStatus.kt`

- Method count: `1`
- Methods: `SubscriptionStatus`

### `domain/subscription/usecase/ObserveSubscriptionStatusUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/subscription/usecase/RestorePurchasesUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/subscription/usecase/StartSubscriptionPurchaseUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/tbf/usecase/GetTbfEventCardUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/tbf/usecase/GetTbfEventDayUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/tbf/usecase/GetTbfUpcomingEventsUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/training/usecase/CompleteTrainingTaskUseCase.kt`

- Method count: `1`
- Methods: `invoke`

### `domain/training/usecase/GetTrainingPlansUseCase.kt`

- Method count: `1`
- Methods: `invoke`

## Data

### `data/aicoach/repository/AiCoachRepositoryImpl.kt`

- Method count: `2`
- Methods: `ask, getConversationHistory`

### `data/auth/FirebaseAuthRepository.kt`

- Method count: `14`
- Methods: `signInWithGoogleIdToken, isSignedIn, signOut, signUpWithEmail, signInWithEmail, sendPasswordResetEmail, confirmPasswordReset, resendVerificationEmail, checkEmailVerified, saveUserToRemote, getLottieConfig, getSplashTexts, resolveField, getCurrentUserId`

### `data/auth/repository/ProfileRepositoryImpl.kt`

- Method count: `5`
- Methods: `getUserProfile, updateUserProfile, updateProfileImage, deleteAccount, signOut`

### `data/barn/repository/BarnRepositoryImpl.kt`

- Method count: `3`
- Methods: `getBarns, getBarnById, toggleFavorite`

### `data/barnmanagement/repository/BarnManagementRepositoryImpl.kt`

- Method count: `5`
- Methods: `getBarnStats, getManagedLessons, createLesson, cancelLesson, getLessonRoster`

### `data/billing/BillingManager.kt`

- Method count: `4`
- Methods: `onBillingSetupFinished, onBillingServiceDisconnected, launchBillingFlow, queryActivePurchases`

### `data/challenge/repository/ChallengeRepositoryImpl.kt`

- Method count: `3`
- Methods: `getActiveChallenges, getEarnedBadges, claimBadge`

### `data/content/repository/ContentRepositoryImpl.kt`

- Method count: `1`
- Methods: `getAppContent`

### `data/equestrian/repository/EquestrianAgendaRepositoryImpl.kt`

- Method count: `5`
- Methods: `getAnnouncements, getCompetitions, getFederatedBarnSyncStatus, getFederationSourceHealth, triggerManualSync`

### `data/health/repository/HealthRepositoryImpl.kt`

- Method count: `4`
- Methods: `getHealthEvents, saveHealthEvent, deleteHealthEvent, markCompleted`

### `data/home/repository/HomeRepositoryImpl.kt`

- Method count: `2`
- Methods: `getRecentActivities, getUserStats`

### `data/horse/repository/HorseHealthRepositoryImpl.kt`

- Method count: `4`
- Methods: `getHealthEvents, addHealthEvent, updateHealthEvent, deleteHealthEvent`

### `data/horse/repository/HorseRepositoryImpl.kt`

- Method count: `5`
- Methods: `getMyHorses, addHorse, deleteHorse, getBreeds, getHorseTips`

### `data/notification/repository/NotificationRepositoryImpl.kt`

- Method count: `3`
- Methods: `getNotifications, markAsRead, markAllAsRead`

### `data/privacy/repository/PrivacyRepositoryImpl.kt`

- Method count: `2`
- Methods: `exportUserData, deleteUserData`

### `data/remote/AuthTokenInterceptor.kt`

- Method count: `1`
- Methods: `intercept`

### `data/remote/LanguageInterceptor.kt`

- Method count: `1`
- Methods: `intercept`

### `data/remote/functions/AppFunctionsDataSource.kt`

- Method count: `47`
- Methods: `getHomeDashboard, getBarns, getBarnDetail, getFederatedBarnsSyncStatus, triggerFederationManualSync, triggerFederationDebugSync, getFederationSourceHealth, getLessons, getHorseTips, getBreeds, getAppContent, submitReview, getMyReviews, getMyHorses, addHorse, deleteHorse, bookLesson, cancelReservation, getMyReservations, getUserSettings, updateUserSettings, getHorseHealthEvents, addHorseHealthEvent, updateHorseHealthEvent, deleteHorseHealthEvent, getEquestrianAnnouncements, getHealthEvents, saveHealthEvent, deleteHealthEvent, markHealthEventCompleted, getEquestrianCompetitions, askAiCoach, getActiveChallenges, getEarnedBadges, checkAndAwardBadges, getTbfEventDay, getTbfEventCard, getTbfUpcomingEvents, getBarnStats, getManagedLessons, createLesson, cancelLesson, getLessonRoster, saveRide, getMyRides, verifyPurchase, getSubscriptionStatus`

### `data/review/repository/ReviewRepositoryImpl.kt`

- Method count: `2`
- Methods: `getMyReviews, submitReview`

### `data/ride/repository/RideHistoryRepositoryImpl.kt`

- Method count: `3`
- Methods: `getRideHistory, getRide, saveRide`

### `data/ride/repository/RideRepositoryImpl.kt`

- Method count: `5`
- Methods: `startRide, stopRide, retryPendingRideSync, setAutoDetect, onLocationResult`

### `data/ride/repository/RideStopSyncOrchestrator.kt`

- Method count: `2`
- Methods: `syncStopOrQueue, retryDuePendingSync`

### `data/ride/repository/RideSyncOutboxStore.kt`

- Method count: `5`
- Methods: `append, listDue, markSynced, rescheduleFailed, purgeExpired`

### `data/schedule/repository/ScheduleRepositoryImpl.kt`

- Method count: `4`
- Methods: `getLessons, bookLesson, cancelReservation, getMyReservations`

### `data/settings/repository/UserSettingsRepositoryImpl.kt`

- Method count: `2`
- Methods: `getUserSettings, updateUserSettings`

### `data/subscription/repository/SubscriptionRepositoryImpl.kt`

- Method count: `5`
- Methods: `observeSubscriptionStatus, getSubscriptionStatus, startSubscriptionPurchase, refreshEntitlements, restorePurchases`

### `data/tbf/repository/TbfRepositoryImpl.kt`

- Method count: `3`
- Methods: `getEventDay, getEventCard, getUpcomingEvents`

### `data/training/repository/TrainingRepositoryImpl.kt`

- Method count: `3`
- Methods: `observeTrainingPlans, getTrainingPlans, completeTrainingTask`

## ViewModel

### `feature/aicoach/presentation/AiCoachViewModel.kt`

- Method count: `3`
- Methods: `onInputChange, sendMessage, clearError`

### `feature/auth/presentation/AuthViewModel.kt`

- Method count: `3`
- Methods: `signInWithGoogleIdToken, isSignedIn, signOut`

### `feature/auth/presentation/EnrollmentViewModel.kt`

- Method count: `12`
- Methods: `updateFirstName, updateLastName, updateEmail, updatePassword, handleDeepLink, setBirthDate, setShowDatePicker, signUp, resendVerificationEmail, dismissVerificationResult, loadLottieConfig, checkEmailVerified`

### `feature/auth/presentation/ForgotPasswordViewModel.kt`

- Method count: `6`
- Methods: `updateEmail, updateNewPassword, updateConfirmPassword, sendResetLink, handleDeepLink, confirmReset`

### `feature/auth/presentation/LoginViewModel.kt`

- Method count: `10`
- Methods: `updateEmail, updatePassword, togglePasswordVisibility, login, resendVerification, resetPassword, toggleAgreement, onSignInCancelled, loginWithGoogle, onGoogleSignInError`

### `feature/auth/presentation/ProfileViewModel.kt`

- Method count: `8`
- Methods: `loadProfile, startEditSession, discardEditSession, updateDraft, saveProfile, updateProfileImage, clearMessages, signOut`

### `feature/barn/presentation/BarnDetailViewModel.kt`

- Method count: `3`
- Methods: `refresh, bookLesson, clearBookingResult`

### `feature/barn/presentation/BarnViewModel.kt`

- Method count: `6`
- Methods: `loadBarns, toggleFavorite, updateQuery, toggleFilter, clearFilters, clearQuery`

### `feature/barnmanagement/presentation/BarnDashboardViewModel.kt`

- Method count: `3`
- Methods: `loadData, cancelLesson, clearError`

### `feature/barnmanagement/presentation/CreateLessonViewModel.kt`

- Method count: `2`
- Methods: `createLesson, clearError`

### `feature/barnmanagement/presentation/LessonRosterViewModel.kt`

- Method count: `1`
- Methods: `loadRoster`

### `feature/challenge/presentation/ChallengeViewModel.kt`

- Method count: `1`
- Methods: `clearError`

### `feature/equestrian/presentation/EquestrianAgendaViewModel.kt`

- Method count: `7`
- Methods: `selectTab, refresh, showAnnouncementPreview, showCompetitionPreview, dismissPreview, triggerManualSync, triggerDebugSync`

### `feature/health/presentation/HealthViewModel.kt`

- Method count: `6`
- Methods: `load, filterByHorse, saveEvent, markCompleted, delete, clearError`

### `feature/home/presentation/HomeViewModel.kt`

- Method count: `2`
- Methods: `refresh, loadRecentActivities`

### `feature/horse/presentation/HorseHealthViewModel.kt`

- Method count: `5`
- Methods: `load, addEvent, deleteEvent, clearError, HorseHealthEvent`

### `feature/horse/presentation/HorseViewModel.kt`

- Method count: `3`
- Methods: `addHorse, deleteHorse, clearSaveState`

### `feature/notifications/presentation/NotificationsViewModel.kt`

- Method count: `2`
- Methods: `onNotificationTap, markAllRead`

### `feature/review/presentation/ReviewViewModel.kt`

- Method count: `2`
- Methods: `submitReview, clearSubmitState`

### `feature/ride/presentation/RideTrackingViewModel.kt`

- Method count: `9`
- Methods: `dismissAutoStopDialog, confirmAutoStop, onRideTypeSelected, onBarnSelected, onToggleRide, onSetAutoDetect, clearError, dismissSavedSummary, onRetryPendingSync`

### `feature/schedule/presentation/ScheduleViewModel.kt`

- Method count: `4`
- Methods: `refresh, bookLesson, cancelReservation, clearBookingState`

### `feature/settings/presentation/SettingsViewModel.kt`

- Method count: `9`
- Methods: `onThemeSelected, onLanguageSelected, onNotificationsChanged, requestDataExport, requestAccountDeletion, consumeExport, clearPrivacyError, consumeRemoteError, consumeSaveError`

### `feature/subscription/presentation/SubscriptionViewModel.kt`

- Method count: `4`
- Methods: `selectPlan, purchase, restorePurchases, clearError`

### `feature/tbf/presentation/TbfViewModel.kt`

- Method count: `2`
- Methods: `selectVenue, switchMode`

### `feature/training/presentation/TrainingPlansViewModel.kt`

- Method count: `3`
- Methods: `refresh, completeTask, clearError`

## Utility

### `feature/ride/presentation/GaitUtils.kt`

- Method count: `1`
- Methods: `buildGaitSegments`

### `feature/ride/presentation/RideRouteSegments.kt`

- Method count: `1`
- Methods: `buildRideRoutePresentation`
