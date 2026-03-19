# T-HEALTH — Horse Health Polish: Filter, Upcoming Notifications

## Goal
At Sağlık ekranına at bazlı filtre chip'leri ekle; yaklaşan randevular için Android local notification desteği getir.

## Mevcut Durum
- `HealthScreen` + `HealthViewModel` var; CRUD çalışıyor
- Overdue / DueSoon / Normal / Completed sections var
- `filterByHorse(horseId)` ViewModel'de var ama UI'da chip yok
- `HorseListScreen`'den at listesi alınabilir
- Local notification altyapısı yok (FCM var ama health reminder için local alarm gerekli)

## Asıl Eksikler
1. `HealthScreen`'de at bazlı filtre chip UI yok
2. Yaklaşan randevu için local alarm/notification yok
3. `HealthViewModel`'de horse listesi state'i yok (filterByHorse var ama at adları gösterilemiyor)

## Kapsam
- `HealthUiState`'e `horses: List<HorseFilterItem>` ekle (GetHorsesUseCase inject)
- `HealthScreen`'e filtre chip row ekle (LazyRow, at isimleri)
- Android `AlarmManager` + `NotificationManager` ile yaklaşan event bildirimi: `scheduledDate - 24h` öncesi
- Bildirim kanalı: mevcut FCM kanallarına ek olarak `health_reminders` local kanal
- strings: TR + EN ek string'ler

## Kapsam Dışı (MVP)
- FCM push notification (server-side tetikleme)
- Takvim görünümü (liste yeterli)
- Tekrarlayan event (recurring)

## Constraints
- SemanticColors zorunlu
- @Preview güncellenmeli
- NotificationManager için `POST_NOTIFICATIONS` izni AndroidManifest'te kontrol et

## Relevant Paths
- `app/src/main/java/com/horsegallop/feature/health/presentation/HealthScreen.kt`
- `app/src/main/java/com/horsegallop/feature/health/presentation/HealthViewModel.kt`
- `app/src/main/java/com/horsegallop/domain/health/model/HealthEvent.kt`
- `app/src/main/java/com/horsegallop/domain/horse/repository/HorseRepository.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings_core.xml`
- `app/src/main/res/values-tr/strings.xml`
- `app/src/main/res/values-en/strings.xml`

## Agent Sırası
1. `researcher` → Horse model, GetHorsesUseCase, mevcut notification kanalları, HorseListScreen
2. `android-feature` → ViewModel + Screen + AlarmManager entegrasyonu
3. `qa-verifier` → PASS/FAIL

## Acceptance Notes
- Filtre chip'leri: "Tümü" + her at için chip
- Seçili at vurgulanmalı (SemanticColors ile)
- Bildirim kanalı ID: `health_reminders`
- Bildirim sadece gelecekteki eventler için schedule edilmeli

