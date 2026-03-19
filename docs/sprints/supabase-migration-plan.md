# HorseGallop — Firebase → Supabase Migration Plan

**Tarih:** 2026-03-18
**Durum:** Onaylandı, Sprint 1 başlıyor
**Toplam:** 8 Sprint

---

## Genel Bakış

Firebase Cloud Functions → Supabase Edge Functions
Firebase Auth → Supabase Auth
Firestore → PostgreSQL (Supabase)
**Android ViewModel/UseCase/Repository katmanları DEĞİŞMEZ — sadece `AppFunctionsDataSource` yeniden yazılır.**

---

## Sprint Tablosu

| Sprint | İçerik | Neden Bu Sıra |
|--------|--------|---------------|
| **Sprint 1** | Supabase proje kurulumu, 24 tablo SQL schema, RLS politikaları, Auth setup (email + Google) | Tüm diğer sprintlerin temeli |
| **Sprint 2** | Android Supabase SDK, `SupabaseDataSource` iskelet, Auth migration (login/register/profile) | Auth olmadan hiçbir şey test edilemez |
| **Sprint 3** | Profil & Ayarlar (`getUserProfile`, `updateUserProfile`, `getUserSettings`, `updateUserSettings`), At yönetimi (`getMyHorses`, `addHorse`, `deleteHorse`, `getBreeds`, `getHorseTips`) | Kullanıcı verisinin temeli |
| **Sprint 4** | Sürüş takibi (`saveRide`, `getMyRides`), Home dashboard (`getHomeDashboard`) | Uygulamanın ana özelliği |
| **Sprint 5** | Ahır & ders (`getBarns`, `getBarnDetail`, `getLessons`, `bookLesson`, `cancelReservation`, `getMyReservations`) | Schedule özelliği tamamlanır |
| **Sprint 6** | At sağlık takvimi (4 fonksiyon), Binici sağlık (4 fonksiyon), Değerlendirmeler, Challenge & Rozet | Profil ekranı eksiksiz olur |
| **Sprint 7** | AI Coach (Gemini Edge Function), Bildirimler, Güvenlik takibi, B2B ahır yönetimi | Gelişmiş özellikler |
| **Sprint 8** | Billing (`verifyPurchase`), TBF yarış verileri, Federasyon sync, Firebase bağımlılıklarını kaldır | Son temizlik + production |

---

## Kapsam

### Ekranlar (30+)
- Auth: LoginScreen, EmailLoginScreen, EnrollScreen, ForgotPasswordScreen
- Onboarding: OnboardingScreen
- Home: HomeScreen
- Ride: RideTrackingScreen, RideDetailScreen
- Schedule: ScheduleScreen, MyReservationsScreen
- Barns: BarnListScreen, BarnDetailScreen, BarnsMapViewScreen
- Horse: HorseListScreen, AddHorseScreen
- Profile: ProfileScreen, EditProfileScreen
- Health: HorseHealthCalendarScreen, RiderHealthCalendarScreen
- Reviews: WriteReviewScreen
- Challenges: ChallengesScreen (embedded in Profile)
- AI Coach: AiCoachScreen
- Safety: SafetyTrackingScreen
- Subscription: SubscriptionScreen
- Settings: SettingsScreen
- Notifications: NotificationsScreen
- B2B: BarnDashboardScreen, CreateLessonScreen, LessonRosterScreen
- Equestrian: EquestrianAgendaScreen (TBF + announcements)

### Firebase Cloud Functions → Supabase (44 fonksiyon)

