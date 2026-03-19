# TBF Faaliyet Takvimi — Analiz Dökümanı

**Tarih:** 2026-03-18
**Hazırlayan:** Researcher Agent
**Durum:** Taslak — Builder'a aktarılmayı bekliyor

---

## 1. Problem Özeti

`binicilik.org.tr/Anasayfa/Faaliyet` sayfasındaki TBF (Türkiye Binicilik Federasyonu) faaliyet listesi, mevcut `EquestrianAgendaScreen` içindeki COMPETITIONS sekmesine benzer bir tablo formatında sunulmaktadır. Bu format aşağıdaki nedenlerle yetersizdir:

- **Statik tablo görünümü** — tüm etkinlikler düz liste olarak akar, tarama zordur
- **Zayıf filtreleme** — 6 disiplin + 9 tür filtresi birbirine bağımsız, çapraz filtreleme yok
- **Coğrafi bağlantı yok** — şehir bilgisi metin olarak var ama harita veya il seçimi yok
- **Tarih görselliği yok** — "19-22 Mart 2026" formatı, ay veya hafta bazlı gezinmeyi desteklemiyor
- **Mobil uyumsuz** — çok sütunlu tablo dar ekranda okunamaz hale geliyor
- **Renk kodlaması yok** — farklı disiplinler (Engel Atlama, At Terbiyesi, Atlı Dayanıklılık vb.) görsel olarak ayrışmıyor

### Mevcut Veri Yapısı (binicilik.org.tr)

| Alan | Örnek |
|------|-------|
| Tarih aralığı | 19-22 Mart 2026 |
| Faaliyet adı | ANTALYA ATLI SPOR KULÜBÜ ENGEL ATLAMA YARIŞMALARI |
| Organizasyon | ABAK |
| Yer / Şehir | ANTALYA |
| Disiplin | ENGEL ATLAMA |
| Tür | TEŞVİK |

**Disiplin seçenekleri:** ENGEL ATLAMA, ATLI DAYANIKLILIK, AT TERBİYESİ, PONY, ATLI CİMNASTİK, ÜÇ GÜNLÜK YARIŞMA

**Tür seçenekleri:** ULUSLARARASI YARIŞMA, ŞAMPİYONA, KUPA, TEŞVİK, EĞİTİM, KATEGORİ BELGESİ SINAVI, SEMİNER, KONFERANS, ÇALIŞTAY

---

## 2. Mevcut Repo Gerçeği

### Mevcut Ekranlar ve Bileşenler

- `feature/equestrian/presentation/EquestrianAgendaScreen.kt` — Ana ekran; ANNOUNCEMENTS, COMPETITIONS, TBF sekmeleri var
- `feature/tbf/presentation/TbfViewModel.kt` + `TbfEventDetailScreen.kt` — At yarışı programı (TJK→TBF), tamamen ayrı bir domain
- `domain/equestrian/model/EquestrianModels.kt` — `EquestrianCompetition(id, title, location, dateLabel, detailUrl)` modeli var

### Dikkat: İki Farklı TBF Bağlamı

Repoda iki farklı "TBF" kavramı karıştırılmamalıdır:

1. **`feature/tbf/`** — At yarışı programı (koşular, jokeyler, bahis oranları). `TbfViewModel`, `TbfEventDay`, `TbfVenue`, `TbfEventCard` modelleri. Kaynak: TBF at yarış programı API.

2. **`feature/equestrian/` → COMPETITIONS sekmesi** — Binicilik federasyonu etkinlikleri (Engel Atlama yarışmaları, seminerler, şampiyonalar vb.). `EquestrianCompetition` modeli. Kaynak: `equestrian_competitions` tablosu (Supabase).

**TBF Faaliyet Takvimi bu ikinci bağlamı genişletir** — `EquestrianAgendaScreen` içindeki COMPETITIONS sekmesini iyileştirme veya ayrı bir ekran olarak konumlandırma kararı alınmalıdır.

### Mevcut Supabase Tabloları

```sql
-- Tablo 22 (initial_schema.sql, satır 291)
equestrian_announcements(id, title, summary, published_at_label, detail_url, image_url, cached_at)

-- Tablo 23 (initial_schema.sql, satır 302)
equestrian_competitions(id, title, location, date_label, detail_url, cached_at)
```

`equestrian_competitions` tablosu şu an `title`, `location`, `date_label` alanlarıyla oldukça zayıf. TBF Faaliyet için `discipline`, `activity_type`, `start_date`, `end_date`, `city`, `organization` alanları eksik.

### Navigation

