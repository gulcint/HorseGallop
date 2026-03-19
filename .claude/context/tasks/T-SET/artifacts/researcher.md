# T-SET Research Artifact

## 1. Problem Ozeti

`UserSettings` domain modelinde `weightUnit` ve `distanceUnit` alanlari var ve Supabase'e okunup yaziliyor. Ancak `SettingsState` / `SettingsRepository` bu alanlari hic tutmuyor; `SettingsViewModel` sync sirasinda bu alanlari ne remote'dan cekiyor ne de remote'a gonderiyor. `SettingsScreen`'de birim secici UI yok.

---

## 2. Mevcut Repo Gercegi

### Soru 1 — `weightUnit` / `distanceUnit` `SettingsState`'de var mi?

**Cevap: HAYIR.**

`SettingsState` (`SettingsModels.kt` sat. 29-33) yalnizca su alanlari tutuyor:

```
themeMode: ThemeMode
language: AppLanguage
notificationsEnabled: Boolean
```

`weightUnit` ve `distanceUnit` `SettingsState`'de **yok**. Sadece `UserSettings` domain modelinde (`UserSettings.kt` sat. 3-9) ve `UserSettingsRepositoryImpl` DTO donusumunde (`UserSettingsRepositoryImpl.kt` sat. 17-23, 28-35) var.

`SettingsViewModel`'deki `uiState` (`SettingsViewModel.kt` sat. 57) direkt `SettingsRepository.state` (yani `StateFlow<SettingsState>`) akisidir — bu yuzden ViewModel katmaninda da `weightUnit`/`distanceUnit` yok.

---

### Soru 2 — `updateUserSettings()` tam imzasi

`UpdateUserSettingsUseCase.invoke()` (`UpdateUserSettingsUseCase.kt` sat. 10-12):

```kotlin
suspend operator fun invoke(settings: UserSettings): Result<Unit>
```

`UserSettingsRepositoryImpl.updateUserSettings()` (`UserSettingsRepositoryImpl.kt` sat. 26-37):

```kotlin
override suspend fun updateUserSettings(settings: UserSettings): Result<Unit>
```

`updateUserSettings` cagrisinda DTO'ya tum `UserSettings` alanlari yaziliyor:
- `themeMode`, `language`, `notificationsEnabled`, `weightUnit`, `distanceUnit`

