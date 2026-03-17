# HorseGallop — Application Overview for TestSprite

## 1. Application Purpose & Vision

**HorseGallop** is a global equestrian mobile application for Android (Equilab competitor), targeting individual riders and farms/stables. It provides ride tracking with GPS and gait detection, barn discovery, lesson booking, horse health management, challenges/badges, AI coaching (Turkish Gemini 1.5 Flash), and equestrian event aggregation (TBF/Turkish federation).

- **Target Users:** Individual riders, farm owners (B2B), competitive riders
- **Market:** Global, Turkey-first (TBF horse racing integration)
- **Monetization:** Freemium — Pro subscription via Google Play Billing (`horsegallop_pro_monthly`, `horsegallop_pro_yearly`)
- **Package:** `com.horsegallop`
- **Min SDK:** 24 | Target SDK: 34 | JVM: 17

---

## 2. Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0.0 |
| UI | Jetpack Compose + Material3 |
| Architecture | Single-module monolith (`:app` only) |
| DI | Hilt 2.52 |
| Navigation | Navigation Compose 2.8.1 |
| Backend | Firebase Cloud Functions (Node.js/TypeScript, us-central1) |
| Auth | Firebase Auth (Email/Password + Google Sign-In) |
| Database | Firestore |
| Messaging | Firebase Cloud Messaging (FCM) |
| Storage | Firebase Storage |
| App Check | PlayIntegrity (release) / Debug (dev) |
| Maps | Google Maps Compose 4.3.0 |
| Images | Coil 2.6.0 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 + Moshi 1.15.1 |
| Animations | Lottie Compose 6.1.0 |
| Billing | Google Play Billing |
| AI | Gemini 1.5 Flash (via Cloud Function `askAiCoach`) |

---

## 3. Repository Structure

```
horsegallop/
├── app/                                  # Android application module
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── src/
│       ├── main/java/com/horsegallop/    # All Kotlin source
│       ├── main/res/                     # Strings, themes, drawables
│       ├── androidTest/                  # Instrumented UI tests
│       └── test/                         # Unit tests
├── backend/                              # Firebase Cloud Functions
│   ├── src/index.ts                      # 76+ exported functions (2664 lines)
│   └── package.json
├── gradle/libs.versions.toml             # Dependency version catalog
├── scripts/                              # pr-pipeline-merge.sh, setup-hooks.sh
├── docs/                                 # Project documentation
└── CLAUDE.md                             # Project instructions
```

---

## 4. Source Code Structure (`app/src/main/java/com/horsegallop/`)

```
com.horsegallop/
├── MainActivity.kt                        # Single activity, Compose entry point
├── MainViewModel.kt                       # App-level auth/role state
│
├── ui/theme/
│   ├── Color.kt                           # Brand palette: SaddleBrown(#8B4513), Chocolate(#D2691E), Cream(#F5E6D3)
│   ├── SemanticColors.kt                  # Design tokens (screenBase, cardElevated, success, warning, etc.)
│   ├── TextColors.kt
│   ├── Theme.kt                           # Material3 theme + dark mode
│   └── Type.kt                            # Typography
│
├── navigation/
│   └── AppNav.kt                          # Sealed class Dest + NavHost wiring
│
├── core/
│   ├── components/                        # Reusable Composables
│   │   ├── Buttons.kt
│   │   ├── Cards.kt
│   │   ├── ChipSelector.kt
│   │   ├── HorseLoadingOverlay.kt
│   │   ├── Inputs.kt
│   │   ├── ProGate.kt                     # Pro feature paywall gate
│   │   ├── QuickActionCard.kt
│   │   └── Skeletons.kt
│   ├── feedback/                          # Snackbar/error feedback system
│   │   ├── AppFeedbackController.kt
│   │   ├── FeedbackErrorMapper.kt
│   │   └── HorseGallopSnackbarHost.kt
│   ├── debug/AppLog.kt                    # Custom logger (replaces Log)
│   ├── error/GlobalExceptionHandler.kt
│   ├── localization/LocalizedContent.kt
│   └── util/
│       ├── Constants.kt
│       └── GeoUtils.kt
│
├── data/
│   ├── di/
│   │   ├── DataModule.kt                  # @Binds @Singleton repository bindings
│   │   ├── FirebaseModule.kt
│   │   └── NetworkModule.kt
│   ├── remote/
│   │   ├── functions/AppFunctionsDataSource.kt   # All Firebase CF calls
│   │   ├── dto/                           # FunctionsDtos.kt, RideDtos.kt, BarnDtos.kt, etc.
│   │   ├── ApiService.kt
│   │   ├── AuthTokenInterceptor.kt
│   │   └── LanguageInterceptor.kt
│   ├── billing/BillingManager.kt          # @Singleton Google Play Billing wrapper
│   ├── ride/repository/
│   │   ├── RideRepositoryImpl.kt          # GPS tracking, gait detection, auto-detect
│   │   ├── RideHistoryRepositoryImpl.kt
│   │   ├── RideSyncOutboxStore.kt
│   │   └── RideStopSyncOrchestrator.kt
│   └── {feature}/repository/             # One Impl per feature (20 total)
│
├── domain/
│   ├── model/ (User, UserRole)
│   └── {feature}/
│       ├── model/                         # Pure Kotlin data classes
│       ├── repository/                    # Interfaces only
│       └── usecase/                       # Single-responsibility use cases (80+)
│
└── feature/
    └── {feature}/presentation/            # Screen + ViewModel pairs (33+ screens)
```

