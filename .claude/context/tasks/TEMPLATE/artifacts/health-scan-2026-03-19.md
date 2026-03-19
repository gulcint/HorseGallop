# HorseGallop Proje Sağlık Taraması — 2026-03-19

Araştırma agenti tarafından üretilmiştir. Kod değişikliği yapılmamıştır.

---

## 1. Problem Özeti

HorseGallop projesinin mevcut durumunu nesnel olarak belgeleyen kapsamlı bir taramadır.
Firebase → Supabase migrasyonu Sprint 7'de tamamlandığı söylenmektedir. Bu rapor neyin
gerçekten bittiğini, neyin stub/yarım kaldığını ve neyin hiç yapılmadığını somut kanıtlarla ortaya koyar.

---

## 2. Mevcut Repo Gerçeği

### 2a. Build Durumu

| Görev                  | Sonuç              | Süre  |
|------------------------|--------------------|-------|
| `assembleDebug`        | BUILD SUCCESSFUL   | 15 s  |
| `testDebugUnitTest`    | BUILD SUCCESSFUL   | 1 s   |

Her iki build temiz geçmektedir. Gradle 9.0 deprecation uyarıları mevcuttur ancak bloklayıcı değildir.

---

### 2b. Firebase Kalıntıları

`FirebaseFirestore`, `FirebaseFunctions`, `FirebaseAuth`, `FirebaseStorage` sınıflarına doğrudan referans
içeren kaynak kodu dosyası **sıfırdır**.

`import com.google.firebase.*` içeren üç dosya kalmaktadır:

- `app/src/main/java/com/horsegallop/HorseGallopApp.kt` — FCM init ve AppCheck
- `app/src/main/java/com/horsegallop/data/di/FirebaseModule.kt` — FCM token kaydı
- `app/src/main/java/com/horsegallop/PushService.kt` — FCM push service

Bu üç dosya beklenen kalıntılardır; FCM bildirim altyapısı Firebase üzerinde bilinçli bırakılmıştır.

`FirebaseAuth.getInstance()` doğrudan çağrısı kaynak kodunda **sıfır** kez geçmektedir.

**Sonuç:** Firebase veri katmanı tamamen temizlenmiştir. Sadece FCM bildirimleri için Firebase kullanımı devam etmektedir. Bu bilinçli bir mimari karardır.

---

### 2c. Boş onClick / Stub Lambdalar

Aşağıdaki yerler gerçek işlevsellik yerine `onClick = {}` içermektedir:

| Dosya | Satır | Neden Kritik |
|-------|-------|--------------|
| `feature/health/presentation/AddHealthEventScreen.kt` | 309 | FilterChip seçimi çalışmıyor — event type seçilemiyor |
| `feature/health/presentation/AddHealthEventScreen.kt` | 330 | "Kaydet" butonu no-op — sağlık olayı kaydedilemiyor |
| `feature/schedule/presentation/ScheduleScreen.kt` | 218, 231 | Bir buton no-op ve disabled |
| `feature/training/presentation/TrainingPlansScreen.kt` | 208 | Antrenman planı eylem butonu boş |
| `feature/barn/presentation/BarnsMapViewScreen.kt` | 623 | Harita üzerindeki aksiyon butonu boş |
| `feature/barn/presentation/BarnListScreen.kt` | 791 | Barn liste aksiyonu boş |
| `feature/ride/presentation/RideTrackingScreen.kt` | 986 | Ride içi buton no-op |

**En kritik olan:** `AddHealthEventScreen` tamamen stub durumdadır. Form görünür fakat hiçbir şey kaydetmez.

---

### 2d. Stub / Yarım Kalan Fonksiyonlar

**SupabaseAuthRepositoryImpl.kt**

- Satır 41: `signOut()` override'ı no-op'tur. Yorumda "ViewModels should call SupabaseAuthDataSource.signOut() directly" yazılıdır. Ancak `SignOutUseCase.execute()` bu interface metodunu çağırmaktadır ve `ProfileViewModel` ile `SettingsViewModel` use case üzerinden sign-out yapmaktadır. Bu silent bug: sign-out işlemi sessizce hiçbir şey yapmaz.
- Satır 91-96: `confirmPasswordReset()` daima `Result.success(Unit)` döner. Kullanıcı şifresini gerçekte sıfırlayamaz.
- Satır 98-102: `resendVerificationEmail()` no-op. Sprint 3'e ertelenmiş, hiç yapılmamış.
- Satır 134-141: `getLottieConfig()` hardcoded URL döner. Sprint 3'e ertelenmiş, hiç yapılmamış.

**EquestrianAgendaRepositoryImpl.kt** satır 57: `manualSync` metodu "stub returns last known sync status" yorumuyla no-op bırakılmıştır.

