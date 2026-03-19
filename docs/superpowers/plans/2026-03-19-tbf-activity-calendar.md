# TBF Faaliyet Takvimi Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `EquestrianAgendaScreen`'e ek olarak, TBF (Türkiye Binicilik Federasyonu) etkinliklerini aylık takvim grid + kart listesi şeklinde gösteren yeni bir ekran ekle.

**Architecture:** Mevcut `domain/equestrian` + `data/equestrian` + `feature/equestrian` katmanları genişletilir. Yeni `tbf_activities` Supabase tablosu, ayrı domain model (`TbfActivity`), bağımsız ViewModel ve custom `CalendarGrid` Compose bileşeni. `equestrian_competitions` tablosuna dokunulmaz.

**Tech Stack:** Kotlin + Jetpack Compose + Hilt + Supabase PostgREST + kotlinx.serialization + `java.time.LocalDate` (coreLibraryDesugaring aktif)

---

## Chunk 1: Veritabanı + Domain Katmanı

### Task 1: coreLibraryDesugaring Kontrolü

**Files:**
- Check: `app/build.gradle.kts`

- [ ] **Step 1:** `app/build.gradle.kts` içinde `isCoreLibraryDesugaringEnabled = true` satırını kontrol et. Yoksa ekle:
```kotlin
compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
// dependencies bloğuna:
coreLibraryDesugaring(libs.desugar.jdk.libs)
```
- [ ] **Step 2:** `gradle/libs.versions.toml` içinde `desugar.jdk.libs` var mı kontrol et. Yoksa ekle:
```toml
[versions]
desugar-jdk-libs = "2.0.4"
[libraries]
desugar-jdk-libs = { module = "com.android.tools.desugar_jdk_libs", version.ref = "desugar-jdk-libs" }
```
- [ ] **Step 3:** `./gradlew assembleDebug` — BUILD SUCCESSFUL

---

### Task 2: Supabase Migration — tbf_activities tablosu

**Files:**
- Create: `supabase/migrations/20260319000001_tbf_activities.sql`

- [ ] **Step 1:** Migration dosyası oluştur:
```sql
CREATE TABLE IF NOT EXISTS tbf_activities (
    id TEXT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    title TEXT NOT NULL DEFAULT '',
    organization TEXT NOT NULL DEFAULT '',
    city TEXT NOT NULL DEFAULT '',
    discipline TEXT NOT NULL,
    activity_type TEXT NOT NULL,
    detail_url TEXT DEFAULT '',
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tbf_activities_start_date
    ON tbf_activities(start_date);
CREATE INDEX IF NOT EXISTS idx_tbf_activities_discipline
    ON tbf_activities(discipline);

-- RLS: herkese okunabilir (public data)
ALTER TABLE tbf_activities ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read tbf_activities"
    ON tbf_activities FOR SELECT USING (true);
```

- [ ] **Step 2:** Migration uygula:
```bash
~/bin/supabase db push
```
Expected: `Applied 1 migration`

---

### Task 3: Seed Data — mock etkinlikler

**Files:**
- Create: `supabase/seed_tbf.sql`

- [ ] **Step 1:** Seed dosyası oluştur (Mart-Nisan 2026 gerçek TBF verisi):
```sql
INSERT INTO tbf_activities (id, start_date, end_date, title, organization, city, discipline, activity_type) VALUES
('tbf-001', '2026-03-19', '2026-03-22', 'ANTALYA ATLI SPOR KULÜBÜ ENGEL ATLAMA YARIŞMALARI', 'ABAK', 'ANTALYA', 'show_jumping', 'incentive'),
('tbf-002', '2026-03-21', '2026-03-22', 'NEVRUZ KUPASI', 'HAL TEKE ATLI SPOR KULÜBÜ', 'ANKARA', 'show_jumping', 'cup'),
('tbf-003', '2026-03-27', '2026-03-29', 'TBF ULUSAL ATLI DAYANIKLILIK KALİFİKASYON', 'TBF', 'İSTANBUL', 'endurance', 'incentive'),
('tbf-004', '2026-03-28', '2026-03-29', 'TÜRKİYE LİGİ 3. AYAK', 'TBF', 'ANTALYA', 'show_jumping', 'incentive'),
('tbf-005', '2026-04-04', '2026-04-05', 'İSTANBUL AT TERBİYESİ ŞAMPİYONASI', 'ÖZEL MANEJ', 'İSTANBUL', 'dressage', 'championship'),
('tbf-006', '2026-04-11', '2026-04-12', 'EGE BÖLGE KUPASI', 'EGE ATLI', 'İZMİR', 'show_jumping', 'cup'),
('tbf-007', '2026-04-18', '2026-04-19', 'PONY LİG 2. AYAK', 'TBF', 'BURSA', 'pony', 'incentive'),
('tbf-008', '2026-04-25', '2026-04-26', 'ULUSLARARASI İSTANBUL CHS', 'TBF', 'İSTANBUL', 'show_jumping', 'international')
ON CONFLICT (id) DO NOTHING;
```

