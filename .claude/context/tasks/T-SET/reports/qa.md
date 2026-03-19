# QA PASS — T-SET: Settings weightUnit/distanceUnit

Kontroller: Semantic PASS | Strings PASS | Build PASS | Lint PASS | Tests PASS | Requirements PASS

---

## 1. Build

```
./gradlew assembleDebug → BUILD SUCCESSFUL (46s)
```

## 2. Tests

```
./gradlew testDebugUnitTest --tests "*Settings*" → BUILD SUCCESSFUL (3m 30s)
```

## 3. Lint

```
./gradlew lintDebug → BUILD SUCCESSFUL, lint error yok
```

## 4. Semantic Token Taramasi

`feature/settings/` altinda `Color.White`, `Color.Black`, `Color(0xFF...`, `colorScheme.surface`, `colorScheme.background` kullanimi bulunamadi.

Tum surface/panel renkleri `LocalSemanticColors.current` tokenlariyla (`semantic.screenBase`, `semantic.cardElevated`, `semantic.cardSubtle`, `semantic.cardStroke`, `semantic.destructive`, `semantic.screenTopBar`) karsilanmis.

## 5. String Kaynak Senkronizasyonu

Asagidaki string'ler **uc dosyada da** mevcuttur:

| String Adi | values/strings_core.xml | values-tr/strings.xml | values-en/strings.xml |
|---|---|---|---|
| `unit_lbs` | 341. satir | 120. satir | 120. satir |
| `unit_mi` | 342. satir | 121. satir | 121. satir |
| `unit_kg` | 373. satir | 363. satir | 361. satir |
| `unit_km` | 339. satir | 344. satir | 342. satir |
| `setting_units_title` | 343. satir | 122. satir | 122. satir |
| `setting_weight_unit_title` | 344. satir | 123. satir | 123. satir |
| `setting_distance_unit_title` | 345. satir | 124. satir | 124. satir |

Not: `values/strings.xml` dosyasinda bu string'ler bulunmuyor; ancak `values/strings_core.xml` ayni `values/` dizininde yer aldigindan default kaynak olarak gecerli sayilir. Build ve lint hata vermeden gecti — kaynak cozumlemesi dogru calisliyor.

## 6. Gereksinim Kontrolü

### onWeightUnitSelected() / onDistanceUnitSelected()
`SettingsViewModel.kt` 94-102. satirlarda her iki metot mevcuttur. Her biri `_unitsState` degerini gunceller ve `syncSettingToBackend()` cagirir.

### syncSettingToBackend() hardcode degeri
`syncSettingToBackend()` (137-167. satirlar) `_unitsState.value` uzerinden `units.weightUnit` ve `units.distanceUnit` okumaktadir. Hardcoded "kg"/"km" gecmez; varsayilan degerler yalnizca `SettingsUnitsUiState` veri sinifinin ilk degerlerinde (47-49. satirlar) ve `syncSettingsFromBackend()` icinde `.ifBlank { "kg" }` / `.ifBlank { "km" }` fallback olarak kullanilmaktadir. Bu kabul edilebilir bir savunmali programlama yaklasimi olup hardcode backend yazimi degil.

### SettingsScreen birim secici UI
`SettingsScreen.kt` 227-268. satirlarda "Units" baslikli `SettingsSectionCard` blogu mevcuttur. Agirlik (kg/lbs) ve mesafe (km/mi) icin `SettingsRadioRow` bilesenlerini icerir; `viewModel.onWeightUnitSelected` ve `viewModel.onDistanceUnitSelected` metodlarina baglidir.

### @Preview
`SettingsScreen.kt` 428-454. satirlarda `@Preview(showBackground = true)` annotasyonlu `SettingsScreenPreview()` fonksiyonu mevcuttur. Preview, ViewModel bagimliligisiz sahte state ile calisir.

## 7. Mimari Uyum

- `domain/` icinde Android import taramasi: Settings domain kodu kontrol edildi, ihlal yok.
- `data/` katmaninda `@Composable` taramasi: Settings data katmaninda Composable yok.

## Degistirilen Dosyalar

- `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsViewModel.kt`
- `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsScreen.kt`
- `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values/strings_core.xml`
- `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values-tr/strings.xml`
- `/Users/gulcintas/HorseGallopProject/horsegallop/app/src/main/res/values-en/strings.xml`
