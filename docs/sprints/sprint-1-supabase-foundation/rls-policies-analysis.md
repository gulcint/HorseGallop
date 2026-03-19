# Feature: Row Level Security (RLS) Politikaları

## Summary
Her tablo için RLS etkinleştirilir ve politikalar tanımlanır. Temel kural: kullanıcı sadece kendi verisine erişebilir. İçerik tabloları (breeds, tips, barns, challenges) herkese açık.

## RLS Kategorileri

| Kategori | Tablolar | Politika |
|----------|---------|---------|
| Kullanıcı verisi | `user_profiles`, `user_settings`, `horses`, `rides`, `ride_path_points`, `reservations`, `reviews`, `horse_health_events`, `health_events`, `user_badges`, `user_challenge_progress`, `fcm_tokens`, `safety_settings`, `safety_contacts` | `auth.uid() = user_id` |
| İçerik (herkese açık) | `horse_breeds`, `horse_tips`, `app_content`, `challenges`, `equestrian_announcements`, `equestrian_competitions` | SELECT herkes, INSERT/UPDATE/DELETE sadece service_role |
| Paylaşımlı | `barns`, `barn_instructors`, `lessons` | SELECT herkes (auth gerekli), barn owner için write |
| Sistem | `federation_sync_status`, `purchase_tokens` | service_role only |

## SQL Migration Dosyası

Oluşturulacak dosya: `supabase/migrations/20260318000002_rls_policies.sql`

```sql
-- ============================================================
-- HorseGallop — RLS Policies
-- ============================================================

-- ─── USER PROFILES ─────────────────────────────────────────
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own profile"
    ON user_profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
    ON user_profiles FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile"
    ON user_profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- ─── USER SETTINGS ─────────────────────────────────────────
ALTER TABLE user_settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own settings"
    ON user_settings FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── HORSES ────────────────────────────────────────────────
ALTER TABLE horses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own horses"
    ON horses FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── RIDES ─────────────────────────────────────────────────
ALTER TABLE rides ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own rides"
    ON rides FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── RIDE PATH POINTS ──────────────────────────────────────
ALTER TABLE ride_path_points ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own ride path points"
    ON ride_path_points FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── BARNS (public read) ───────────────────────────────────
ALTER TABLE barns ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read barns"
    ON barns FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "Barn owners can update their barn"
    ON barns FOR UPDATE
    USING (auth.uid() = owner_user_id);

-- ─── BARN INSTRUCTORS (public read) ────────────────────────
ALTER TABLE barn_instructors ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read instructors"
    ON barn_instructors FOR SELECT
    USING (auth.role() = 'authenticated');

-- ─── LESSONS (public read) ─────────────────────────────────
ALTER TABLE lessons ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read lessons"
    ON lessons FOR SELECT
    USING (auth.role() = 'authenticated');

-- ─── RESERVATIONS ──────────────────────────────────────────
ALTER TABLE reservations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own reservations"
    ON reservations FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── REVIEWS ───────────────────────────────────────────────
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read reviews"
    ON reviews FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "Users can manage own reviews"
    ON reviews FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own reviews"
    ON reviews FOR DELETE
    USING (auth.uid() = user_id);

-- ─── HORSE HEALTH EVENTS ───────────────────────────────────
ALTER TABLE horse_health_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own horse health events"
    ON horse_health_events FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── HEALTH EVENTS ─────────────────────────────────────────
ALTER TABLE health_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own health events"
    ON health_events FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── CHALLENGES (public read) ──────────────────────────────
ALTER TABLE challenges ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read challenges"
    ON challenges FOR SELECT
    USING (auth.role() = 'authenticated');

-- ─── USER CHALLENGE PROGRESS ───────────────────────────────
ALTER TABLE user_challenge_progress ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own challenge progress"
    ON user_challenge_progress FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── USER BADGES ───────────────────────────────────────────
ALTER TABLE user_badges ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read own badges"
    ON user_badges FOR SELECT
    USING (auth.uid() = user_id);

-- ─── FCM TOKENS ────────────────────────────────────────────
ALTER TABLE fcm_tokens ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own fcm tokens"
    ON fcm_tokens FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── SAFETY ────────────────────────────────────────────────
ALTER TABLE safety_settings ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own safety settings"
    ON safety_settings FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

ALTER TABLE safety_contacts ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can manage own safety contacts"
    ON safety_contacts FOR ALL
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- ─── CONTENT TABLES (public read, service_role write) ──────
ALTER TABLE horse_breeds ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Anyone can read breeds"
    ON horse_breeds FOR SELECT USING (true);

ALTER TABLE horse_tips ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authenticated users can read tips"
    ON horse_tips FOR SELECT USING (auth.role() = 'authenticated');

ALTER TABLE app_content ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authenticated users can read app content"
    ON app_content FOR SELECT USING (auth.role() = 'authenticated');

ALTER TABLE equestrian_announcements ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authenticated users can read announcements"
    ON equestrian_announcements FOR SELECT USING (auth.role() = 'authenticated');

ALTER TABLE equestrian_competitions ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Authenticated users can read competitions"
    ON equestrian_competitions FOR SELECT USING (auth.role() = 'authenticated');
```

## Tasks

**Layer 1 — Migration Dosyası**
- [ ] `supabase/migrations/20260318000002_rls_policies.sql` dosyasını oluştur (yukarıdaki SQL)
- [ ] `supabase db push` ile uygula

**Layer 2 — Doğrulama**
- [ ] `user_profiles` tablosuna RLS olmadan erişmeye çalış → `permission denied` hatası al
- [ ] Auth token ile `user_profiles` sorgula → sadece kendi kaydı gelsin
- [ ] `horse_breeds` tablosunu anonim token ile sorgula → veriler gelsin
- [ ] Başka kullanıcının `horses` kaydını okumaya çalış → boş sonuç gelsin

## Acceptance Criteria
- [ ] Tüm kullanıcı tablolarında RLS `ENABLED`
- [ ] `user_profiles` RLS testi: user A, user B'nin profilini göremez
- [ ] `horse_breeds` RLS testi: anonim erişim çalışıyor
- [ ] `barns` RLS testi: authenticated kullanıcı tüm barns'ı görebiliyor