| Domain | Fonksiyonlar |
|--------|-------------|
| Home | `getHomeDashboard` |
| Auth/Profile | `getUserProfile`, `updateUserProfile` |
| Settings | `getUserSettings`, `updateUserSettings` |
| Horses | `getMyHorses`, `addHorse`, `deleteHorse`, `getBreeds`, `getHorseTips` |
| Rides | `saveRide`, `getMyRides` |
| Barns | `getBarns`, `getBarnDetail`, `getFederatedBarns`, `getFederatedBarnDetail` |
| Lessons | `getLessons`, `bookLesson`, `cancelReservation`, `getMyReservations` |
| Reviews | `getMyReviews`, `submitReview` |
| Horse Health | `getHorseHealthEvents`, `addHorseHealthEvent`, `updateHorseHealthEvent`, `deleteHorseHealthEvent` |
| Rider Health | `getHealthEvents`, `saveHealthEvent`, `deleteHealthEvent`, `markHealthEventCompleted` |
| Challenges | `getActiveChallenges`, `getEarnedBadges`, `checkAndAwardBadges` |
| AI Coach | `askAiCoach` |
| TBF Events | `getTbfEventDay`, `getTbfEventCard`, `getTbfUpcomingEvents` |
| Equestrian | `getEquestrianAnnouncements`, `getEquestrianCompetitions` |
| B2B | `getBarnStats`, `getManagedLessons`, `createLesson`, `cancelLesson`, `getLessonRoster` |
| Content | `getAppContent` |
| Billing | `verifyPurchase`, `getSubscriptionStatus` |
| Notifications | `sendGeneralNotification` |
| Safety | `getSafetySettings`, `updateSafetySettings`, `addSafetyContact`, `removeSafetyContact`, `triggerSafetyAlarm` |
| Federation | `getFederatedBarnsSyncStatus`, `triggerFederationManualSync`, `getFederationSourceHealth` |

### PostgreSQL Tabloları (24)
`user_profiles`, `user_settings`, `horses`, `rides`, `ride_path_points`, `barns`, `barn_instructors`, `lessons`, `reservations`, `reviews`, `horse_health_events`, `health_events`, `challenges`, `user_challenge_progress`, `user_badges`, `fcm_tokens`, `safety_settings`, `safety_contacts`, `horse_breeds`, `horse_tips`, `app_content`, `equestrian_announcements`, `equestrian_competitions`, `federation_sync_status`

---

## Mimari Karar

### Ne Değişir
- `AppFunctionsDataSource.kt` → `SupabaseDataSource.kt` (aynı interface, farklı implementasyon)
- `FirebaseAuthDataSource` → Supabase Auth Kotlin SDK
- `backend/src/index.ts` (Firebase CF) → `supabase/functions/` (Edge Functions, Deno)
- `build.gradle.kts` — Firebase SDK kaldırılır, Supabase SDK eklenir
- `google-services.json` — kaldırılır, `supabase.properties` eklenir

### Ne Değişmez
- Tüm domain modelleri (`domain/*/model/`)
- Tüm use case'ler (`domain/*/usecase/`)
- Tüm repository interface'leri (`domain/*/repository/`)
- Tüm ViewModel'lar (`feature/*/presentation/*ViewModel.kt`)
- Tüm Composable ekranlar (`feature/*/presentation/*Screen.kt`)
- Navigation (`AppNav.kt`)
- DI (`DataModule`, `FirebaseModule` → `SupabaseModule`)

### Supabase Mimarisi
```
Android App
    └── SupabaseDataSource (Kotlin)
            ├── supabase.auth     → Supabase Auth (email + Google)
            ├── supabase.postgrest → Direct table access (CRUD)
            └── supabase.functions → Edge Functions (complex logic)

Supabase Backend
    ├── PostgreSQL (24 tables + RLS)
    ├── Auth (email/password + Google OAuth)
    ├── Edge Functions (Deno/TypeScript)
    │     ├── get-home-dashboard
    │     ├── book-lesson
    │     ├── ask-ai-coach
    │     ├── verify-purchase
    │     ├── get-tbf-events
    │     └── ...
    └── Storage (horse images, profile photos)
```

### PostgREST vs Edge Functions Kararı
Basit CRUD → PostgREST (direkt tablo erişimi, Edge Function gerekmez)
Karmaşık logic → Edge Function