**AddHealthEventScreen.kt**: Ekranın ikinci yarısı (satır 294–336) "HealthViewModelPreview" benzeri görünüm kodudur; gerçek ViewModel'e bağlanmamış, hardcoded değerler (`"Rüzgar"`, `"15 Mar 2026"`) içermektedir. Tüm form interaksiyonları boştur.

---

### 2e. SemanticColors İhlalleri

`feature/` ve `core/` altında `Color(0x...)`, `Color.White`, `Color.Black`, `Color.Gray`, `Color.Red`, `Color.Green`, `Color.Blue` veya `color = Color.` pattern'ine uyan **sıfır eşleşme** bulunmuştur.

**Sonuç:** SemanticColors kuralı ihlal edilmemektedir. `enforceSemanticSurfaceTokens` preBuild adımı başarıyla geçmektedir.

---

### 2f. @Preview Eksik Ekranlar

34 `*Screen.kt` dosyasının tamamı `@Preview` içermektedir. Eksik Preview yoktur.

---

### 2g. Hardcoded String Tespiti (stringResource yerine literal)

Aşağıdaki literal string kullanımları tespit edilmiştir:

| Dosya | Satır | İçerik |
|-------|-------|--------|
| `feature/barnmanagement/presentation/CreateLessonScreen.kt` | 216 | `Text("HH:mm")` |
| `feature/health/presentation/AddHealthEventScreen.kt` | 318 | `Text("At")` |
| `feature/health/presentation/AddHealthEventScreen.kt` | 326 | `Text("Tarih")` |
| `feature/health/presentation/AddHealthEventScreen.kt` | 331 | `Text("Kaydet")` |
| `feature/horse/presentation/HorseListScreen.kt` | 101 | `Text("Henüz at eklemediniz")` |
| `feature/horse/presentation/HorseListScreen.kt` | 133 | `Text("Atı Sil")` |
| `feature/horse/presentation/HorseListScreen.kt` | 142 | `Text("Sil")` |

7 hardcoded string tespit edilmiştir. Bunların tamamı Türkçedir ve i18n açısından sorunludur.

---

### 2h. Unit Test Coverage

| Metrik | Sayı |
|--------|------|
| Test dosyası (app/src/test) | 13 |
| ViewModel dosyası (feature/) | 29 |

13 test dosyasının breakdown'ı:
- Data layer: BarnRepository, HomeRepository, HorseRepository, ReviewRepository, ScheduleRepository, UserSettingsRepository, TrainingRepository (7 dosya)
- Feature layer: HealthViewModel, HomeViewModel, HorseHealthViewModel, HorseViewModel, ReviewViewModel, ScheduleViewModel (6 dosya)

**Kapsanmayan ViewModel'ler** (23 adet):
AiCoachViewModel, AuthViewModel, BarnDetailViewModel, BarnManagementViewModel, BarnViewModel, ChallengeViewModel, ContentViewModel, CreateLessonViewModel, EquestrianAgendaViewModel, ForgotPasswordViewModel, EnrollmentViewModel, LessonRosterViewModel, NotificationViewModel, OnboardingViewModel, ProfileViewModel, RideDetailViewModel, RideTrackingViewModel, SettingsViewModel, SubscriptionViewModel, TbfViewModel, TrainingPlansViewModel, WriteReviewViewModel, HorseListViewModel

---

### 2i. AppNav Rotaları

Tüm 30 Dest rotası NavHost içinde `composable {}` bloğu ile eşleştirilmiştir. Eksik composable yoktur.

Tek anomali: `Dest.BarnsMapView` (satır 522–528) bir boş composable'dır; içinde `LaunchedEffect` ile `Dest.Barns`'a yönlendirme yapar. Bu bilinçli bir redirect pattern'idir (eski route'ların geriye uyumluluğu için).

---

### 2j. DataModule Binding'leri

Tüm 22 binding `SupabaseXxxRepositoryImpl` sınıflarına bağlanmıştır. Firebase'e bağlı tek bir binding kalmamıştır. DataModule tamamen temizdir.

---

### 2k. Supabase Migration Dosyaları

```
supabase/migrations/
  20260318000001_initial_schema.sql
  20260318000002_rls_policies.sql
  20260318000003_auth_trigger.sql
  20260318000004_seed_data.sql
  20260318000005_ai_coach_messages.sql

supabase/functions/
  ai-coach/
```

5 migration, 1 Edge Function mevcuttur.

**Kritik bulgu:** `supabase/functions/ai-coach/index.ts` içinde `GROQ_API_KEY` kullanılmaktadır. Memory.md'de "GEMINI_API_KEY secret" eksik olarak belgelenmiştir, ancak Edge Function'da Gemini değil **Groq** API kullanılmaktadır. Memory.md güncel değildir. GROQ_API_KEY Supabase Dashboard'dan set edilmeli veya zaten set edilmiş olmalıdır.

---

## 3. Dış Kaynaklar veya Kontratlar

