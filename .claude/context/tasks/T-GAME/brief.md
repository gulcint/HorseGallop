# T-GAME — Social Gamification MVP: Monthly Distance League

## Goal
Tek challenge tipiyle (MONTHLY_DISTANCE) aylık mesafe ligi MVP'si yap. Kullanıcılar kendi progress'ini görsün; basit leaderboard ile ligdeki diğer kullanıcıları görsün.

## Mevcut Durum
- `Challenge` domain modeli var: `ChallengeType.MONTHLY_DISTANCE`, progress, daysLeft
- `ChallengeScreen` + `ChallengeViewModel` var
- Challenge verisi Supabase'den çekiliyor
- `Badge` domain modeli var, rozet görünümü mevcut
- Leaderboard yok

## Kapsam Kararları (Brainstorm)

### Anti-cheat Kuralları
- Sadece `ride_sessions` tablosundaki verilerden hesaplama (manual entry yok)
- Ay başında sıfırlama: Supabase scheduled function (pg_cron)
- Minimum ride süresi: 5 dakika (kısa testleri filtrele)

### Leaderboard Kapsamı
- Küresel değil, "arkadaş ligi" veya "aynı country" ile başla — MVP için country bazlı
- Gizlilik: Kullanıcı leaderboard'dan opt-out edebilmeli

### Supabase
- `leaderboard_monthly` view veya materialized view: user_id, display_name, total_distance, rank
- Her ride_session kayıt edildiğinde trigger ile monthly_distance güncellenir

## Kapsam (MVP — Tek Sprint)
- Supabase: `monthly_league` view (ride_sessions aggregate) + RLS
- `SupabaseDataSource.getMonthlyLeaderboard()` metodu
- `LeaderboardScreen.kt` + `LeaderboardViewModel.kt`
- `ChallengeScreen`'e "Ligde Sıran" widget ekle
- Navigation: ChallengeScreen'den LeaderboardScreen'e route

## Kapsam Dışı
- Gerçek zamanlı leaderboard (polling yeterli, 5dk refresh)
- Farklı challenge tipleri (sprint 2)
- Rozet push notification

## Constraints
- SemanticColors zorunlu
- @Preview zorunlu
- strings: TR + EN
- LeaderboardScreen kendi route'una ihtiyaç duyar (AppNav.kt güncellenmeli)

## Relevant Paths
- `app/src/main/java/com/horsegallop/domain/challenge/model/`
- `app/src/main/java/com/horsegallop/feature/challenge/presentation/`
- `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt`
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`
- `supabase/` (migrations veya functions)
- `app/src/main/res/values/strings_core.xml`

## Agent Sırası
1. `researcher` → ChallengeScreen mevcut UI, ChallengeViewModel, AppNav route pattern
2. `operator` → Supabase monthly_league view + RLS migration
3. `android-feature` → LeaderboardViewModel + LeaderboardScreen + AppNav route
4. `qa-verifier` → PASS/FAIL

## Open Questions
- Leaderboard filtresi: global mı, ülke bazlı mı, sadece takipçiler mi? → MVP: global (ilk 50 kullanıcı)
- Opt-out: `user_profiles.leaderboard_visible` boolean mı? → Evet, varsayılan true
- Puan sistemi: sadece mesafe mi? → Evet, km cinsinden

