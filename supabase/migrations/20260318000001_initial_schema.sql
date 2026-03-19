-- ============================================================
-- HorseGallop — Initial Schema Migration
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── 1. USER PROFILES ──────────────────────────────────────
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    first_name TEXT NOT NULL DEFAULT '',
    last_name TEXT NOT NULL DEFAULT '',
    email TEXT NOT NULL DEFAULT '',
    phone TEXT DEFAULT '',
    city TEXT DEFAULT '',
    birth_date DATE,
    country_code TEXT DEFAULT 'TR',
    weight_kg NUMERIC,
    photo_url TEXT,
    is_pro BOOLEAN NOT NULL DEFAULT FALSE,
    subscription_tier TEXT NOT NULL DEFAULT 'FREE',
    subscription_expires_at TIMESTAMPTZ,
    last_purchase_token TEXT,
    last_purchase_product_id TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 2. USER SETTINGS ──────────────────────────────────────
CREATE TABLE user_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
    theme_mode TEXT NOT NULL DEFAULT 'SYSTEM',
    language TEXT NOT NULL DEFAULT 'SYSTEM',
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    weight_unit TEXT NOT NULL DEFAULT 'kg',
    distance_unit TEXT NOT NULL DEFAULT 'km',
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 3. HORSES ─────────────────────────────────────────────
CREATE TABLE horses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    breed TEXT DEFAULT '',
    birth_year INT,
    color TEXT DEFAULT '',
    gender TEXT NOT NULL DEFAULT 'unknown',
    weight_kg INT,
    image_url TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_horses_user_id ON horses(user_id);