**Kritik Bulgu:** `SettingsViewModel.syncSettingToBackend()` (`SettingsViewModel.kt` sat. 116-143) `UserSettings` olusturken `weightUnit` ve `distanceUnit` icin sabit degerleri degil, SettingsState'den okunan bir deger yok; mevcut kodda bu alanlar varsayilan degerleriyle `UserSettings` data class default'larina dusuyor ("kg" ve "km"). Yani her kaydetme islemi bu iki alani **sifirlayabilir** (default'a dusurebilir).

`SettingsViewModel.kt` sat. 125-131 — mevcut cagri:
```kotlin
updateUserSettingsUseCase(
    UserSettings(
        themeMode = state.themeMode.id.uppercase(),
        language = state.language.id.uppercase(),
        notificationsEnabled = state.notificationsEnabled
        // weightUnit ve distanceUnit eksik — data class default'lari "kg" ve "km" yazilir
    )
)
```

---

### Soru 3 — Mevcut UI Pattern

`SettingsScreen.kt` uc privat composable kullandigi goruldu:

| Composable | Kullanim Yeri | Satir |
|---|---|---|
| `SettingsRadioRow` | Theme (SYSTEM/LIGHT/DARK), Language secimi | 167-209 |
| `SettingsSwitchRow` | Notifications toggle | 217-222 |
| `SettingsActionRow` | Privacy export + hesap silme (Button ile) | 230-247 |

Birim secici icin **`SettingsRadioRow` pattern uygundur** — Theme secicisiyle bire bir ayni yapiyi kullanabilir. Alternatif olarak `SettingsSectionCard` icinde iki radio row (kg/lbs veya km/mi) yeterli. Yeni bir `SettingsSectionCard` blogu, notifications blogundan sonra ve privacy blogundan once eklenmeli (sat. 224-247 arasina).

Icon onerisiz (mevcut bloklar: `Palette`, `Language`, `Notifications`, `Shield`) — `Straighten` veya `Speed` ikonu uygun olabilir ancak bu builder karari.

---

### Soru 4 — String Resource Konumu

Ayarlarla ilgili string'ler `strings_core.xml`'de degil, **hem `strings_core.xml` hem de `values-tr/strings.xml` ve `values-en/strings.xml`'de** paralel olarak tutuluyor.

Mevcut `setting_` prefix'li key'ler:

| Dosya | Satir Araligi | Key'ler |
|---|---|---|
| `values/strings_core.xml` | 66-79 | `setting_theme_title`, `setting_language_title`, `setting_language_system/english/turkish`, `setting_notifications_title`, `setting_notifications_subtitle`, `setting_privacy_title` |
| `values-tr/strings.xml` | 101-114 | Ayni key'lerin Turkce karsiliklari |
| `values-en/strings.xml` | 101-114 | Ayni key'lerin Ingilizce karsiliklari |

Mevcut `unit_` prefix'li key'ler (`strings_core.xml` sat. 338-368):
- `unit_kcal`, `unit_km`, `unit_kmh`, `unit_kg` var
- `unit_lbs` ve `unit_mi` **yok** — eklenmesi gerekiyor

**Yeni eklenmesi gereken key'ler:**
- `strings_core.xml`, `values-tr/strings.xml`, `values-en/strings.xml` uclune de eklenmeli:
  - `setting_units_title` (Olcum Birimleri / Units)
  - `setting_weight_unit_title` (Agirlik Birimi / Weight Unit)
  - `setting_distance_unit_title` (Mesafe Birimi / Distance Unit)
  - `unit_lbs` → "lbs"
  - `unit_mi` → "mi"

---

### Soru 5 — `syncSettingToBackend` Metodu

**Evet, mevcut.** `SettingsViewModel.kt` sat. 116'da `private fun syncSettingToBackend()` tanimli.

Akis:
1. Onceki `saveJob` iptal edilir (debounce)
2. `SettingsSyncUiState.isSaving = true` set edilir
3. `updateUserSettingsUseCase(UserSettings(...))` suspend cagrilir
4. Basarida `isSaving = false`, hata durumunda `saveErrorMessageResId` doldurulur

`syncSettingToBackend`, `onThemeSelected`, `onLanguageSelected`, `onNotificationsChanged` tarafindan cagrilir. `weightUnit`/`distanceUnit` icin benzer `onWeightUnitSelected()` / `onDistanceUnitSelected()` fonksiyonlari eklenmeli.

---

## 3. Dis Kaynaklar / Kontratlar

- `SettingsRepository` (`SettingsRepository.kt`) **sadece** `themeMode`, `language`, `notificationsEnabled` alanlari icin SharedPreferences persist ediyor. `replaceState()` de yalnizca bu uc alani kabul ediyor (sat. 39-49).
- `weightUnit`/`distanceUnit` `SettingsRepository`'e **eklenmeyecek** (brief constraint: "WeightUnit/DistanceUnit sadece Supabase'e yazilacak, local cache SettingsViewModel state'de tutulacak").
- Supabase `SupabaseUserSettingsDto` zaten her iki alani iceriyor — backend degisikligi gerekmez.

---

## 4. Riskler / Bilinmeyenler

| Risk | Aciklama | Onem |
|---|---|---|
| **Veri kaybi riski (kritik)** | Mevcut `syncSettingToBackend` her kaydetmede `weightUnit="kg"` ve `distanceUnit="km"` hard-code'u gonderiyor (data class default). Kullanici daha once "lbs"/"mi" secmisse, ilk theme/language degistiriginde bu deger ezilir. | YUKSEK |
| **State tutarsizligi** | `syncSettingsFromBackend` remote'dan `weightUnit`/`distanceUnit` okuyup `settingsRepository.replaceState()`'e gonderiyor, ancak `replaceState` bu alanlari almadigi icin remote deger `SettingsState`'e hic yazilmiyor. | YUKSEK |
| **`SettingsRepository.listener` eksikligi** | `SharedPreferences.OnSharedPreferenceChangeListener` sadece `KEY_THEME_MODE`, `KEY_LANGUAGE`, `KEY_NOTIFICATIONS` key'lerini izliyor. Yeni key eklenirse listener'in guncellenmesi gerekmez (birimler SharedPrefs'e yazilmayacak) — bu bir risk degil ama builder'in bilmesi gerekir. | DUSUK |
| **Locale-aware unit label** | `contentState` (backend'den dinamik alt basliklar) birim bilgisi icermiyor. Builder'in `SettingsContentUiState`'e `unitsSubtitle` eklemesi gerekip gerekemedigi belirsiz — muhtemelen gerek yok, statik string yeterli. | DUSUK |

---

## 5. Builder icin Net Girdiler

### Degistirilen / Eklenmesi Gereken Dosyalar

1. **`SettingsViewModel.kt`**
   - `SettingsSyncUiState` veya ayri bir data class'a `weightUnit: String = "kg"` ve `distanceUnit: String = "km"` alanlari ekle (MutableStateFlow ile ViewModel icinde yonet)
   - `syncSettingsFromBackend().onSuccess` bloguna remote `weightUnit`/`distanceUnit` alip ViewModel state'ini guncelle
   - `syncSettingToBackend()` cagrisinda `UserSettings` olusturulurken `weightUnit` ve `distanceUnit` alanlari ViewModel state'inden okunmali (sat. 125-131 guncellenmeli)
   - `fun onWeightUnitSelected(unit: String)` ekle — state'i guncelle, `syncSettingToBackend()` cagir
   - `fun onDistanceUnitSelected(unit: String)` ekle — state'i guncelle, `syncSettingToBackend()` cagir

2. **`SettingsScreen.kt`**
   - Notifications blogu (sat. 212-223) ile Privacy blogu (sat. 225) arasina yeni `SettingsSectionCard` ekle
   - Kart icinde `SettingsRadioRow` x2 (kg / lbs) ve `SettingsRadioRow` x2 (km / mi) — veya tek karti ikiye (weight + distance) bolmek de okunabilir
   - `state` (veya ViewModel'den ayri StateFlow) `weightUnit`/`distanceUnit` degerini basmali
   - `settingsControlsEnabled` flag'i birim satirlarina da uygulanmali

3. **`strings_core.xml`** (ve `values-tr/` + `values-en/` paralel guncellemeler)
   - `unit_lbs` → "lbs"
   - `unit_mi` → "mi"
   - `setting_units_title` → "Units" / "Birimler"
   - (Opsiyonel) `setting_weight_unit_title`, `setting_distance_unit_title`

4. **Degistirilmeyecek dosyalar:**
   - `UserSettings.kt` — tam, degisiklik gerekmez
   - `UserSettingsRepositoryImpl.kt` — tam, degisiklik gerekmez
   - `UpdateUserSettingsUseCase.kt` — tam, degisiklik gerekmez
   - `SettingsRepository.kt` — `weightUnit`/`distanceUnit` buraya eklenmeyecek (brief constraint)

### Deger Sabitleri (Builder icin)

```
weightUnit gecerli degerler  : "kg" | "lbs"
distanceUnit gecerli degerler: "km" | "mi"
Varsayilan                   : "kg", "km"
Sync strateji                : server wins on initial load — remote deger ViewModel state'ini ezer
```

### Kural Hatirlatma

- `LocalSemanticColors.current` kullan, direkt renk yasak
- Her yeni Composable'a `@Preview(showBackground = true)` ekle
- Yeni string key'ler uc dosyaya (core + tr + en) eklenmeli