- [ ] **Step 2:** Seed'i uygula:
```bash
~/bin/supabase db reset --linked  # sadece seed'i push etmek için
# VEYA direkt SQL çalıştır:
~/bin/supabase db execute --file supabase/seed_tbf.sql
```

---

### Task 4: Domain Model

**Files:**
- Create: `app/src/main/java/com/horsegallop/domain/equestrian/model/TbfActivity.kt`

- [ ] **Step 1:** Test yaz:
```kotlin
// app/src/test/java/com/horsegallop/domain/equestrian/model/TbfActivityTest.kt
class TbfActivityTest {
    @Test
    fun `TbfDiscipline fromString maps show_jumping correctly`() {
        assertEquals(TbfDiscipline.SHOW_JUMPING, TbfDiscipline.fromString("show_jumping"))
    }
    @Test
    fun `TbfDiscipline fromString returns OTHER for unknown`() {
        assertEquals(TbfDiscipline.OTHER, TbfDiscipline.fromString("bilinmeyen"))
    }
    @Test
    fun `TbfActivity isMultiDay true when startDate != endDate`() {
        val activity = TbfActivity(
            id = "1", startDate = LocalDate.of(2026, 3, 19), endDate = LocalDate.of(2026, 3, 22),
            title = "Test", organization = "TBF", city = "Ankara",
            discipline = TbfDiscipline.SHOW_JUMPING, type = TbfActivityType.INCENTIVE
        )
        assertTrue(activity.isMultiDay)
    }
}
```

- [ ] **Step 2:** Testi çalıştır → FAIL beklenir

- [ ] **Step 3:** Domain model yaz:
```kotlin
// app/src/main/java/com/horsegallop/domain/equestrian/model/TbfActivity.kt
import java.time.LocalDate

data class TbfActivity(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val title: String,
    val organization: String,
    val city: String,
    val discipline: TbfDiscipline,
    val type: TbfActivityType,
    val detailUrl: String = ""
) {
    val isMultiDay: Boolean get() = startDate != endDate
    val dateLabel: String get() = if (isMultiDay)
        "${startDate.dayOfMonth}-${endDate.dayOfMonth} ${startDate.month.turkishName()} ${startDate.year}"
    else
        "${startDate.dayOfMonth} ${startDate.month.turkishName()} ${startDate.year}"
}

enum class TbfDiscipline(val displayNameTr: String, val colorKey: String) {
    SHOW_JUMPING("Engel Atlama", "primary"),
    ENDURANCE("Atlı Dayanıklılık", "success"),
    DRESSAGE("At Terbiyesi", "info"),
    PONY("Pony", "gaitWalk"),
    VAULTING("Atlı Cimnastik", "warning"),
    EVENTING("Üç Günlük", "ratingStar"),
    OTHER("Diğer", "cardStroke");

    companion object {
        fun fromString(value: String): TbfDiscipline = entries.firstOrNull {
            it.name.lowercase() == value.lowercase().replace(" ", "_")
        } ?: OTHER
    }
}

enum class TbfActivityType(val displayNameTr: String) {
    INTERNATIONAL("Uluslararası"),
    CHAMPIONSHIP("Şampiyona"),
    CUP("Kupa"),
    INCENTIVE("Teşvik"),
    EDUCATION("Eğitim"),
    CATEGORY_EXAM("Kategori Sınavı"),
    SEMINAR("Seminer"),
    CONFERENCE("Konferans"),
    WORKSHOP("Çalıştay"),
    OTHER("Diğer");

    companion object {
        fun fromString(value: String): TbfActivityType = entries.firstOrNull {
            it.name.lowercase() == value.lowercase()
        } ?: OTHER
    }
}

// Extension — Türkçe ay adı
private fun java.time.Month.turkishName(): String = when (this) {
    java.time.Month.JANUARY -> "Ocak"
    java.time.Month.FEBRUARY -> "Şubat"
    java.time.Month.MARCH -> "Mart"
    java.time.Month.APRIL -> "Nisan"
    java.time.Month.MAY -> "Mayıs"
    java.time.Month.JUNE -> "Haziran"
    java.time.Month.JULY -> "Temmuz"
    java.time.Month.AUGUST -> "Ağustos"
    java.time.Month.SEPTEMBER -> "Eylül"
    java.time.Month.OCTOBER -> "Ekim"
    java.time.Month.NOVEMBER -> "Kasım"
    java.time.Month.DECEMBER -> "Aralık"
}
```

