# T-SET — Settings Backend: weightUnit/distanceUnit UI + Sync Polish

## Goal
Ayarlar ekranına `weightUnit` (kg/lbs) ve `distanceUnit` (km/mi) seçici UI ekle; SettingsViewModel ↔ UserSettings sync'ini uçtan uca doğrula.

## Mevcut Durum
- `UserSettingsRepositoryImpl.getUserSettings()` / `updateUserSettings()` Supabase'e bağlı, çalışıyor
- `UserSettings` domain modelinde `weightUnit: String`, `distanceUnit: String` var
- `SettingsViewModel` theme/language/notifications sync yapıyor ama weightUnit/distanceUnit'i işlemiyor
- `SettingsScreen.kt` — unit seçici UI yok

## Asıl Eksikler
1. `SettingsViewModel`'de weightUnit/distanceUnit state alanları yok
2. `SettingsScreen`'de birim seçici composable yok
3. `UserSettings` değiştiğinde remote'a yazılmıyor (sadece theme/language/notifications yazılıyor)
4. Sync precedence kuralı belirsiz: remote değer her zaman local'ı ezecek mi? (Yes — server wins on init)

## Kapsam
- `SettingsSyncUiState` veya `SettingsState`'e weightUnit/distanceUnit ekle
- `SettingsViewModel.onWeightUnitSelected()` / `onDistanceUnitSelected()` fonksiyonları
- `updateUserSettings` çağrısına weightUnit/distanceUnit dahil et
- `SettingsScreen`'e segmented button veya dialog seçici ekle (kg/lbs, km/mi)
- strings: `strings_core.xml` + `values-tr/` + `values-en/`

## Constraints
- SemanticColors zorunlu
- @Preview ekle
- `SettingsRepository` (proto datastore) doğrudan renk/birim persist etmiyor — WeightUnit/DistanceUnit sadece Supabase'e yazılacak, local cache SettingsViewModel state'de tutulacak

## Relevant Paths
- `app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsScreen.kt`
- `app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsViewModel.kt`
- `app/src/main/java/com/horsegallop/domain/settings/model/UserSettings.kt`
- `app/src/main/java/com/horsegallop/data/settings/repository/UserSettingsRepositoryImpl.kt`
- `app/src/main/res/values/strings_core.xml`
- `app/src/main/res/values-tr/strings.xml`
- `app/src/main/res/values-en/strings.xml`

## Agent Sırası
1. `researcher` → UserSettings model, SettingsRepository proto, mevcut SettingsScreen UI
2. `android-feature` → ViewModel + Screen değişiklikleri
3. `qa-verifier` → PASS/FAIL

## Acceptance Notes
- Remote sync başarısız olursa SnackBar gösterilmeli
- Birim değişikliği anında syncSettingToBackend çağırmalı
- weightUnit: "kg" | "lbs" — distanceUnit: "km" | "mi"

