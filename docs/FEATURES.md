# HorseGallop — Özellik Haritası

> Son güncelleme: 2026-03-15

---

## Kullanıcı Yolculuğu

```
Onboarding (ilk açılış, tek seferlik)
    └─► Login ekranı
            ├─► Google ile giriş
            └─► Email/şifre ile giriş / Kayıt / Şifre sıfırlama
                    └─► Home (ana ekran)
                            ├─► Barns    — ahır keşfi ve rezervasyon
                            ├─► Ride     — canlı sürüş takibi
                            ├─► Schedule — ders programı ve rezervasyonlarım
                            └─► Profile  — at yönetimi, ayarlar, Pro abonelik
```

Çıkış: Settings → Çıkış yap → Onboarding'e dön (back-stack temizlenir).

---

## Temel Ekranlar (Bottom Nav)

### Home
- Son sürüş aktivitelerini listeler (RideSession kartları, tıklanabilir → RideDetail).
- Kullanıcı istatistikleri özeti (toplam mesafe, süre, kalori).
- Dinamik At İpuçları (backend'den kategorili HorseTip listesi).
- Kısayollar: Sürüşe Başla, Ahırları Keşfet, Antrenman Planları, Federasyon Takvimi.
- Tüm aktiviteler sayfasına yönlendirme (RecentActivityDetailScreen).

### Barns (Ahır Keşfi)
- Yakındaki ahırların liste görünümü; filtre ve arama.
- Harita görünümüne geçiş (BarnsMapViewScreen — Google Maps Compose).
- Favori ahır ekleme/kaldırma (ToggleBarnFavoriteUseCase).
- Ahır detayına tıklama → BarnDetail.

### Ride (Sürüş Takibi)
Aktif sürüş ekranı — canlı GPS metrikleri:
- Hız, mesafe, süre, anlık yükseklik.
- Gait renk kodlu canlı gösterim: yürüyüş (mavi) / tırıs (yeşil) / dörtnal (turuncu).
- Binici ve at kalori takibi (MET tabanlı, gait'e göre ayrı hesap: "Sen: X kcal | At: Y kcal").
- Elevation profil kartı (Canvas chart, anlık yükseklik değişimi).
- At seçimi dropdown (kayıtlı atlar arasından).
- Ride auto-detect: 5 dakika hareketsizlik sonrası otomatik durdurma sinyali.
- Offline sync: bağlantı yoksa sürüş yerel olarak saklanır, tekrar bağlanınca senkronize edilir.
- Sürüş başlatma/durdurma, onay dialog'u.

### Schedule (Program)
- Ders listesi (tarih, başlık, eğitmen, süre, seviye, fiyat, müsait yer).
- Ders detay bottom sheet: rezervasyon yap / iptal et.
- Rezervasyonlarım sayfası (MyReservationsScreen): mevcut rezervasyonlar, ders detayı, değerlendirme yaz.

### Profile
- Kullanıcı profili kartı (avatar, isim, email).
- Navigasyon: Atlarım, Bildirimler, Sağlık Takvimi, Meydan Okumalar, AI Koç, Ayarlar.
- Abonelik durumu gösterimi (Pro rozeti / Aboneliği Yönet).
- Çıkış yap.

---

## Sürüş & Antrenman

### Ride Tracking (GPS Sürüş Takibi)
**Dosya:** `feature/ride/presentation/RideTrackingScreen.kt`

- Gerçek zamanlı GPS konumu (Location servis izni istenir).
- 5-noktalı hareketli ortalama ile hız yumuşatma (noise filtresi).
- GeoPoint model: lat, lng, altitudeM — yükseklik dahil kaydedilir.
- Gait tespiti: `< 6 km/h` → yürüyüş, `6–13 km/h` → tırıs, `> 13 km/h` → dörtnal.
- Elevation profil kartı: Canvas üzerinde çizgi grafik.
- Sürüş sonrası otomatik backend sync (AppFunctionsDataSource.saveRide).

### Ride Detail (Sürüş Detayı)
**Dosya:** `feature/ride/presentation/RideDetailScreen.kt`

- Google Maps üzerinde renk kodlu rota polyline (gait'e göre segment renklendirilir).
- Metrik kartlar: mesafe, süre, ort. hız, maks. hız, toplam yükselti.
- Kalori özeti: binici + at ayrı ayrı.
- Gait dağılım çubuğu (GaitBar): yürüyüş/tırıs/dörtnal yüzdesi.
- ElevationProfileCard: tam Canvas grafiği.

### Training Plans (Antrenman Planları) — Pro
**Dosya:** `feature/training/presentation/TrainingPlansScreen.kt`

- Backend'den çekilen antrenman planı listesi (TrainingPlan + TrainingTask).
- Görev tamamlama (CompleteTrainingTaskUseCase).
- Pro abonelik yoksa SubscriptionScreen'e yönlendirir.

---

## At Yönetimi

### Horse Profile (At Profili)
**Dosya:** `feature/horse/presentation/`

- At listesi (HorseListScreen): kayıtlı atlar kartlar halinde.
- At ekleme (AddHorseScreen): isim, cins (backend'den dinamik liste), yaş, cinsiyet, renk, notlar.
- At silme (DeleteHorseUseCase).
- Her at kartından Sağlık Geçmişi'ne geçiş.

### Horse Health (At Sağlık Geçmişi)
**Dosya:** `feature/horse/presentation/HorseHealthScreen.kt`

- At bazında sağlık olayı listesi.
- Olay tipleri: FARRIER (nalbant, 6 hafta), VACCINE (aşı, yıllık), DENTAL (diş, 6 ay), VET (veteriner, manuel tarih).
- Olayları ekleme ve silme (AddHorseHealthEventUseCase, DeleteHorseHealthEventUseCase).

### Health Calendar (Sağlık Takvimi)
**Dosya:** `feature/health/presentation/HealthScreen.kt`

- Tüm atlar için birleşik takvim görünümü.
- Gecikmiş (isOverdue) ve yaklaşan (isDueSoon — 7 gün içinde) olaylar renk kodlu gösterim.
- Olay ekleme ekranı (AddHealthEventScreen).
- Bakım türü seçimi, tarih seçici, notlar alanı.

---

## Ahır & Sosyal

### Barn Discovery (Ahır Keşfi)
**Dosya:** `feature/barn/presentation/BarnListScreen.kt`, `BarnsMapViewScreen.kt`

- Ahır listesi: isim, konum, rating, kapasitye, hero görsel.
- Arama ve filtre.
- Harita görünümü: Google Maps Compose marker'lar.

### Barn Detail (Ahır Detayı)
**Dosya:** `feature/barn/presentation/BarnDetail.kt`

- Hero görsel, kapasite, telefon.
- Eğitmenler listesi (Instructor modeli).
- Ahıra bağlı dersler: getLessons() gerçek backend çağrısı.
- Ders rezervasyonu: bookLesson() (mocks yok).
- Değerlendirme yazma (WriteReviewScreen'e yönlendirme).
- Ahır sahibi rolündeyse "Ahırı Yönet" butonu → BarnDashboard.

### B2B Ahır Yönetim Modu
**Dosya:** `feature/barnmanagement/presentation/`

Sadece ahır sahibi/yönetici rolündeki kullanıcılara görünür.

- **BarnDashboardScreen:** İstatistikler (toplam ders, toplam rezervasyon, benzersiz öğrenci sayısı, yaklaşan ders sayısı). Ders listesi. Ders oluştur ve roster görüntüle kısayolları.
- **CreateLessonScreen:** Yeni ders oluşturma formu (başlık, tarih, süre, seviye, fiyat, kapasite).
- **LessonRosterScreen:** Dersin öğrenci listesi (StudentRosterEntry: isim, rezervasyon tarihi, durum).

---

## Motivasyon

### Challenge & Rozet Sistemi
**Dosya:** `feature/challenge/presentation/ChallengeScreen.kt`

- Aktif meydan okumalar listesi (GetActiveChallengesUseCase).
- Meydan okuma tipleri: MONTHLY_DISTANCE, WEEKLY_RIDES, SPEED_GOAL, EXPLORE_BARNS.
- Her meydan okuma için ilerleme çubuğu ve kalan gün göstergesi.
- Kazanılan rozetler listesi (GetEarnedBadgesUseCase).
- Rozet talep etme (ClaimBadgeUseCase).

---

## Bildirimler & Güvenlik

### FCM Push Notifications
**Dosya:** `feature/notifications/presentation/NotificationsScreen.kt`

- Firestore realtime listener ile canlı bildirim akışı (mock yok).
- 3 FCM kanalı: `general`, `reservation`, `lesson`.
- FCM token her `onNewToken`'da Firestore'a kaydedilir.
- Okunmamış bildirim sayacı.
- Bildirime tıklama → ilgili rotaya navigasyon (onOpenTargetRoute).
- Tek okundu / tümünü okundu işaretle (MarkNotificationReadUseCase, MarkAllNotificationsReadUseCase).

### Safety Tracking (Güvenlik Takibi)
**Dosya:** `feature/safety/presentation/SafetyScreen.kt`

- Etkinleştirme/devre dışı bırakma toggle.
- Güvenlik kişileri CRUD (AddSafetyContactUseCase, RemoveSafetyContactUseCase).
- Canlı konum paylaşımı güvenlik kişileriyle.
- Otomatik alarm: sürüş sırasında 5 dakika hareketsizlik algılandığında güvenlik kişilerine bildirim (TriggerSafetyAlarmUseCase).
- Settings ekranından tetiklenir.

---

## AI & Kişiselleştirme

### Türkçe AI Koç
**Dosya:** `feature/aicoach/presentation/AiCoachScreen.kt`

- Chat arayüzü: kullanıcı mesajları ve AI yanıtları balon görünümüyle.
- Gemini 1.5 Flash destekli (AskAiCoachUseCase → AppFunctionsDataSource).
- Keyword fallback: backend erişimi yoksa yerel kural tabanlı yanıt.
- Türkçe ve İngilizce soru desteği.
- Profil ekranından erişilir.

### Equestrian Agenda (Federasyon Takvimi)
**Dosya:** `feature/equestrian/presentation/EquestrianAgendaScreen.kt`

- 2 sekme: Duyurular (EquestrianAnnouncement) ve Yarışmalar (EquestrianCompetition).
- Duyuru kartları: başlık, özet, yayın tarihi, görsel, detay linki (harici tarayıcı).
- Yarışma kartları: başlık, konum, tarih, detay linki.
- Federasyon kaynak sağlık durumu (FederationSourceHealthItem): kaynak adı, durum, son başarılı sync, veri yaşı (dakika), bayat veri uyarısı.
- Manuel senkronizasyon butonu (TriggerFederationManualSyncUseCase) — throttle korumalı.
- Bottom sheet: duyuru detayı (tam içerik + dış link).
- Home ekranından kısayol ile açılır.

---

## Hesap & Ayarlar

### Auth (Kimlik Doğrulama)
**Dosya:** `feature/auth/presentation/`

- Google Sign-In (SignInWithGoogleUseCase).
- Email/şifre ile giriş (SignInWithEmailUseCase).
- Email/şifre ile kayıt (SignUpWithEmailUseCase) + email doğrulama.
- Şifremi Unuttum: email ile sıfırlama linki gönderme (SendPasswordResetEmailUseCase).
- Deep link ile şifre sıfırlama tamamlama: `horsegallop.page.link/reset-password` (ConfirmPasswordResetUseCase).
- Şifre sıfırlama deep link formatları: Firebase Dynamic Links + doğrudan oobCode pattern.

### Profil & Düzenleme
**Dosya:** `feature/auth/presentation/ProfileScreen.kt`, `EditProfileScreen.kt`

- Profil görüntüleme: avatar, isim, email, kayıt tarihi.
- Profil düzenleme: isim, biyografi, fotoğraf güncelleme (UpdateUserProfileUseCase, UpdateProfileImageUseCase).
- EditProfileScreen, ProfileViewModel'i parent back-stack entry'den paylaşır.

### Settings (Ayarlar)
**Dosya:** `feature/settings/presentation/SettingsScreen.kt`

- Tema: SYSTEM / LIGHT / DARK (UserSettings.themeMode).
- Dil: SYSTEM / TR / EN (UserSettings.language).
- Bildirimler açık/kapalı toggle.
- Ağırlık birimi: kg / lbs.
- Mesafe birimi: km / mi.
- Güvenlik Takibi sayfasına navigasyon.
- Veri dışa aktarma (RequestDataExportUseCase).
- Hesap silme (DeleteAccountUseCase + DeleteUserDataUseCase) → Onboarding'e yönlendirme.
- Not: Ayarlar modeli var (UserSettings domain), backend entegrasyonu beklemede.

### Onboarding
**Dosya:** `feature/onboarding/presentation/OnboardingScreen.kt`

- Uygulamanın ilk açılışında bir kez gösterilir.
- SharedPreferences ile `onboarding_done` flag'i saklanır.
- "Başla" veya "Atla" ile Login'e yönlendirme.

---

## Monetizasyon

### Pro Abonelik
**Dosya:** `feature/subscription/presentation/SubscriptionScreen.kt`

- Aylık ve yıllık plan seçimi.
- Google Play Billing entegrasyonu (BillingManager singleton, BillingClient wrapper).
- Product ID'ler: `horsegallop_pro_monthly`, `horsegallop_pro_yearly`.
- Satın alma akışı Activity referansı gerektirir (`LocalContext.current as? Activity`).
- Satın almaları geri yükleme (RestorePurchasesUseCase).
- Abonelik durumu Stream (ObserveSubscriptionStatusUseCase).

### Pro Gated Özellikler
- Antrenman Planları (TrainingPlansScreen) — Pro yoksa Subscription'a yönlendirir.
- Gelecekte eklenecek daha fazla özellik.

---

## Gizlilik & Veri

- Veri dışa aktarma isteği (RequestDataExportUseCase → Firebase Cloud Function).
- Tüm kullanıcı verisini silme (DeleteUserDataUseCase — GDPR uyumluluğu).
- Hesap silme: Firebase Auth + Firestore verileri temizlenir.

---

## Teknik Altyapı

### Stack
| Katman | Teknoloji |
|--------|-----------|
| Dil | Kotlin |
| UI | Jetpack Compose + Material3 |
| Navigasyon | Navigation Compose (sealed class `Dest`) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Backend | Firebase Cloud Functions (us-central1), Firestore, Auth, FCM, App Check |
| Harita | Google Maps Compose |
| Görsel | Coil |
| Billing | Google Play Billing |
| AI | Gemini 1.5 Flash (Cloud Function proxy) |

### Mimari
- **Single-module monolith** — sadece `:app` modülü.
- **Katmanlı mimari:** domain (pure Kotlin) → data (Firebase/Retrofit) → feature (Compose UI).
- Tüm remote çağrılar `AppFunctionsDataSource` üzerinden akar.
- Repository pattern: domain interface → data implementasyon → Hilt `@Binds @Singleton`.
- ViewModel: `@HiltViewModel`, `StateFlow<UiState>`, `collectAsStateWithLifecycle()`.

### Design System
- SemanticColors: `LocalSemanticColors.current` — tüm renk erişimi bu token üzerinden.
- Marka paleti: LightBronze, DesertSand, AlmondCream, DrySage, DustyOlive, AshGrey.
- `enforceSemanticSurfaceTokens` build task'ı — feature/core/navigation katmanlarında doğrudan renk kullanımını build-time'da engeller.
- Tam dark mode desteği.

### Firebase Konfigürasyonu
- App Check: debug → `DebugAppCheckProviderFactory`, release → `PlayIntegrityAppCheckProviderFactory`.
- FCM: token her `onNewToken`'da Firestore'a kaydedilir.
- Auth deep link: `horsegallop.page.link/reset-password`.

### Min/Target SDK
- Min SDK: 24 | Target SDK: 34 | JVM: 17

---

## Yol Haritası (Yakın Vadeli)

1. **TJK Yarış Entegrasyonu** — TJK at/jokey istatistikleri, canlı yarış sonuçları, yarış günlüğü. Equilab/Ridely'de bulunmuyor → Türkiye pazarı için kritik farklılaşma. (tjk.org web scraping veya unofficial API araştırması gerekli)
2. **Payments (iyzico / Stripe)** — Google Play Billing skeleton mevcut; native ödeme altyapısı eklenecek.
3. **Settings Backend Senkronizasyonu** — UserSettings domain modeli ve use case'leri hazır; Firebase'e yazma/okuma entegrasyonu yapılacak.
4. **At Sağlık Takvimi Bildirimleri** — Nalbant/aşı/diş hatırlatıcıları için FCM push gönderimi.
5. **Challenge Liderlik Tablosu** — "İstanbul Binicileri" gibi bölge bazlı grup ligleri.
6. **B2B Ahır Yönetimi Genişletme** — Eğitmen notu, öğrenci ilerleme raporu, at sağlık entegrasyonu.