- [ ] **Step 4:** Testi çalıştır → PASS beklenir
```bash
./gradlew testDebugUnitTest --tests "*.TbfActivityTest" -q
```

- [ ] **Step 5:** Commit
```bash
git add supabase/migrations/20260319000001_tbf_activities.sql \
        supabase/seed_tbf.sql \
        app/src/main/java/com/horsegallop/domain/equestrian/model/TbfActivity.kt \
        app/src/test/java/com/horsegallop/domain/equestrian/model/TbfActivityTest.kt
git commit -m "feat(tbf): domain model + Supabase migration"
```

---

## Chunk 2: Data Katmanı + Repository

### Task 5: SupabaseDtos — TbfActivityDto

**Files:**
- Modify: `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDtos.kt`

- [ ] **Step 1:** `SupabaseDtos.kt` sonuna ekle:
```kotlin
@Serializable
data class SupabaseTbfActivityDto(
    val id: String,
    @SerialName("start_date") val startDate: String,      // "2026-03-19"
    @SerialName("end_date") val endDate: String,
    val title: String,
    val organization: String,
    val city: String,
    val discipline: String,
    @SerialName("activity_type") val activityType: String,
    @SerialName("detail_url") val detailUrl: String = "",
    @SerialName("cached_at") val cachedAt: String = ""
)

fun SupabaseTbfActivityDto.toDomain(): TbfActivity = TbfActivity(
    id = id,
    startDate = LocalDate.parse(startDate),
    endDate = LocalDate.parse(endDate),
    title = title,
    organization = organization,
    city = city,
    discipline = TbfDiscipline.fromString(discipline),
    type = TbfActivityType.fromString(activityType),
    detailUrl = detailUrl
)
```

---

### Task 6: SupabaseDataSource — getTbfActivities()

**Files:**
- Modify: `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt`

- [ ] **Step 1:** `SupabaseDataSource` sınıfına ekle:
```kotlin
suspend fun getTbfActivities(
    yearMonth: String? = null,  // "2026-03" formatında
    discipline: String? = null
): Result<List<SupabaseTbfActivityDto>> = runCatching {
    client.postgrest["tbf_activities"].select {
        if (yearMonth != null) {
            filter {
                gte("start_date", "$yearMonth-01")
                lte("start_date", "$yearMonth-31")
            }
        }
        if (discipline != null) {
            filter { eq("discipline", discipline) }
        }
        order("start_date", ascending = true)
    }.decodeList<SupabaseTbfActivityDto>()
}
```

---

### Task 7: Repository Interface + Implementation

**Files:**
- Create: `app/src/main/java/com/horsegallop/domain/equestrian/repository/TbfActivityRepository.kt`
- Create: `app/src/main/java/com/horsegallop/data/equestrian/repository/TbfActivityRepositoryImpl.kt`

- [ ] **Step 1:** Test yaz:
```kotlin
// app/src/test/java/com/horsegallop/data/equestrian/repository/TbfActivityRepositoryImplTest.kt
class TbfActivityRepositoryImplTest {
    private val dataSource: SupabaseDataSource = mock()
    private val repo = TbfActivityRepositoryImpl(dataSource)

    @Test
    fun `getActivitiesForMonth returns mapped domain models on success`() = runTest {
        val dto = SupabaseTbfActivityDto(
            id = "1", startDate = "2026-03-19", endDate = "2026-03-22",
            title = "Test", organization = "TBF", city = "Ankara",
            discipline = "show_jumping", activityType = "incentive"
        )
        whenever(dataSource.getTbfActivities("2026-03", null))
            .thenReturn(Result.success(listOf(dto)))

        val result = repo.getActivitiesForMonth(java.time.YearMonth.of(2026, 3))

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(TbfDiscipline.SHOW_JUMPING, result.getOrNull()?.first()?.discipline)
    }

    @Test
    fun `getActivitiesForMonth returns failure when dataSource fails`() = runTest {
        whenever(dataSource.getTbfActivities(any(), any()))
            .thenReturn(Result.failure(RuntimeException("network")))

        val result = repo.getActivitiesForMonth(java.time.YearMonth.of(2026, 3))

        assertTrue(result.isFailure)
    }
}
```

