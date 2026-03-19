# T-B2B — B2B Barn Management: Role Model + Owner Dashboard

## Goal
Ahır sahibi rolü için rol modeli tasarla ve mevcut B2C navigasyondan ayrılmış bir Barn Owner akışı kur. BarnDashboard ve CreateLesson gibi ekranlar zaten var; bunları role-aware hale getir.

## Mevcut Durum
- `BarnDashboardScreen`, `BarnDashboardViewModel`, `CreateLessonScreen`, `LessonRosterScreen` mevcut
- `UserRole` domain modeli var
- Barn owner ile normal kullanıcı arasındaki navigasyon ayrımı belirsiz
- `BarnManagementRepositoryImpl` var

## Kapsam Kararları (Brainstorm)

### Rol Modeli
- `UserRole`: RIDER, BARN_OWNER (belki TRAINER eklenebilir — sprint 2)
- Supabase'de `user_profiles.role` kolonu var mı? → Araştırılacak
- Barn owner başka bir kullanıcı tarafından atanır (admin) veya self-onboard mu? → MVP: self-onboard akışı

### Navigasyon Ayrımı
- Barn owner: alt navigasyonda "Ahırım" tab'ı görünür
- Normal kullanıcı: Barns (keşif) görünür
- AppNav.kt'de role-conditional routing

### Dashboard Gereksinimleri
- Aktif dersler + rezervasyonlar
- At listesi (ahırdaki atlar)
- Gelir özeti (MVP dışı, ileride)

## Kapsam (MVP)
- `UserRole` enum kontrolü: AppNav.kt'de rol-aware bottom nav
- Self-onboard: `BarnOnboardingScreen` — ahır adı, konum gir; `user_profiles.role = BARN_OWNER` set et
- `BarnDashboard` zaten var, routing'i tamamla
- Supabase `user_profiles.role` kolonu yoksa migration ekle

## Kapsam Dışı
- Gelir/finansal dashboard
- Multi-barn (bir kullanıcı birden fazla ahır)
- Trainer rol ayrımı

## Constraints
- SemanticColors zorunlu
- @Preview zorunlu
- strings: TR + EN
- Katman sırası: domain → data → di → feature → navigation

## Relevant Paths
- `app/src/main/java/com/horsegallop/feature/barnmanagement/presentation/`
- `app/src/main/java/com/horsegallop/domain/auth/model/` (UserRole)
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`
- `app/src/main/java/com/horsegallop/data/barnmanagement/repository/BarnManagementRepositoryImpl.kt`
- `supabase/` (migrations)
- `app/src/main/res/values/strings_core.xml`

## Agent Sırası
1. `researcher` → UserRole model, AppNav bottom nav logic, BarnManagementRepository metotları, Supabase schema
2. `android-feature` → BarnOnboardingScreen + role-aware AppNav
3. `operator` → Supabase migration (role kolonu varsa skip)
4. `qa-verifier` → PASS/FAIL

## Open Questions
- `user_profiles` tablosunda `role` kolonu var mı? → Researcher bulacak
- Bottom nav: BARN_OWNER için "Barns" tab'ı "Ahırım" olarak mı rename edilmeli? → Evet
- AppNav'da rol nereden okunacak: ProfileRepository.getCurrentUser()? → Araştırılacak