`AppNav.kt` içinde `EquestrianAgendaScreen` zaten rotaya bağlı. Yeni bir `TbfActivityScreen` eklenmesi durumunda ayrı bir `Dest` tanımı gerekir.

### Google Maps

`libs.versions.toml` içinde `maps-compose` zaten mevcut. Harita tabanlı seçenek için ek bağımlılık gerekmez.

---

## 3. Dış Kaynaklar ve Kısıtlamalar

### Veri Kaynağı Stratejisi

| Seçenek | Avantaj | Dezavantaj |
|---------|---------|------------|
| Web scraping (binicilik.org.tr) | Güncel veri | Site yapısı değişebilir, hukuki belirsizlik, rate limiting |
| Supabase Edge Function + cron | Kontrollü, önbelleğe alınmış | Periyodik gecikme, bakım maliyeti |
| Manuel admin girişi | Tam kontrol | Ölçeklenmez, operasyonel yük |

**Önerilen:** Supabase scheduled cron job (her gece 02:00 TSI) → `binicilik.org.tr` scrape → `tbf_activities` tablosuna yaz. Mevcut `federation_sync_status` mekanizması model alınabilir.

### Takvim Bileşeni

Android'de resmi Material3 takvim bileşeni `DatePicker` var ancak aylık grid görünümü için özel Compose bileşeni yazmak gerekecektir. Alternatif: `kizitonwose/calendar-compose` kütüphanesi (MIT lisanslı, min SDK 21 uyumlu).

---

## 4. UX Önerileri — 3 Seçenek

### Seçenek A: Takvim + Kart Grid (Önerilen)

**Neden önerilen:** Mobil dostu, tarihsel gezinme doğal, veri yoğunluğunu iyi yönetir.

```
┌─────────────────────────────────────┐
│  < Mart 2026 >                      │  ← Ay navigasyonu (IconButton)
├─────────────────────────────────────┤
│ [ENGEL] [DAY.] [TER.] [TÜM]        │  ← Disiplin FilterChip (LazyRow)
│ [TEŞVİK] [ŞAMPİYONA] [TÜM]        │  ← Tür FilterChip (LazyRow)
├─────────────────────────────────────┤
│ Pt  Sl  Çr  Pr  Cu  Ct  Pz         │
│  2   3   4   5   6   7   8          │
│  ●   9  10  11  12  ●   ●           │  ← ● = etkinlik var (renk = disiplin)
│ 16  17  ●   ●  20  21  22           │
│ 23  24  25  26  ●  28  29           │
├─────────────────────────────────────┤
│ 19 Mart — 2 etkinlik                │  ← Seçili gün
│ ┌─────────────────────────────────┐ │
│ │ ■ ENGEL ATLAMA  •  TEŞVİK      │ │  ← renk bandı
│ │ Antalya Atlı Spor Kulübü        │ │
│ │ Antalya  •  19-22 Mart 2026    │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ ■ AT TERBİYESİ  •  SEMİNER     │ │
│ │ TBF Eğitim Merkezi              │ │
│ │ İstanbul  •  19 Mart 2026      │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

- `LazyColumn` + sticky header: takvim grid + filtreler üstte sabitlenir
- Etkinlik olan günler: disiplin renginde `●` nokta
- Seçilen gün: etkinlik kartları aşağıda listelenir
- Google Calendar'a ekle: `Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)`

**Renk Şeması (SemanticColors uyumlu):**

| Disiplin | Renk Token |
|----------|-----------|
| ENGEL ATLAMA | `MaterialTheme.colorScheme.primary` (Saddle Brown) |
| ATLI DAYANIKLILIK | `semantic.gaitTrot` (yeşil) |
| AT TERBİYESİ | `semantic.info` (mavi-gri) |
| PONY | `semantic.gaitWalk` (açık mavi) |
| ATLI CİMNASTİK | `semantic.warning` (turuncu-kahve) |
| ÜÇ GÜNLÜK YARIŞMA | `semantic.ratingStar` (amber) |

### Seçenek B: Harita + Liste (Masaüstü / Tablet Odaklı)

- Türkiye haritası (Google Maps Compose — zaten `libs.versions.toml`'da var)
- Her şehirde `MarkerInfoWindow` — tıklayınca şehrin etkinlikleri
- Sağda `ModalBottomSheet`: etkinlik listesi
- Mobilde harita `collapsible` veya varsayılan liste görünümü
- **Risk:** Harita performansı düşük cihazlarda sorunlu olabilir; marker kümesi yönetimi (`Clustering`) ek iş yükü

### Seçenek C: Swipeable Timeline (En Hızlı Geliştirme)

- Yatay kaydırmalı ay seçici (`HorizontalPager` veya `LazyRow`)
- Dikey timeline: `LazyColumn` + `stickyHeader` (ay ve hafta başlıkları)
- Her etkinlik satırı → disiplin renk bandı + kart
- Swipe-to-detail: tıklayınca `ModalBottomSheet`
- **Avantaj:** Mevcut `EquestrianAgendaScreen` altyapısına en kolay entegre
- **Dezavantaj:** Takvim grid olmadığından "bu ay ne var?" sorusunu görsel yanıtlamıyor

### Alternatif: Hızlı Liste İyileştirmesi (1 Sprint, Düşük Risk)

Takvim implementasyonu zaman alıyorsa, mevcut listeyi iyileştirme yaklaşımı:

- `LazyColumn` + `stickyHeader` ile ay başlıkları
- Her satır → `Card` ile sol kenar disiplin renk bandı (4dp)
- Filtreler → `ModalBottomSheet` (Disiplin + Tür multi-select)
- Arama: şehir veya etkinlik adı (`SearchBar` veya `TextField`)
- Skeleton loading (`ShimmerEffect`)
- Bu yaklaşım 1 sprint içinde tamamlanabilir

---

## 5. Önerilen Domain Modeli

Mevcut `EquestrianCompetition` modeli TBF Faaliyet için yetersiz. Yeni model:

```kotlin
// domain/equestrian/model/TbfActivity.kt
data class TbfActivity(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val title: String,
    val organization: String,
    val city: String,
    val discipline: TbfDiscipline,
    val type: TbfActivityType
)