-- ─── 4. RIDES ──────────────────────────────────────────────
CREATE TABLE rides (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    duration_sec INT NOT NULL DEFAULT 0,
    distance_km NUMERIC NOT NULL DEFAULT 0,
    calories NUMERIC DEFAULT 0,
    avg_speed_kmh NUMERIC DEFAULT 0,
    max_speed_kmh NUMERIC DEFAULT 0,
    ride_type TEXT NOT NULL DEFAULT 'FREE',
    barn_name TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    saved_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_rides_user_id ON rides(user_id);
CREATE INDEX idx_rides_started_at ON rides(started_at DESC);

-- ─── 5. RIDE PATH POINTS ───────────────────────────────────
CREATE TABLE ride_path_points (
    id BIGSERIAL PRIMARY KEY,
    ride_id TEXT NOT NULL REFERENCES rides(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    lat NUMERIC NOT NULL,
    lng NUMERIC NOT NULL,
    alt_m NUMERIC,
    speed_kmh NUMERIC,
    timestamp_ms BIGINT,
    sort_order INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_ride_path_points_ride_id ON ride_path_points(ride_id, sort_order);

-- ─── 6. BARNS ──────────────────────────────────────────────
CREATE TABLE barns (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    location TEXT DEFAULT '',
    lat NUMERIC,
    lng NUMERIC,
    tags TEXT[] DEFAULT '{}',
    amenities TEXT[] DEFAULT '{}',
    rating NUMERIC DEFAULT 0,
    review_count INT DEFAULT 0,
    hero_image_url TEXT,
    capacity INT DEFAULT 0,
    phone TEXT,
    is_federated BOOLEAN DEFAULT FALSE,
    owner_user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_barns_lat_lng ON barns(lat, lng);

-- ─── 7. BARN INSTRUCTORS ───────────────────────────────────
CREATE TABLE barn_instructors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    barn_id TEXT NOT NULL REFERENCES barns(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    photo_url TEXT DEFAULT '',
    specialty TEXT DEFAULT '',
    rating NUMERIC DEFAULT 0
);
CREATE INDEX idx_barn_instructors_barn_id ON barn_instructors(barn_id);

-- ─── 8. LESSONS ────────────────────────────────────────────
CREATE TABLE lessons (
    id TEXT PRIMARY KEY,
    barn_id TEXT REFERENCES barns(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    instructor_name TEXT NOT NULL DEFAULT '',
    lesson_date TIMESTAMPTZ,
    duration_min INT NOT NULL DEFAULT 60,
    level TEXT NOT NULL DEFAULT 'Beginner',
    price NUMERIC NOT NULL DEFAULT 0,
    spots_total INT NOT NULL DEFAULT 10,
    spots_available INT NOT NULL DEFAULT 10,
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_lessons_lesson_date ON lessons(lesson_date);
CREATE INDEX idx_lessons_barn_id ON lessons(barn_id);

-- ─── 9. RESERVATIONS ───────────────────────────────────────
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    lesson_id TEXT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    lesson_title TEXT NOT NULL DEFAULT '',
    lesson_date TEXT NOT NULL DEFAULT '',
    instructor_name TEXT NOT NULL DEFAULT '',
    barn_id TEXT,
    status TEXT NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_lesson_id ON reservations(lesson_id);

-- ─── 10. REVIEWS ───────────────────────────────────────────
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    author_name TEXT NOT NULL DEFAULT '',
    target_id TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_name TEXT NOT NULL DEFAULT '',
    rating INT NOT NULL DEFAULT 5,
    comment TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_target_id ON reviews(target_id);

-- ─── 11. HORSE HEALTH EVENTS ───────────────────────────────
CREATE TABLE horse_health_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    horse_id UUID NOT NULL REFERENCES horses(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    event_date TEXT NOT NULL,
    notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_horse_health_events_horse_id ON horse_health_events(horse_id);
CREATE INDEX idx_horse_health_events_user_id ON horse_health_events(user_id);

-- ─── 12. HEALTH EVENTS (rider) ─────────────────────────────
CREATE TABLE health_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    horse_id UUID REFERENCES horses(id) ON DELETE SET NULL,
    horse_name TEXT DEFAULT '',
    type TEXT NOT NULL,
    scheduled_date TIMESTAMPTZ NOT NULL,
    completed_date TIMESTAMPTZ,
    notes TEXT DEFAULT '',
    is_completed BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_health_events_user_id ON health_events(user_id);
CREATE INDEX idx_health_events_scheduled_date ON health_events(scheduled_date);

-- ─── 13. CHALLENGES ────────────────────────────────────────
CREATE TABLE challenges (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL DEFAULT '',
    title_en TEXT NOT NULL DEFAULT '',
    description TEXT DEFAULT '',
    description_en TEXT DEFAULT '',
    target_value NUMERIC NOT NULL DEFAULT 1,
    unit TEXT NOT NULL DEFAULT 'rides',
    icon TEXT DEFAULT '',
    start_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_date TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '30 days'
);

-- ─── 14. USER CHALLENGE PROGRESS ───────────────────────────
CREATE TABLE user_challenge_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    challenge_id TEXT NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    current_value NUMERIC NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, challenge_id)
);
CREATE INDEX idx_user_challenge_progress_user_id ON user_challenge_progress(user_id);

-- ─── 15. USER BADGES ───────────────────────────────────────
CREATE TABLE user_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    earned_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, type)
);
CREATE INDEX idx_user_badges_user_id ON user_badges(user_id);

-- ─── 16. FCM TOKENS ────────────────────────────────────────
CREATE TABLE fcm_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    platform TEXT DEFAULT 'android',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_fcm_tokens_user_id ON fcm_tokens(user_id);

-- ─── 17. SAFETY SETTINGS ───────────────────────────────────
CREATE TABLE safety_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE UNIQUE,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    alert_after_stillness_min INT NOT NULL DEFAULT 5,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 18. SAFETY CONTACTS ───────────────────────────────────
CREATE TABLE safety_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_safety_contacts_user_id ON safety_contacts(user_id);

-- ─── 19. HORSE BREEDS (content) ────────────────────────────
CREATE TABLE horse_breeds (
    id TEXT PRIMARY KEY,
    name_en TEXT NOT NULL DEFAULT '',
    name_tr TEXT NOT NULL DEFAULT '',
    sort_order INT NOT NULL DEFAULT 99
);

-- ─── 20. HORSE TIPS (content) ──────────────────────────────
CREATE TABLE horse_tips (
    id TEXT PRIMARY KEY,
    locale TEXT NOT NULL DEFAULT 'en',
    title TEXT NOT NULL DEFAULT '',
    body TEXT NOT NULL DEFAULT '',
    category TEXT DEFAULT ''
);
CREATE INDEX idx_horse_tips_locale ON horse_tips(locale);

-- ─── 21. APP CONTENT (localization) ────────────────────────
CREATE TABLE app_content (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    locale TEXT NOT NULL,
    key TEXT NOT NULL,
    value TEXT NOT NULL DEFAULT '',
    UNIQUE(locale, key)
);
CREATE INDEX idx_app_content_locale ON app_content(locale);

-- ─── 22. EQUESTRIAN ANNOUNCEMENTS ──────────────────────────
CREATE TABLE equestrian_announcements (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL DEFAULT '',
    summary TEXT DEFAULT '',
    published_at_label TEXT DEFAULT '',
    detail_url TEXT DEFAULT '',
    image_url TEXT,
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 23. EQUESTRIAN COMPETITIONS ───────────────────────────
CREATE TABLE equestrian_competitions (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL DEFAULT '',
    location TEXT DEFAULT '',
    date_label TEXT DEFAULT '',
    detail_url TEXT DEFAULT '',
    cached_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── 24. FEDERATION SYNC STATUS ────────────────────────────
CREATE TABLE federation_sync_status (
    id TEXT PRIMARY KEY,
    status TEXT NOT NULL DEFAULT 'idle',
    synced_at TIMESTAMPTZ,
    item_count INT DEFAULT 0,
    error_message TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── PURCHASE TOKENS (billing) ─────────────────────────────
CREATE TABLE purchase_tokens (
    token TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    product_id TEXT NOT NULL,
    redeemed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