---

## 5. Feature Modules

### 5.1 Authentication (`feature/auth/`)
- **Screens:** LoginScreen, EmailLoginScreen, EnrollmentScreen, ForgotPasswordScreen, ProfileScreen, EditProfileScreen
- **ViewModels:** AuthViewModel, LoginViewModel, EnrollmentViewModel, ForgotPasswordViewModel, ProfileViewModel
- **Capabilities:** Google Sign-In, Email/Password, password reset (deep link: `horsegallop.page.link/reset-password`), account deletion

### 5.2 Home Dashboard (`feature/home/`)
- **Screens:** HomeScreen, RecentActivityDetailScreen
- **ViewModel:** HomeViewModel
- **Data:** Recent activities, user stats, dynamic horse tips from backend

### 5.3 Ride Tracking (`feature/ride/`)
- **Screens:** RideTrackingScreen, RideDetailScreen
- **ViewModels:** RideTrackingViewModel, RideDetailViewModel
- **Capabilities:**
  - Real-time GPS tracking with gait detection (walk/trot/canter)
  - Color-coded polyline per gait segment (blue/green/orange)
  - 5-point speed smoothing (`GaitThresholds.kt`)
  - Elevation tracking (GPS altitude → elevation profile chart)
  - Rider vs. Horse calorie calculation (750/2000/3500 kcal/hr tiers)
  - Gait distribution statistics (walk/trot/canter %)
  - Auto-detect ride stop (5 min stillness algorithm)
  - Offline sync (`RideSyncOutboxStore`)

### 5.4 Schedule & Booking (`feature/schedule/`)
- **Screens:** ScheduleScreen, MyReservationsScreen
- **ViewModel:** ScheduleViewModel
- **Data:** Lesson list, booking via bottom sheet, reservation management

### 5.5 Barn Discovery (`feature/barn/`)
- **Screens:** BarnListScreen, BarnDetailScreen, BarnsMapViewScreen
- **ViewModels:** BarnViewModel, BarnDetailViewModel
- **Capabilities:** Chip filters, map view (Google Maps), favorite toggle, barn reviews, lesson booking within barn

### 5.6 Horse Management (`feature/horse/`)
- **Screens:** HorseListScreen, AddHorseScreen, HorseHealthScreen
- **ViewModels:** HorseViewModel, HorseHealthViewModel
- **Data:** Horse profiles, dynamic breed list from backend, health event history

### 5.7 Barn Management — B2B (`feature/barnmanagement/`)
- **Screens:** BarnDashboardScreen, CreateLessonScreen, LessonRosterScreen
- **ViewModels:** BarnDashboardViewModel, CreateLessonViewModel, LessonRosterViewModel
- **Access:** Visible only to users with `UserRole.BARN_OWNER`

### 5.8 Training Plans (`feature/training/`) — Pro Gated
- **Screen:** TrainingPlansScreen
- **ViewModel:** TrainingPlansViewModel
- **Gate:** `ProGate` composable wraps content; redirects to SubscriptionScreen if FREE

### 5.9 Health Calendar (`feature/health/`)
- **Screens:** HealthScreen, AddHealthEventScreen
- **ViewModel:** HealthViewModel
- **Data:** Rider + horse health events (distinct from HorseHealthScreen)

### 5.10 Challenges & Badges (`feature/challenge/`)
- **Screen:** ChallengeScreen
- **ViewModel:** ChallengeViewModel
- **Data:** Active challenges, earned badges, badge claiming

### 5.11 AI Coach (`feature/aicoach/`)
- **Screen:** AiCoachScreen (chat bubble UI)
- **ViewModel:** AiCoachViewModel
- **Backend:** `askAiCoach` Cloud Function → Gemini 1.5 Flash, Turkish, keyword fallback