- Supabase SDK v3.1.4 Android entegrasyonu aktiftir (`SupabaseModule`, `SupabaseAuthDataSource`).
- Groq API (`https://api.groq.com/openai/v1/chat/completions`) AI Coach Edge Function'da kullanılmaktadır, Gemini değil.
- Google Play Billing (`BillingManager`) backend doğrulama olmadan mevcuttur; ödeme akışı tamamlanmamıştır.

---

## 4. Riskler / Bilinmeyenler

### Kritik Riskler (production'a çıkmayı engelleyen)

1. **Sign-out silent bug** — `SupabaseAuthRepositoryImpl.signOut()` no-op. `SignOutUseCase` üzerinden tetiklenen tüm sign-out akışları sessizce başarısız olur. Kullanıcı oturumu kapanmaz.
   - Dosya: `/app/src/main/java/com/horsegallop/data/auth/SupabaseAuthRepositoryImpl.kt` satır 37–41
   
2. **Şifre sıfırlama tamamen kırık** — `confirmPasswordReset()` her zaman başarı döner, gerçek Supabase `auth.updateUser` çağrısı yapılmaz.
   - Dosya: aynı dosya, satır 91–96

3. **AddHealthEventScreen tamamen stub** — Form görünür, ViewModel bağlantısı yoktur, hiçbir veri kaydedilmez.
   - Dosya: `/app/src/main/java/com/horsegallop/feature/health/presentation/AddHealthEventScreen.kt` satır 294–336

### Orta Riskler

4. **GROQ_API_KEY ortam değişkeni** — AI Coach Edge Function için Supabase Dashboard'da ayarlanmış olması gerekir. Yoksa tüm AI coach sorguları başarısız olur.

5. **Ödeme backend doğrulaması yoktur** — `BillingManager` Google Play ile bağlantı kurar ancak satın alma sonrası Supabase/backend doğrulaması implemente edilmemiştir.

6. **resendVerificationEmail no-op** — Email doğrulama yeniden gönderme çalışmaz.

7. **getLottieConfig hardcoded** — Animasyon URL'leri backend'den değil, kod içinden alınmaktadır.

### Düşük Riskler

8. 7 hardcoded Türkçe string strings.xml'e taşınmamıştır (i18n).
9. 23 ViewModel için test coverage yoktur.
10. Gradle 9.0 deprecation uyarıları mevcuttur.
11. `BarnDetail` ekranında telefon arama butonu `onClick = {}` olarak kalmıştır (`BarnListScreen.kt:791`).

---

## 5. Builder veya Operator İçin Net Girdiler

### Öncelik 1 — Kritik Fix (production blocker)

**P1-A:** `SupabaseAuthRepositoryImpl.signOut()` override'ını gerçek suspend çağrısına bağla.
  - Seçenek: interface metodunu `suspend` yap veya `authDataSource.signOut()` çağrısını coroutine scope içinde yap.
  - Etkilenen: `data/auth/SupabaseAuthRepositoryImpl.kt:37-41`

**P1-B:** `confirmPasswordReset()` içine gerçek `auth.updateUser { password = newPassword }` çağrısını ekle.
  - Etkilenen: `data/auth/SupabaseAuthRepositoryImpl.kt:91-96`

**P1-C:** `AddHealthEventScreen`'i gerçek `HealthViewModel` state'ine bağla. Form alanlarını bağla, "Kaydet" butonunu `viewModel.addHealthEvent()` çağıracak şekilde implemente et.
  - Etkilenen: `feature/health/presentation/AddHealthEventScreen.kt:294-336`

### Öncelik 2 — Önemli Fix

**P2-A:** Supabase Dashboard'da `GROQ_API_KEY` secret'ının set edildiğini doğrula. Memory.md'deki "GEMINI_API_KEY" notunu "GROQ_API_KEY" olarak düzelt.

**P2-B:** `resendVerificationEmail()` için `auth.resendEmail()` Supabase çağrısını ekle.

**P2-C:** `BarnDetail` iletişim butonu için telefon arama intent'i ekle.

### Öncelik 3 — Teknik Borç

**P3-A:** 7 hardcoded Türkçe string'i strings.xml'e taşı (bkz. bölüm 2g tablosu).

**P3-B:** 23 test edilmemiş ViewModel için öncelikli olarak `ProfileViewModel`, `SettingsViewModel`, `AuthViewModel`, `RideTrackingViewModel` için birim test yaz.

**P3-C:** Ödeme akışı için backend doğrulama endpointi yaz (Supabase Edge Function veya RLS).

**P3-D:** `getLottieConfig()` hardcoded URL'leri Supabase `app_content` tablosuna taşı.

---

_Rapor tarihi: 2026-03-19. Tarama kapsamı: app/src/main/java/com/horsegallop tüm Kotlin kaynakları + supabase/ dizini._
