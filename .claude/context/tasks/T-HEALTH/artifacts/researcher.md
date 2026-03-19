# T-HEALTH Researcher Artifact

## 1. Problem Ozeti

HealthScreen CRUD calisiyor. Eksik olan iki sey:
1. At bazli filtre chip UI — `filterByHorse` ViewModel'de var, ama `HealthUiState`'te `horses` listesi ve ekranda chip row yok.
2. Local bildirim (AlarmManager) — yaklaşan etkinlikler icin hicbir alarm/notification altyapisi yok.

---

## 2. Mevcut Repo Gercegi

### Soru 1 — `HealthUiState` icinde `horses` listesi veya `selectedHorseId` var mi?

`selectedHorseId: String? = null` VAR — satir 23, `HealthViewModel.kt`.
`horses: List<...>` YOK — UiState icinde at listesi tutulmuyor.

```
// HealthViewModel.kt satir 19-25
data class HealthUiState(
    val loading: Boolean = true,
    val events: List<HealthEvent> = emptyList(),
    val error: String? = null,
    val selectedHorseId: String? = null,   // <-- var
    val isSaving: Boolean = false
)
// horses listesi YOK
```

### Soru 2 — `filterByHorse(horseId)` ViewModel'de nasil calisiyor?

`HealthViewModel.kt` satir 57-60:
- `selectedHorseId`'yi gunceller, ardindan `load()` cagirir.
- `load()` (satir 50): `getHealthEventsUseCase(_ui.value.selectedHorseId)` seklinde zaten filtre parametresini geciyor.
- Yani backend filtresi calisir; eksik olan sadece UI'daki chip listesi icin at isimlerini tutacak state.

### Soru 3 — `GetHorsesUseCase` veya `HorseRepository.getHorses()` HealthViewModel'e inject edilmis mi?

HAYIR — `HealthViewModel` konstruktoru sadece sunlari alir (satir 28-33):
- `getHealthEventsUseCase`
- `saveHealthEventUseCase`
- `deleteHealthEventUseCase`
- `getCurrentUserIdUseCase`

`GetMyHorsesUseCase` inject EDILMEMIS.

Kullanilacak seyler:
- Sinif: `com.horsegallop.domain.horse.usecase.GetMyHorsesUseCase` (dosya: `domain/horse/usecase/GetMyHorsesUseCase.kt`)
- Imza: `operator fun invoke(): Flow<List<Horse>>` — parametresiz, `Flow` dondurur
- Model: `Horse(id, name, breed, ...)` — sadece `id` ve `name` filtre icin yeterli

### Soru 4 — `POST_NOTIFICATIONS` izni Manifest'te var mi?

EVET — `AndroidManifest.xml` satir 8:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
API 33+ runtime permission istegi icin manifest hazir.

### Soru 5 — Mevcut FCM kanallari neler? `health_reminders` kanalı var mi?

`PushService.kt` satir 114-118'deki companion object:
```kotlin
const val CHANNEL_GENERAL    = "horsegallop_general"
const val CHANNEL_RESERVATION = "horsegallop_reservation"
const val CHANNEL_LESSON     = "horsegallop_lesson"
```

`health_reminders` kanalı MEVCUT DEGIL — builder tarafindan yeni olusturulmalidir.

Manifest'te default kanal `horsegallop_general` olarak tanimlanmis (satir 47-49).

### Soru 6 — `SCHEDULE_EXACT_ALARM` veya `USE_EXACT_ALARM` izni var mi? (API 31+)

HAYIR — AndroidManifest'te bu iki izinden HICBIRI yok.