### 5.12 Equestrian Agenda (`feature/equestrian/`)
- **Screen:** EquestrianAgendaScreen (3 tabs: Announcements, Competitions, TBF Events)
- **ViewModel:** EquestrianAgendaViewModel
- **Data:** Federation announcements, competition calendar, Turkish horse racing events (TBF)

### 5.13 TBF Event Details (`feature/tbf/`)
- **Screens:** TbfEventDetailScreen
- **ViewModels:** TbfEventDetailViewModel, TbfViewModel
- **Data:** Race day programs, race cards, jockey/horse stats

### 5.14 Notifications (`feature/notifications/`)
- **Screen:** NotificationsScreen
- **ViewModel:** NotificationsViewModel
- **FCM Channels:** `general`, `reservation`, `lesson`
- **Data source:** Firestore realtime listener

### 5.15 Subscription (`feature/subscription/`)
- **Screen:** SubscriptionScreen (Pro feature preview list + paywall)
- **ViewModel:** SubscriptionViewModel
- **Billing:** `BillingManager` requires Activity reference; call `launchBillingFlow(activity, productId)`
- **Product IDs:** `horsegallop_pro_monthly`, `horsegallop_pro_yearly`

### 5.16 Settings (`feature/settings/`)
- **Screen:** SettingsScreen
- **ViewModel:** SettingsViewModel
- **Backend:** Synced via `getUserSettings` / `updateUserSettings` Cloud Functions

### 5.17 Reviews (`feature/review/`)
- **Screen:** WriteReviewScreen
- **ViewModel:** ReviewViewModel
- **Targets:** LESSON, INSTRUCTOR (`ReviewTargetType`)

### 5.18 Onboarding (`feature/onboarding/`)
- **Screen:** OnboardingScreen
- **ViewModel:** OnboardingViewModel

---

## 6. Navigation Routes (AppNav.kt — Sealed Class `Dest`)

```
Dest.Onboarding
Dest.Login
Dest.EmailLogin
Dest.ForgotPassword
Dest.Enroll
Dest.Home                          ← Bottom Nav
Dest.Ride                          ← Bottom Nav
Dest.Barns                         ← Bottom Nav
Dest.Schedule                      ← Bottom Nav
Dest.Profile                       ← Bottom Nav
Dest.ProfileEdit
Dest.Settings
Dest.MyReservations
Dest.MyHorses
Dest.AddHorse
Dest.WriteReview(targetId, targetType, targetName)
Dest.BarnDetail(id)
Dest.BarnDashboard(barnId)
Dest.CreateLesson(barnId)
Dest.LessonRoster(lessonId)
Dest.RecentActivityDetail
Dest.BarnsMapView
Dest.RideDetail(id)
Dest.Notifications
Dest.Subscription
Dest.HealthCalendar
Dest.AddHealthEvent
Dest.HorseHealth(horseId, horseName)
Dest.Training
Dest.EquestrianAgenda
Dest.Challenges
Dest.AiCoach
Dest.TbfEvents
Dest.TbfEventDetail(venueCode, eventIndex)
```

---

## 7. Domain Models

### Auth / User
| Model | Location | Key Fields |
|-------|----------|-----------|
| `User` | `domain/model/User.kt` | uid, email, displayName |
| `UserRole` | `domain/model/UserRole.kt` | RIDER, BARN_OWNER, ADMIN |

### Ride
| Model | Key Fields |
|-------|-----------|
| `RideSession` | id, startTime, endTime, pathPoints, metrics |
| `RideMetrics` | durationSec, distanceM, avgSpeedKmh, calsBurned, elevationGainM |
| `GeoPoint` | lat, lng, altitudeM, speedKmh, timestampMs |
| `GaitThresholds` | walkMax, trotMax (speed thresholds) |

### Schedule
| Model | Key Fields |
|-------|-----------|
| `Lesson` | id, date, title, instructorName, durationMin, level, price, spotsTotal, spotsAvailable, isBookedByMe |
| `Reservation` | id, lessonId, userId, status |

### Horse
| Model | Key Fields |
|-------|-----------|
| `Horse` | id, name, breed, birthYear, imageUrl |
| `HorseHealthEvent` | id, horseId, date, type, notes |
| `HorseTip` | id, category, text |

### Barn
| Model | Key Fields |
|-------|-----------|
| `BarnUi` | id, name, location, rating, heroImageUrl, capacity, phone |
| `BarnWithLocation` | barn + LatLng |
| `BarnReview` | rating, comment, authorName |
| `Instructor` | id, name, specialization, rating |