- [ ] **Step 2:** Testi çalıştır → FAIL beklenir

- [ ] **Step 3:** Interface yaz:
```kotlin
// domain/equestrian/repository/TbfActivityRepository.kt
interface TbfActivityRepository {
    suspend fun getActivitiesForMonth(month: java.time.YearMonth): Result<List<TbfActivity>>
    suspend fun getActivitiesForDay(date: java.time.LocalDate): Result<List<TbfActivity>>
}
```

- [ ] **Step 4:** Implementation yaz:
```kotlin
// data/equestrian/repository/TbfActivityRepositoryImpl.kt
@Singleton
class TbfActivityRepositoryImpl @Inject constructor(
    private val dataSource: SupabaseDataSource
) : TbfActivityRepository {

    override suspend fun getActivitiesForMonth(
        month: java.time.YearMonth
    ): Result<List<TbfActivity>> = runCatching {
        val yearMonth = "${month.year}-${month.monthValue.toString().padStart(2, '0')}"
        dataSource.getTbfActivities(yearMonth = yearMonth)
            .getOrThrow()
            .map { it.toDomain() }
    }

    override suspend fun getActivitiesForDay(
        date: java.time.LocalDate
    ): Result<List<TbfActivity>> = runCatching {
        val yearMonth = "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
        dataSource.getTbfActivities(yearMonth = yearMonth)
            .getOrThrow()
            .map { it.toDomain() }
            .filter { activity ->
                !date.isBefore(activity.startDate) && !date.isAfter(activity.endDate)
            }
    }
}
```

- [ ] **Step 5:** Test çalıştır → PASS
```bash
./gradlew testDebugUnitTest --tests "*.TbfActivityRepositoryImplTest" -q
```

---

### Task 8: UseCase

**Files:**
- Create: `app/src/main/java/com/horsegallop/domain/equestrian/usecase/GetTbfActivitiesUseCase.kt`

- [ ] **Step 1:** Test yaz:
```kotlin
// app/src/test/java/com/horsegallop/domain/equestrian/usecase/GetTbfActivitiesUseCaseTest.kt
class GetTbfActivitiesUseCaseTest {
    private val repo: TbfActivityRepository = mock()
    private val useCase = GetTbfActivitiesUseCase(repo)

    @Test
    fun `invoke delegates to repository with correct month`() = runTest {
        val month = java.time.YearMonth.of(2026, 3)
        whenever(repo.getActivitiesForMonth(month)).thenReturn(Result.success(emptyList()))

        val result = useCase(month)

        verify(repo).getActivitiesForMonth(month)
        assertTrue(result.isSuccess)
    }
}
```

- [ ] **Step 2:** UseCase yaz:
```kotlin
class GetTbfActivitiesUseCase @Inject constructor(
    private val repository: TbfActivityRepository
) {
    suspend operator fun invoke(month: java.time.YearMonth): Result<List<TbfActivity>> =
        repository.getActivitiesForMonth(month)
}
```

- [ ] **Step 3:** DataModule'a binding ekle:
```kotlin
// data/di/DataModule.kt
@Binds @Singleton
abstract fun bindTbfActivityRepository(
    impl: TbfActivityRepositoryImpl
): TbfActivityRepository
```

- [ ] **Step 4:** Test + build:
```bash
./gradlew testDebugUnitTest --tests "*.GetTbfActivitiesUseCaseTest" -q
./gradlew assembleDebug -q
```

- [ ] **Step 5:** Commit
```bash
git add app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDtos.kt \
        app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt \
        app/src/main/java/com/horsegallop/domain/equestrian/repository/TbfActivityRepository.kt \
        app/src/main/java/com/horsegallop/data/equestrian/repository/TbfActivityRepositoryImpl.kt \
        app/src/main/java/com/horsegallop/domain/equestrian/usecase/GetTbfActivitiesUseCase.kt \
        app/src/main/java/com/horsegallop/data/di/DataModule.kt
git commit -m "feat(tbf): data layer + repository + usecase"
```

---

## Chunk 3: ViewModel + Navigation

### Task 9: TbfActivityViewModel

**Files:**
- Create: `app/src/main/java/com/horsegallop/feature/equestrian/presentation/TbfActivityViewModel.kt`