API 31+ cihazlarda `setExact()` veya `setExactAndAllowWhileIdle()` kullanilacaksa:
- `android.permission.SCHEDULE_EXACT_ALARM` (API 31-32, otomatik verilir ama manifest'e eklenmeli)
- `android.permission.USE_EXACT_ALARM` (API 33+, uygulama kategori sinirlamasi var)

Alternatif olarak `setWindow()` veya inexact alarm (`set()`) kullanilabilir; 24 saatlik onceden uyari icin dakika hassasiyeti gerekmiyor, bu yuzden inexact kabul edilebilir. Ancak brief'te `scheduledDate - 24h` oncesi denildigi icin `setExactAndAllowWhileIdle` tercih edilebilir — bu durumda manifest'e eklenmeli.

### Soru 7 — `HealthEvent.scheduledDate` turu nedir?

`domain/health/model/HealthEvent.kt` satir 16:
```kotlin
val scheduledDate: Long   // epoch ms
```
`Long` — epoch millisecond. `LocalDate` veya `LocalDateTime` DEGIL. Dogrudan AlarmManager'a gecilebilir.

---

## 3. Dis Kaynaklar veya Kontratlar

- `GetHealthEventsUseCase` imzasi: `operator fun invoke(horseId: String?): Flow<List<HealthEvent>>`  (ViewModel satir 50'den cikartildi — filtre parametresi aliyor)
- `HorseRepository.getMyHorses()`: `Flow<List<Horse>>` dondurur, parametresiz
- `AddHealthEventScreen.kt` zaten `HorseViewModel` inject ediyor ve `horseUiState.horses` listesini dropdown icin kullaniyor. Ayni at listesi `HealthViewModel`'e de eklenebilir (ya da Screen katmaninda `HorseViewModel` paylasimi yapilabilir — ancak brief ViewModel'de tutmayi oneriyor)

---

## 4. Riskler / Bilinmeyenler

| Risk | Detay | Oneri |
|------|-------|-------|
| API 33+ notification permission | `POST_NOTIFICATIONS` manifest'te var; runtime'da `ActivityResultContracts.RequestPermission` ile istenmeli | Builder Screen'e permission launcher eklemeli |
| Exact alarm API 31-33 farki | `SCHEDULE_EXACT_ALARM` manifest'te eksik; inexact alarm (setWindow 23h-25h arasi) daha guvenli | Builder inexact kullansin veya manifest'e izni eklesin |
| AlarmManager survive reboot | `HealthReminderReceiver` reboot'ta kaybolur; `RECEIVE_BOOT_COMPLETED` + `BootReceiver` gerekirdi | MVP kapsam disi, brief'te belirtilmemis — builder notu dusmeli |
| `selectedHorseId` null = "Tumü" | `filterByHorse(null)` zaten `load()`'u tetikliyor — null = filtre yok davranisi dogru | Chip "Tümü" = `filterByHorse(null)` cagirisi |
| `GetHealthEventsUseCase` imzasi | ViewModel satir 50'den goruluyor ama dosya okunmadi; imza tahmin edildi | Builder import kontrolu yapmali |

---

## 5. Builder icin Net Girdiler

### HealthViewModel degisiklikleri

1. `GetMyHorsesUseCase` constructor'a inject et:
   - Import: `com.horsegallop.domain.horse.usecase.GetMyHorsesUseCase`
   - Model: `com.horsegallop.domain.horse.model.Horse`

2. `HealthUiState`'e alan ekle:
   ```kotlin
   val horses: List<Horse> = emptyList()
   ```

3. `init` bloguna at listesi collect'i ekle:
   ```kotlin
   viewModelScope.launch {
       getMyHorsesUseCase().collect { list ->
           _ui.update { it.copy(horses = list) }
       }
   }
   ```

### HealthScreen degisiklikleri

4. `HealthScreen` ust kismina at filtre chip row ekle (LazyRow):
   - Ilk chip: "Tümü" — `onClick = { viewModel.filterByHorse(null) }`
   - Diger chipler: `uiState.horses` uzerinden — `onClick = { viewModel.filterByHorse(horse.id) }`
   - Secili chip: `uiState.selectedHorseId == horse.id` → vurgu rengi `LocalSemanticColors.current` ile

5. LazyColumn'a `key()` ve `contentType()` zaten var (overdue_, soon_, normal_, done_ prefix'leri ile) — degistirme.

### Local Notification (AlarmManager)

6. Yeni dosyalar olusturulacak:
   - `feature/health/notification/HealthReminderReceiver.kt` — `BroadcastReceiver`
   - Manifest'e `<receiver>` kaydi

7. Manifest'e izin ekle (inexact tercih edilirse gerekmez; exact kullanilacaksa):
   ```xml
   <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
   ```

8. Bildirim kanal ID: `health_reminders` (PushService'teki 3 kanaldan AYRI, yeni tanimlanacak)

9. `saveEvent` cagrisinin ardindan alarm kurulmali: `scheduledDate - 24 * 60 * 60 * 1000L` — bu deger pozitifse (gelecekte) alarm set edilmeli, yoksa skip.

10. `delete(eventId)` cagrisinda AlarmManager.cancel() yapilmali.

### Kapsam Notlari

- Reboot survival (RECEIVE_BOOT_COMPLETED) kapsam disi, builder notu dusmeli.
- `collectAsStateWithLifecycle()` kullanilmali — HealthScreen satir 82'de `collectAsState()` var, `collectAsStateWithLifecycle` ile degistirilmeli (Lifecycle 2.6+ mevcut).
- `@Preview` guncellenmeli: `horses` listesi fake data ile doldurulmali.