### Other Domains
| Model | Feature |
|-------|---------|
| `SubscriptionStatus` | FREE / PRO |
| `TrainingPlan`, `TrainingTask` | Training |
| `Challenge`, `Badge` | Challenges |
| `AppNotification` | Notifications |
| `HealthEvent` | Health Calendar |
| `UserSettings` | Settings |
| `ChatMessage` | AI Coach |
| `Review` | Reviews |
| `EquestrianModels` (Event, Announcement, Competition) | Equestrian Agenda |
| `TbfVenue`, `TbfCompetition`, `TbfEventDay`, `TbfEventCard`, `TbfAthlete` | TBF |
| `BarnStats`, `ManagedLesson`, `StudentRosterEntry` | Barn Management |

---

## 8. Backend — Firebase Cloud Functions

**File:** `backend/src/index.ts` | **Region:** `us-central1` | **Total:** 76+ exported functions

### User & Profile
| Function | Description |
|----------|-----------|
| `getUserProfile` | Fetch user profile from Firestore |
| `updateUserProfile` | Update firstName, lastName, phone, city, etc. |
| `getUserSettings` | Fetch user preferences |
| `updateUserSettings` | Sync settings to backend |
| `deleteUserData` | GDPR data deletion |

### Home Dashboard
| Function | Description |
|----------|-----------|
| `getHomeDashboard` | Returns stats, horse tips, recent activities |

### Barn & Lessons
| Function | Description |
|----------|-----------|
| `getBarns` | List barns with filters |
| `getBarnDetail` | Barn info + instructors + reviews |
| `getLessons` | Lessons for a barn |
| `bookLesson` | Reserve a lesson |
| `cancelReservation` | Cancel booking |
| `getMyReservations` | User's booked lessons |
| `getBarnStats` | Barn statistics (owners) |
| `getManagedLessons` | Lessons for barn owner |
| `createLesson` / `createManagedLesson` | Create lesson (owner) |
| `cancelLesson` / `cancelManagedLesson` | Cancel lesson (owner) |
| `getLessonRoster` | Students enrolled in lesson |

### Horses & Health
| Function | Description |
|----------|-----------|
| `getBreeds` | Dynamic breed list |
| `getHorseTips` | Dynamic horse care tips |
| `getMyHorses` | User's horse profiles |
| `addHorse` / `deleteHorse` | Horse management |
| `getHorseHealthEvents` | Horse health history |
| `addHorseHealthEvent` / `updateHorseHealthEvent` / `deleteHorseHealthEvent` | Health CRUD |
| `getHealthEvents` | User health calendar |
| `saveHealthEvent` / `markHealthEventCompleted` | Health event management |
| `syncHorseHealthReminders` | Scheduled reminder sync |

### Ride Tracking
| Function | Description |
|----------|-----------|
| `saveRide` | Save completed GPS ride session |
| `getMyRides` | Ride history |

### Reviews
| Function | Description |
|----------|-----------|
| `submitReview` | Submit lesson/instructor review |
| `getMyReviews` | User's reviews |

### Notifications
| Function | Description |
|----------|-----------|
| `sendGeneralNotification` | FCM push notification |
| `getNotifications` | Fetch notification list |
| `markNotificationsRead` | Mark as read |

### Safety Tracking
| Function | Description |
|----------|-----------|
| `getSafetySettings` | Safety mode config |
| `updateSafetySettings` | Update safety preferences |
| `addSafetyContact` / `removeSafetyContact` | Emergency contacts CRUD |
| `triggerSafetyAlarm` | Trigger emergency alert |

### Challenges & Subscription
| Function | Description |
|----------|-----------|
| `getActiveChallenges` | Active challenges |
| `getEarnedBadges` | Earned badges |
| `checkAndAwardBadges` | Award badges |
| `getSubscriptionStatus` | Pro status check |
| `verifyPurchase` | Google Play receipt verification |

### AI Coach
| Function | Description |
|----------|-----------|
| `askAiCoach` | Chat with Gemini 1.5 Flash (Turkish, keyword fallback) |

### Equestrian Events (TBF)
| Function | Description |
|----------|-----------|
| `getEquestrianAnnouncements` | Federation announcements |
| `getEquestrianCompetitions` | Upcoming competitions |
| `getTbfUpcomingEvents` | TBF horse racing events |
| `getTbfEventDay` | Races for a specific TBF date |
| `getTbfEventCard` | Single race card details |
| `getTbfAthleteStats` | Jockey/horse stats |

