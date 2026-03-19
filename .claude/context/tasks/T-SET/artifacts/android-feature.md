# T-SET Android Feature Artifact

## STATUS: DONE

## Yapılan Değişiklikler

### 1. SettingsViewModel.kt
**Dosya:** `app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsViewModel.kt`

Yapılan değişiklikler:

**a) Yeni `SettingsUnitsUiState` data class eklendi (satır 46-49):**
```kotlin
data class SettingsUnitsUiState(
    val weightUnit: String = "kg",
    val distanceUnit: String = "km"
)
```

**b) `_unitsState` MutableStateFlow ve `unitsState` public StateFlow eklendi (satır 69-70):**
```kotlin
private val _unitsState = MutableStateFlow(SettingsUnitsUiState())
val unitsState: StateFlow<SettingsUnitsUiState> = _unitsState.asStateFlow()
```

**c) `onWeightUnitSelected` / `onDistanceUnitSelected` metodları eklendi (satır 94-102):**
```kotlin
fun onWeightUnitSelected(unit: String) {
    _unitsState.value = _unitsState.value.copy(weightUnit = unit)
    syncSettingToBackend()
}

fun onDistanceUnitSelected(unit: String) {
    _unitsState.value = _unitsState.value.copy(distanceUnit = unit)
    syncSettingToBackend()
}
```

**d) `syncSettingsFromBackend()` güncellendi — remote değerleri `_unitsState`'e yazıyor (satır 120-123):**
```kotlin
_unitsState.value = SettingsUnitsUiState(
    weightUnit = remote.weightUnit.lowercase().ifBlank { "kg" },
    distanceUnit = remote.distanceUnit.lowercase().ifBlank { "km" }
)
```

**e) `syncSettingToBackend()` bug fix — `weightUnit`/`distanceUnit` artık `_unitsState`'den okunuyor (satır 141-153):**
```kotlin
val units = _unitsState.value
updateUserSettingsUseCase(
    UserSettings(
        themeMode = state.themeMode.id.uppercase(),
        language = state.language.id.uppercase(),
        notificationsEnabled = state.notificationsEnabled,
        weightUnit = units.weightUnit,      // KRİTİK BUG FIX
        distanceUnit = units.distanceUnit   // KRİTİK BUG FIX
    )
)
```

---

### 2. SettingsScreen.kt
**Dosya:** `app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsScreen.kt`

Yapılan değişiklikler:

- `Icons.Filled.Straighten` import eklendi
- `unitsState` collectAsState() ile toplandı
- Notifications kartından sonra, Privacy kartından önce yeni `SettingsSectionCard("Birimler")` eklendi
- Kart içinde: ağırlık başlığı + kg/lbs radio rows, mesafe başlığı + km/mi radio rows
- `settingsControlsEnabled` flag'i tüm birim satırlarına uygulandı
- `@Preview` güncellendi — Units kartını da gösteren, ViewModel bağımlılığı olmayan preview

---

### 3. String Resources — 3 Dosya

**`app/src/main/res/values/strings_core.xml`:**
```xml
<string name="unit_lbs">lbs</string>
<string name="unit_mi">mi</string>
<string name="setting_units_title">Units</string>
<string name="setting_weight_unit_title">Weight Unit</string>
<string name="setting_distance_unit_title">Distance Unit</string>
```

**`app/src/main/res/values-tr/strings.xml`:**
```xml
<string name="unit_lbs">lbs</string>
<string name="unit_mi">mi</string>
<string name="setting_units_title">Ölçüm Birimleri</string>
<string name="setting_weight_unit_title">Ağırlık Birimi</string>
<string name="setting_distance_unit_title">Mesafe Birimi</string>
```

**`app/src/main/res/values-en/strings.xml`:**
```xml
<string name="unit_lbs">lbs</string>
<string name="unit_mi">mi</string>
<string name="setting_units_title">Units</string>
<string name="setting_weight_unit_title">Weight Unit</string>
<string name="setting_distance_unit_title">Distance Unit</string>
```

---

## Build Sonucu

`./gradlew compileDebugKotlin` → **BUILD SUCCESSFUL** (7m 8s)
Yalnızca pre-existing deprecation warning'ler, yeni hata yok.

---

## Kural Uyumu

| Kural | Durum |
|-------|-------|
| SemanticColors — direkt renk yok | UYUMLU — tüm renkler `MaterialTheme.colorScheme.*` veya `semantic.*` |
| `@Preview` — ViewModel bağımlılığı olmadan | UYUMLU — preview güncellendi, fake state ile çalışıyor |
| String yasağı — hardcoded string yok | UYUMLU — tüm stringler 3 dosyaya eklendi |
| `LaunchedEffect` içinde `stringResource()` yok | UYUMLU — screen'de mevcut LaunchedEffect blokları değiştirilmedi, yeni kod LaunchedEffect dışında |

---

## Mimari Notlar

- `weightUnit`/`distanceUnit` brief constraint gereği `SettingsRepository` (SharedPrefs) yerine ViewModel state'de tutuldu
- Server-wins: remote'dan gelen değerler `syncSettingsFromBackend()` içinde `_unitsState`'i eziyor
- Error feedback: sync hatası mevcut `saveErrorMessageResId` + `consumeSaveError()` mekanizması üzerinden SnackBar gösteriyor (yeni kod gerekmedi)
- `SettingsState` (`com.horsegallop.settings.SettingsModels.kt`) değiştirilmedi — brief constraint