- [ ] **Step 1:** Test yaz:
```kotlin
// app/src/test/java/com/horsegallop/feature/equestrian/presentation/TbfActivityViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class TbfActivityViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val useCase: GetTbfActivitiesUseCase = mock()
    private lateinit var viewModel: TbfActivityViewModel

    @Before fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(useCase(any())).thenReturn(Result.success(emptyList()))
        viewModel = TbfActivityViewModel(useCase)
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state has current month and loading false after load`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(java.time.YearMonth.now(), state.currentMonth)
    }

    @Test
    fun `nextMonth advances currentMonth by one`() = runTest {
        advanceUntilIdle()
        val initial = viewModel.uiState.value.currentMonth
        viewModel.nextMonth()
        advanceUntilIdle()
        assertEquals(initial.plusMonths(1), viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `previousMonth decrements currentMonth by one`() = runTest {
        advanceUntilIdle()
        val initial = viewModel.uiState.value.currentMonth
        viewModel.previousMonth()
        advanceUntilIdle()
        assertEquals(initial.minusMonths(1), viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `selectDay updates selectedDay and filters activities`() = runTest {
        val date = java.time.LocalDate.of(2026, 3, 19)
        val activity = TbfActivity(
            id = "1", startDate = date, endDate = date,
            title = "Test", organization = "TBF", city = "Ankara",
            discipline = TbfDiscipline.SHOW_JUMPING, type = TbfActivityType.INCENTIVE
        )
        whenever(useCase(any())).thenReturn(Result.success(listOf(activity)))
        viewModel = TbfActivityViewModel(useCase)
        advanceUntilIdle()

        viewModel.selectDay(date)

        assertEquals(date, viewModel.uiState.value.selectedDay)
        assertEquals(1, viewModel.uiState.value.activitiesForSelectedDay.size)
    }

    @Test
    fun `toggleDisciplineFilter adds and removes discipline`() = runTest {
        advanceUntilIdle()
        viewModel.toggleDisciplineFilter(TbfDiscipline.SHOW_JUMPING)
        assertTrue(viewModel.uiState.value.disciplineFilters.contains(TbfDiscipline.SHOW_JUMPING))
        viewModel.toggleDisciplineFilter(TbfDiscipline.SHOW_JUMPING)
        assertFalse(viewModel.uiState.value.disciplineFilters.contains(TbfDiscipline.SHOW_JUMPING))
    }
}
```

- [ ] **Step 2:** Test çalıştır → FAIL beklenir

- [ ] **Step 3:** ViewModel yaz:
```kotlin
@Immutable
data class TbfActivityUiState(
    val isLoading: Boolean = true,
    val currentMonth: java.time.YearMonth = java.time.YearMonth.now(),
    val selectedDay: java.time.LocalDate? = null,
    val activitiesForMonth: List<TbfActivity> = emptyList(),
    val activitiesForSelectedDay: List<TbfActivity> = emptyList(),
    val disciplineFilters: Set<TbfDiscipline> = emptySet(),
    val error: String? = null
) {
    val daysWithActivities: Set<java.time.LocalDate> get() =
        activitiesForMonth
            .filter { disciplineFilters.isEmpty() || it.discipline in disciplineFilters }
            .flatMap { activity ->
                generateSequence(activity.startDate) { d ->
                    if (d < activity.endDate) d.plusDays(1) else null
                }.toList()
            }.toSet()

    val filteredActivitiesForSelectedDay: List<TbfActivity> get() =
        if (disciplineFilters.isEmpty()) activitiesForSelectedDay
        else activitiesForSelectedDay.filter { it.discipline in disciplineFilters }
}

@HiltViewModel
class TbfActivityViewModel @Inject constructor(
    private val getActivities: GetTbfActivitiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TbfActivityUiState())
    val uiState: StateFlow<TbfActivityUiState> = _uiState.asStateFlow()

    init { loadMonth(_uiState.value.currentMonth) }

    fun nextMonth() {
        val next = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { it.copy(currentMonth = next, selectedDay = null) }
        loadMonth(next)
    }

    fun previousMonth() {
        val prev = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { it.copy(currentMonth = prev, selectedDay = null) }
        loadMonth(prev)
    }

    fun selectDay(date: java.time.LocalDate) {
        val activities = _uiState.value.activitiesForMonth.filter { activity ->
            !date.isBefore(activity.startDate) && !date.isAfter(activity.endDate)
        }
        _uiState.update { it.copy(selectedDay = date, activitiesForSelectedDay = activities) }
    }

    fun toggleDisciplineFilter(discipline: TbfDiscipline) {
        _uiState.update { state ->
            val filters = state.disciplineFilters.toMutableSet()
            if (discipline in filters) filters.remove(discipline) else filters.add(discipline)
            state.copy(disciplineFilters = filters)
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }

    private fun loadMonth(month: java.time.YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getActivities(month)
                .onSuccess { activities ->
                    _uiState.update { it.copy(isLoading = false, activitiesForMonth = activities) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
```

- [ ] **Step 4:** Test çalıştır → PASS
```bash
./gradlew testDebugUnitTest --tests "*.TbfActivityViewModelTest" -q
```

---

### Task 10: Navigation

**Files:**
- Modify: `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

- [ ] **Step 1:** `AppNav.kt` içinde `Dest` sealed class'a ekle:
```kotlin
data object TbfActivityCalendar : Dest("tbf_activity_calendar")
```

- [ ] **Step 2:** NavHost içine route ekle:
```kotlin
composable(Dest.TbfActivityCalendar.route) {
    TbfActivityScreen(onNavigateBack = { navController.popBackStack() })
}
```

- [ ] **Step 3:** `EquestrianAgendaScreen.kt` içinde COMPETITIONS sekmesine "Takvim Görünümü" butonu ekle:
```kotlin
// COMPETITIONS sekmesi içinde listenin üstüne:
OutlinedButton(
    onClick = { onNavigateToCalendar() },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
) {
    Icon(Icons.Default.CalendarMonth, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text(stringResource(R.string.tbf_view_calendar))
}
```

`EquestrianAgendaScreen` parametresine `onNavigateToCalendar: () -> Unit` ekle. AppNav'da bağla.

- [ ] **Step 4:** Build kontrol:
```bash
./gradlew assembleDebug -q
```

- [ ] **Step 5:** Commit
```bash
git add app/src/main/java/com/horsegallop/feature/equestrian/presentation/TbfActivityViewModel.kt \
        app/src/main/java/com/horsegallop/navigation/AppNav.kt \
        app/src/main/java/com/horsegallop/feature/equestrian/presentation/EquestrianAgendaScreen.kt \
        app/src/test/java/com/horsegallop/feature/equestrian/presentation/TbfActivityViewModelTest.kt
git commit -m "feat(tbf): ViewModel + navigation routing"
```

---

## Chunk 4: UI — CalendarGrid + TbfActivityScreen

### Task 11: CalendarGrid Bileşeni

**Files:**
- Create: `app/src/main/java/com/horsegallop/core/components/CalendarGrid.kt`

- [ ] **Step 1:** Bileşeni yaz (SemanticColors kullan, direkt renk yasak):
```kotlin
// core/components/CalendarGrid.kt
@Composable
fun CalendarGrid(
    yearMonth: java.time.YearMonth,
    selectedDay: java.time.LocalDate?,
    daysWithActivities: Map<java.time.LocalDate, List<TbfDiscipline>>,  // gün → disiplin renkleri
    onDayClick: (java.time.LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Pazartesi = 0 başlangıç
    val firstDayOfWeek = (firstDay.dayOfWeek.value - 1)  // Mon=0..Sun=6

    Column(modifier = modifier) {
        // Gün başlıkları
        Row(Modifier.fillMaxWidth()) {
            listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.cardStroke
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Takvim grid
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOfWeek + 1
                    val isValid = dayNumber in 1..daysInMonth
                    val date = if (isValid) yearMonth.atDay(dayNumber) else null
                    val isSelected = date == selectedDay
                    val disciplines = if (date != null) daysWithActivities[date] ?: emptyList() else emptyList()

                    CalendarDayCell(
                        dayNumber = if (isValid) dayNumber else null,
                        isSelected = isSelected,
                        isToday = date == java.time.LocalDate.now(),
                        activityDisciplines = disciplines,
                        onClick = { date?.let(onDayClick) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int?,
    isSelected: Boolean,
    isToday: Boolean,
    activityDisciplines: List<TbfDiscipline>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(enabled = dayNumber != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (dayNumber != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dayNumber.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                )
                // Etkinlik noktaları (max 3)
                if (activityDisciplines.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        activityDisciplines.take(3).forEach { discipline ->
                            Box(
                                Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(disciplineColor(discipline, semantic))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun disciplineColor(discipline: TbfDiscipline, semantic: SemanticColors): Color =
    when (discipline) {
        TbfDiscipline.SHOW_JUMPING -> MaterialTheme.colorScheme.primary
        TbfDiscipline.ENDURANCE -> semantic.success
        TbfDiscipline.DRESSAGE -> semantic.info
        TbfDiscipline.PONY -> semantic.gaitWalk
        TbfDiscipline.VAULTING -> semantic.warning
        TbfDiscipline.EVENTING -> semantic.ratingStar
        TbfDiscipline.OTHER -> semantic.cardStroke
    }

@Preview(showBackground = true)
@Composable
private fun CalendarGridPreview() {
    HorseGallopTheme {
        CalendarGrid(
            yearMonth = java.time.YearMonth.of(2026, 3),
            selectedDay = java.time.LocalDate.of(2026, 3, 19),
            daysWithActivities = mapOf(
                java.time.LocalDate.of(2026, 3, 19) to listOf(TbfDiscipline.SHOW_JUMPING),
                java.time.LocalDate.of(2026, 3, 21) to listOf(TbfDiscipline.SHOW_JUMPING),
                java.time.LocalDate.of(2026, 3, 27) to listOf(TbfDiscipline.ENDURANCE)
            ),
            onDayClick = {}
        )
    }
}
```

---

### Task 12: TbfActivityScreen

**Files:**
- Create: `app/src/main/java/com/horsegallop/feature/equestrian/presentation/TbfActivityScreen.kt`

- [ ] **Step 1:** Strings ekle (`app/src/main/res/values/strings.xml` + `values-tr/strings.xml`):
```xml
<!-- values/strings.xml -->
<string name="tbf_activity_calendar">Activity Calendar</string>
<string name="tbf_view_calendar">Calendar View</string>
<string name="tbf_no_events_day">No events on this day</string>
<string name="tbf_filter_all">All</string>
<string name="tbf_add_to_calendar">Add to Calendar</string>

<!-- values-tr/strings.xml -->
<string name="tbf_activity_calendar">Faaliyet Takvimi</string>
<string name="tbf_view_calendar">Takvim Görünümü</string>
<string name="tbf_no_events_day">Bu gün etkinlik yok</string>
<string name="tbf_filter_all">Tümü</string>
<string name="tbf_add_to_calendar">Takvime Ekle</string>
```

- [ ] **Step 2:** Ekranı yaz:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TbfActivityScreen(
    onNavigateBack: () -> Unit,
    viewModel: TbfActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val semantic = LocalSemanticColors.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.tbf_activity_calendar)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        containerColor = semantic.screenBase
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ay navigasyonu
            item(key = "month_nav", contentType = "month_nav") {
                MonthNavigationRow(
                    yearMonth = state.currentMonth,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth
                )
            }

            // Disiplin filtreleri
            item(key = "filters", contentType = "filters") {
                DisciplineFilterRow(
                    activeFilters = state.disciplineFilters,
                    onToggle = viewModel::toggleDisciplineFilter
                )
            }

            // Takvim grid
            item(key = "calendar", contentType = "calendar") {
                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth().height(240.dp),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val daysMap = state.daysWithActivities
                        .groupBy { it }
                        .mapValues { (date, _) ->
                            state.activitiesForMonth
                                .filter { a -> !date.isBefore(a.startDate) && !date.isAfter(a.endDate) }
                                .map { it.discipline }
                        }
                    CalendarGrid(
                        yearMonth = state.currentMonth,
                        selectedDay = state.selectedDay,
                        daysWithActivities = daysMap,
                        onDayClick = viewModel::selectDay
                    )
                }
            }

            // Seçili gün başlığı + etkinlik listesi
            if (state.selectedDay != null) {
                val dayActivities = state.filteredActivitiesForSelectedDay
                item(key = "day_header", contentType = "header") {
                    Text(
                        text = state.selectedDay?.let {
                            "${it.dayOfMonth} ${it.month.turkishName()} — ${dayActivities.size} etkinlik"
                        } ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = semantic.cardStroke
                    )
                }

                if (dayActivities.isEmpty()) {
                    item(key = "empty", contentType = "empty") {
                        Text(
                            text = stringResource(R.string.tbf_no_events_day),
                            style = MaterialTheme.typography.bodyMedium,
                            color = semantic.cardStroke,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(
                        items = dayActivities,
                        key = { it.id },
                        contentType = { "activity_card" }
                    ) { activity ->
                        TbfActivityCard(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigationRow(
    yearMonth: java.time.YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious,
            Modifier.semantics { contentDescription = "Önceki ay" }) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
        Text(
            text = "${yearMonth.month.turkishName()} ${yearMonth.year}",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext,
            Modifier.semantics { contentDescription = "Sonraki ay" }) {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun DisciplineFilterRow(
    activeFilters: Set<TbfDiscipline>,
    onToggle: (TbfDiscipline) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(TbfDiscipline.entries.filter { it != TbfDiscipline.OTHER },
            key = { it.name }) { discipline ->
            FilterChip(
                selected = discipline in activeFilters,
                onClick = { onToggle(discipline) },
                label = { Text(discipline.displayNameTr, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
private fun TbfActivityCard(
    activity: TbfActivity,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated)
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Sol renk bandı — disipline göre
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(disciplineColorComposable(activity.discipline))
            )
            Column(Modifier.padding(12.dp).weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(activity.discipline.displayNameTr,
                            style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(activity.type.displayNameTr,
                            style = MaterialTheme.typography.labelSmall) }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(activity.title, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${activity.organization} • ${activity.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.cardStroke
                )
                Text(
                    text = activity.dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.cardStroke
                )
            }
        }
    }
}

@Composable
private fun disciplineColorComposable(discipline: TbfDiscipline): Color {
    val semantic = LocalSemanticColors.current
    return when (discipline) {
        TbfDiscipline.SHOW_JUMPING -> MaterialTheme.colorScheme.primary
        TbfDiscipline.ENDURANCE -> semantic.success
        TbfDiscipline.DRESSAGE -> semantic.info
        TbfDiscipline.PONY -> semantic.gaitWalk
        TbfDiscipline.VAULTING -> semantic.warning
        TbfDiscipline.EVENTING -> semantic.ratingStar
        TbfDiscipline.OTHER -> semantic.cardStroke
    }
}

@Preview(showBackground = true)
@Composable
fun TbfActivityScreenPreview() {
    HorseGallopTheme {
        // Fake state ile preview — ViewModel bağımlılığı yok
        val fakeActivity = TbfActivity(
            id = "1",
            startDate = java.time.LocalDate.of(2026, 3, 19),
            endDate = java.time.LocalDate.of(2026, 3, 22),
            title = "ANTALYA ATLI SPOR KULÜBÜ ENGEL ATLAMA YARIŞMALARI",
            organization = "ABAK",
            city = "ANTALYA",
            discipline = TbfDiscipline.SHOW_JUMPING,
            type = TbfActivityType.INCENTIVE
        )
        TbfActivityCard(activity = fakeActivity)
    }
}
```

- [ ] **Step 3:** Build + test:
```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug 2>&1 | grep -c "Error"  # 0 olmalı
```

- [ ] **Step 4:** Commit
```bash
git add app/src/main/java/com/horsegallop/core/components/CalendarGrid.kt \
        app/src/main/java/com/horsegallop/feature/equestrian/presentation/TbfActivityScreen.kt \
        app/src/main/res/values/strings.xml \
        app/src/main/res/values-tr/strings.xml
git commit -m "feat(tbf): CalendarGrid bileşeni + TbfActivityScreen"
```

---

## Chunk 5: QA + Review + Deploy

### Task 13: qa-verifier

- [ ] `qa-verifier` ajanını çalıştır — PASS zorunlu:
  - `./gradlew assembleDebug` → BUILD SUCCESSFUL
  - `./gradlew testDebugUnitTest` → tüm testler geçmeli
  - `./gradlew lintDebug` → Error: 0
  - SemanticColors ihlali: 0
  - `@Preview` TbfActivityScreen + CalendarGrid'de mevcut

### Task 14: Automated Code Review

- [ ] `coderabbit:review` VEYA `code-review:code-review` skill'ini çalıştır
- [ ] Issues varsa → düzelt → review tekrar
- [ ] PASS → commit-push-pr

### Task 15: PR + Deploy

- [ ] PR oluştur:
```bash
bash scripts/pr-pipeline-merge.sh
```

- [ ] Emülatöre kur:
```bash
bash scripts/deploy-emulator.sh
```

- [ ] Smoke test:
```bash
bash scripts/smoke-test.sh
```

- [ ] Retrospektif:
```bash
bash scripts/retrospective.sh <pr-number>
```

---

## Kapsam Dışı (Sonraki İterasyon)

- `binicilik.org.tr` scraping Edge Function (cron tabanlı gerçek veri)
- Google Calendar'a ekleme butonu (`CalendarContract.Events` intent)
- Push notification: "Yaklaşan etkinlik" hatırlatıcısı
- Şehir filtresi + harita görünümü (Seçenek B)
- `tbf_activities` tablosuna `TODO: scrape` notu bırak

---

*Plan: 2026-03-19 | TbfActivityCalendar feature*