### Federation Sync (Scheduled)
| Function | Description |
|----------|-----------|
| `syncFederatedBarns` | Cron: sync federation barns |
| `syncEquestrianAgenda` | Cron: sync events |
| `getFederatedBarnsSyncStatus` | Sync status check |
| `getFederationSourceHealth` | Data source health check |
| `triggerFederationManualSync` | Manual sync trigger |

---

## 9. Data Flow Pattern

```
[Composable]
    ↓ collectAsStateWithLifecycle()
[ViewModel (StateFlow<UiState>)]
    ↓ viewModelScope.launch
[UseCase]
    ↓ repository.someOperation()
[RepositoryImpl]
    ↓ functionsDataSource.someCloudFunction()
[AppFunctionsDataSource]
    ↓ Firebase.functions.getHttpsCallable(...)
[Firebase Cloud Functions — us-central1]
    ↓
[Firestore / External APIs]
```

**Result handling:**
```kotlin
repo.operation()
    .onSuccess { _ui.update { it.copy(data = result) } }
    .onFailure { _ui.update { it.copy(error = it.message) } }
```

---

## 10. ViewModel State Pattern

```kotlin
data class ExampleUiState(
    val loading: Boolean = true,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val useCase: GetExampleUseCase
) : ViewModel() {
    private val _ui = MutableStateFlow(ExampleUiState())
    val ui: StateFlow<ExampleUiState> = _ui
}
```

---

## 11. Design System

### Color Tokens (always use `LocalSemanticColors.current`)
| Token | Usage |
|-------|-------|
| `screenBase` | Main screen background |
| `cardElevated` | Elevated card surface |
| `cardSubtle` | Subtle card surface |
| `cardStroke` | Card border |
| `success` | Success state |
| `warning` | Warning/caution |
| `destructive` | Destructive actions |
| `panelOverlay` | Bottom nav + modal surfaces |
| `ratingStar` | Star rating color |

**Rule:** Never use `Color(0xFF...)`, `Color.White`, `Color.Black`, or `MaterialTheme.colorScheme.surface/background` in `feature/`, `core/`, `navigation/`, or `MainActivity.kt`. The `enforceSemanticSurfaceTokens` Gradle task will fail the build if detected.

---

## 12. Test Files

### Instrumented Tests (`app/src/androidTest/`)
```
feature/auth/presentation/AuthSmokeTest.kt          # Auth flow smoke tests
feature/auth/presentation/ProfileScreenTest.kt       # Profile screen UI tests
feature/ride/presentation/RideTrackingScreenTest.kt  # Ride tracking screen tests
```

### Backend Tests (`backend/src/__tests__/`)
```
home-service.test.ts    # Home service unit tests
validators.test.ts      # Input validation tests
```

---

## 13. Firebase Configuration

| Property | Value |
|----------|-------|
| Project ID | `com-horse-gallop` |
| Project Number | 866585485346 |
| Storage Bucket | `com-horse-gallop.firebasestorage.app` |
| Package | `com.horsegallop` |
| Functions Region | `us-central1` |
| Google Sign-In | OAuth 2.0 (Web client) |
| App Check (release) | PlayIntegrityAppCheckProviderFactory |
| App Check (debug) | DebugAppCheckProviderFactory |
| FCM Token | Saved to Firestore on `onNewToken` |

---

## 14. Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Clean build
./gradlew clean assembleDebug

# Open PR (runs local gate first)
bash scripts/pr-pipeline-merge.sh
```

**Git hooks:**
- `pre-commit`: conflict marker + shell syntax checks
- `pre-push`: mandatory `lintDebug` + `testDebugUnitTest`

---

## 15. Localization

| Locale | Files |
|--------|-------|
| Default | `values/strings.xml`, `values/arrays.xml` |
| Turkish | `values-tr/strings.xml`, `values-tr/arrays.xml` |
| English | `values-en/strings.xml`, `values-en/arrays.xml` |

**Constraint:** Turkish strings with apostrophes must be escaped: `Pro\'ya Geç` (not `Pro'ya Geç`).

---

## 16. Project Statistics

| Metric | Count |
|--------|-------|
| Total Kotlin Files | 277+ |
| Feature Modules | 17 |
| Screens (Composable) | 33+ |
| ViewModels | 27+ |
| Domain Models | 50+ |
| Repository Implementations | 20 |
| Use Cases | 80+ |
| Cloud Functions (Backend) | 76+ |
| Core Components | 8 |
| Instrumented Tests | 3 |
| Backend (index.ts) | ~2664 lines |