enum class TbfDiscipline {
    SHOW_JUMPING,    // ENGEL ATLAMA
    ENDURANCE,       // ATLI DAYANIKLILIK
    DRESSAGE,        // AT TERBİYESİ
    PONY,            // PONY
    VAULTING,        // ATLI CİMNASTİK
    EVENTING         // ÜÇ GÜNLÜK YARIŞMA
}

enum class TbfActivityType {
    INTERNATIONAL,    // ULUSLARARASI YARIŞMA
    CHAMPIONSHIP,     // ŞAMPİYONA
    CUP,              // KUPA
    INCENTIVE,        // TEŞVİK
    EDUCATION,        // EĞİTİM
    CATEGORY_EXAM,    // KATEGORİ BELGESİ SINAVI
    SEMINAR,          // SEMİNER
    CONFERENCE,       // KONFERANS
    WORKSHOP          // ÇALIŞTAY
}
```

### Yeni Supabase Tablosu

Mevcut `equestrian_competitions` tablosu yerine (veya ek olarak):

```sql
CREATE TABLE tbf_activities (
    id TEXT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    title TEXT NOT NULL DEFAULT '',
    organization TEXT DEFAULT '',
    city TEXT DEFAULT '',
    discipline TEXT NOT NULL,  -- enum: show_jumping | endurance | dressage | pony | vaulting | eventing
    activity_type TEXT NOT NULL, -- enum: international | championship | cup | incentive | education | ...
    detail_url TEXT DEFAULT '',
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tbf_activities_start_date ON tbf_activities(start_date);
CREATE INDEX idx_tbf_activities_discipline ON tbf_activities(discipline);
```

---

## 6. Mimari Plan

### Katmanlar (mevcut mimariyle uyumlu)

```
domain/equestrian/model/TbfActivity.kt          ← yeni domain model
domain/equestrian/repository/TbfActivityRepository.kt  ← interface
domain/equestrian/usecase/GetTbfActivitiesUseCase.kt    ← filtreli sorgu

data/equestrian/repository/TbfActivityRepositoryImpl.kt  ← Supabase PostgREST
data/remote/supabase/SupabaseDataSource.kt               ← tbf_activities tablo sorgusu

feature/equestrian/presentation/TbfActivityScreen.kt     ← Seçenek A: yeni ekran
feature/equestrian/presentation/TbfActivityViewModel.kt  ← UiState + filtreler

navigation/AppNav.kt  ← Dest.TbfActivity eklenmeli
```

### Veri Akışı

```
binicilik.org.tr (scraping)
    ↓ (Supabase Edge Function, cron tabanlı)
tbf_activities tablosu (Supabase PostgreSQL)
    ↓ (PostgREST, filtreli SELECT)
TbfActivityRepositoryImpl
    ↓ (Use Case)
TbfActivityViewModel (UiState: ay, seçili gün, filtreler, liste)
    ↓ (StateFlow)
TbfActivityScreen (Composable: takvim grid + kart listesi)
```

### ViewModel UiState Taslağı

```kotlin
data class TbfActivityUiState(
    val isLoading: Boolean = true,
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDay: LocalDate? = null,
    val disciplineFilter: Set<TbfDiscipline> = emptySet(),  // boş = tümü
    val typeFilter: Set<TbfActivityType> = emptySet(),        // boş = tümü
    val activitiesForMonth: List<TbfActivity> = emptyList(),  // tüm ay verisi
    val activitiesForDay: List<TbfActivity> = emptyList(),    // seçili gün filtreli
    val error: String? = null
)
```

---

## 7. Implementation Plan

### Sprint X.1 — Temel Ekran (1 Sprint)

1. `tbf_activities` tablosu Supabase migration olarak eklenir
2. Domain modeli (`TbfActivity`, `TbfDiscipline`, `TbfActivityType`) yazılır
3. `TbfActivityRepository` interface + `TbfActivityRepositoryImpl` (PostgREST)
4. `GetTbfActivitiesUseCase` (ay + disiplin + tür filtreleriyle)
5. `TbfActivityViewModel` + `TbfActivityUiState`
6. `TbfActivityScreen` — Seçenek C (Timeline) ile başlanır; Seçenek A'ya sonraki sprintte geçilir
7. Navigation: `Dest.TbfActivity` + `EquestrianAgendaScreen` COMPETITIONS sekmesine link

### Sprint X.2 — Takvim Grid + Detay (1 Sprint)

1. Özel Compose takvim grid bileşeni (`core/components/CalendarGrid.kt`)
   - Alternatif: `kizitonwose/calendar-compose` bağımlılığı değerlendirilir
2. Disiplin renk noktaları takvim üzerinde gösterilir
3. Etkinlik detay `ModalBottomSheet`
4. Google Calendar'a ekleme (`Intent`)
5. Skeleton loading + animasyon
6. Supabase Edge Function: `scrape-tbf-activities` (cron tabanlı)

### Kapsam Dışı (Şimdilik)

- Harita tabanlı seçim (Seçenek B) — ayrı görev olarak planlanabilir
- Push notification: "Yaklaşan etkinlik hatırlatıcısı"
- Favorilere ekleme / kaydetme

---

## 8. Riskler ve Bilinmeyenler

| Risk | Olasılık | Etki | Önlem |
|------|----------|------|-------|
| binicilik.org.tr site yapısının değişmesi | Orta | Yüksek | Scraper testleri + hata izleme |
| `equestrian_competitions` tablosuyla çakışma | Düşük | Orta | Ayrı `tbf_activities` tablosu kullanılırsa sorun yok |
| Takvim grid bileşeni geliştirme süresi | Yüksek | Orta | Sprint X.1'de Timeline (Seçenek C) ile başla, X.2'de grid'e geç |
| Min SDK 24 + `LocalDate` | Düşük | Düşük | `LocalDate` API 26+; `ThreeTenABP` veya `desugaring` gerekebilir — `build.gradle.kts` kontrol edilmeli |
| TBF veri lisanslama | Belirsiz | Yüksek | Scraping öncesi hukuki değerlendirme yapılmalı |

### Bilinmeyenler

- `build.gradle.kts` içinde `coreLibraryDesugaring` aktif mi? (`LocalDate` için gerekli)
- `equestrian_competitions` tablosu Sprint 8'e kadar TBF aktivitelerini de mi barındırıyor, yoksa ayrı scraping sistemi mi var?
- `EquestrianAgendaViewModel` içindeki COMPETITIONS sekmesi şu an hangi veriyi çekiyor? (binicilik.org.tr mu, yoksa elle girilen mi?)

---

## 9. Builder için Net Girdiler

Builder bu dökümanı alırken şu kararlar kesinleşmiş olmalıdır:

1. **UX Seçimi:** Seçenek A (Takvim), B (Harita) veya C (Timeline) — ya da Hızlı Liste İyileştirmesi
2. **Tablo stratejisi:** `equestrian_competitions` genişletilecek mi, yoksa `tbf_activities` ayrı tablo olarak mı oluşturulacak?
3. **Konum:** Yeni ekran (`Dest.TbfActivity`) mü, yoksa `EquestrianAgendaScreen` COMPETITIONS sekmesinin iyileştirilmesi mi?
4. **Cron / scraping:** Sprint X.1'de mock veri mi kullanılacak, yoksa scraping de bu sprintte mi yapılacak?
5. **`coreLibraryDesugaring`:** `build.gradle.kts` kontrol edilmeli — `LocalDate` kullanımı için gerekli