| Fonksiyon | Yaklaşım |
|-----------|---------|
| `getMyHorses` | PostgREST `SELECT` |
| `addHorse` | PostgREST `INSERT` |
| `getMyRides` | PostgREST `SELECT` |
| `saveRide` | PostgREST `INSERT` (ride + path_points) |
| `getBarns` | PostgREST `SELECT` |
| `bookLesson` | Edge Function (transaction: reservation + spots_available--) |
| `getHomeDashboard` | Edge Function (aggregation) |
| `askAiCoach` | Edge Function (Gemini API) |
| `getTbfEvents` | Edge Function (web scraping) |
| `verifyPurchase` | Edge Function (Google Play API) |

---

## Sprint Detayları (özet)

### Sprint 1 — Supabase Foundation
SQL schema + RLS + Auth setup. **Backend sadece.** Android dokunulmaz.
→ Çıktı: Supabase'de tüm tablolar, RLS politikaları, Auth provider'lar hazır.

### Sprint 2 — Android SDK + Auth Migration
Supabase Kotlin SDK entegrasyonu. `FirebaseAuth` → `SupabaseAuth`.
→ Çıktı: Login/register/forgot password Supabase ile çalışıyor.

### Sprint 3 — Profile + Settings + Horses
PostgREST ile basit CRUD. `SupabaseDataSource`'un ilk gerçek implementasyonları.
→ Çıktı: Profil ekranı, at listesi Supabase'den dolduruluyor.

### Sprint 4 — Rides + Home Dashboard
`saveRide` (ride + path_points insert), `getMyRides`, `getHomeDashboard` Edge Function.
→ Çıktı: Sürüş kayıt ve geçmiş çalışıyor.

### Sprint 5 — Schedule + Barns
`bookLesson` Edge Function (transaction), barns PostgREST, lessons PostgREST.
→ Çıktı: Ders rezervasyon akışı uçtan uca çalışıyor.

### Sprint 6 — Health + Reviews + Challenges
CRUD fonksiyonlar, tümü PostgREST.
→ Çıktı: Profil ekranı eksiksiz dolduruluyor.

### Sprint 7 — Advanced Features
Gemini AI Coach, FCM via Edge Function, Safety, B2B barn management.
→ Çıktı: Pro özellikler çalışıyor.

### Sprint 8 — Billing + TBF + Cleanup
Google Play purchase verification, TBF scraping, Firebase SDK tamamen kaldırılır.
→ Çıktı: Production-ready, Firebase bağımlılığı sıfır.

---

## Gelecek Feature: TBF Faaliyet Takvimi (Öncelik: Düşük)

TBF (Türkiye Binicilik Federasyonu) etkinlik takvimini interaktif hale getir.
Detaylı analiz: `docs/sprints/tbf-activity-calendar-analysis.md`

### Özet

- Mevcut `equestrian_competitions` tablosu + `EquestrianAgendaScreen` COMPETITIONS sekmesi yetersiz
- `binicilik.org.tr/Anasayfa/Faaliyet` kaynaklı 6 disiplin + 9 etkinlik türü desteklenmeli
- Hedef UX: takvim grid görünümü (ay navigasyonu + disiplin renk noktaları) veya timeline liste
- Yeni `tbf_activities` Supabase tablosu gerekiyor (`start_date`, `end_date`, `discipline`, `activity_type`, `city`, `organization`)
- Veri kaynağı: Supabase Edge Function (cron tabanlı scraping) — hukuki değerlendirme yapılmalı

### Renk kodlaması

Disiplinler SemanticColors amber palette'e uygun renk bandlarıyla ayrışacak.

### Tahmini: 2 Sprint

- Sprint X.1: domain model, repository, temel liste ekranı (Timeline veya Hızlı Liste)
- Sprint X.2: takvim grid bileşeni, detay bottom sheet, Google Calendar entegrasyonu, scraping Edge Function
