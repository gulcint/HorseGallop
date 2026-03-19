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
