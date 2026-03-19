-- ============================================================
-- HorseGallop — Seed Data
-- ============================================================

-- ─── HORSE BREEDS ──────────────────────────────────────────
INSERT INTO horse_breeds (id, name_en, name_tr, sort_order) VALUES
('breed_arabian',       'Arabian',          'Arap Atı',         1),
('breed_thoroughbred',  'Thoroughbred',     'İngiliz Safkanı',  2),
('breed_andalusian',    'Andalusian',       'Endülüs Atı',      3),
('breed_akhal_teke',    'Akhal-Teke',       'Ahal-Teke',        4),
('breed_quarter',       'Quarter Horse',    'Quarter Horse',    5),
('breed_warmblood',     'Warmblood',        'Sıcakkanlı',       6),
('breed_friesian',      'Friesian',         'Friesian',         7),
('breed_appaloosa',     'Appaloosa',        'Appaloosa',        8),
('breed_paint',         'Paint Horse',      'Paint Horse',      9),
('breed_haflinger',     'Haflinger',        'Haflinger',        10),
('breed_connemara',     'Connemara',        'Connemara',        11),
('breed_mixed',         'Mixed / Unknown',  'Melez / Bilinmiyor', 99)
ON CONFLICT (id) DO NOTHING;

-- ─── HORSE TIPS (Turkish) ──────────────────────────────────
INSERT INTO horse_tips (id, locale, category, title, body) VALUES
('tip_tr_1', 'tr', 'breed',      'Arap Atı: Eşsiz Anatomisi',   'Arap atları diğer ırklardan farklı olarak 17 kuyruk omuru taşır. Bu yapı, zarif ve yüksek kuyruk taşıma pozisyonuna zemin hazırlar.'),
('tip_tr_2', 'tr', 'physiology', 'At Kalbi: Bir Motor Gibi',    'Bir atın kalbi 3,6–4,5 kg ağırlığında olup egzersizde dakikada 38 litreye kadar kan pompalayabilir.'),
('tip_tr_3', 'tr', 'vision',     '360 Derece Görüş',            'Atlar neredeyse 360 derecelik panoramik görüşe sahiptir; yalnızca burnun hemen önü ve tam arkaları kör noktadır.'),
('tip_tr_4', 'tr', 'behavior',   'Ayakta Uyuma Sanatı',         'Atlar, bacaklarındaki kilitleme mekanizması sayesinde ayakta uyuyabilir. REM uykusu için kısa süreli yatmaları gerekir.'),
('tip_tr_5', 'tr', 'care',       'Atlar Neden Sürekli Otlar?',  'Atlar birer arka bağırsak fermantörüdür ve sindirim sağlığı için günde 16–18 saat otlanmaya ihtiyaç duyarlar.')
ON CONFLICT (id) DO NOTHING;

-- ─── HORSE TIPS (English) ──────────────────────────────────
INSERT INTO horse_tips (id, locale, category, title, body) VALUES
('tip_en_1', 'en', 'breed',      'Arabian: A Unique Skeleton',     'Arabian horses have 17 tail vertebrae instead of the usual 18–19 in other breeds, contributing to their high tail carriage.'),
('tip_en_2', 'en', 'physiology', 'The Horse Heart: A Powerhouse',  'A horse''s heart weighs 3.6–4.5 kg and can pump up to 38 litres per minute during intense exercise.'),
('tip_en_3', 'en', 'vision',     'Nearly 360° Vision',             'Horses have almost panoramic vision with only two blind spots: directly in front of their nose and directly behind them.'),
('tip_en_4', 'en', 'behavior',   'Horses Sleep Standing Up',       'Thanks to a passive stay apparatus in their legs, horses can sleep lightly while standing.'),
('tip_en_5', 'en', 'care',       'Horses Need to Graze Constantly', 'Horses need 16–18 hours of grazing per day for digestive health. Empty gut time significantly increases colic risk.')
ON CONFLICT (id) DO NOTHING;

-- ─── BARNS ─────────────────────────────────────────────────
INSERT INTO barns (id, name, description, location, lat, lng, tags, amenities, rating, review_count, capacity) VALUES
('barn_adin_country', 'Adin Country', 'Beginner to Pro rides', 'Istanbul, TR', 41.0082, 28.9784,
 ARRAY['cafe','indoor_arena','parking','lessons','open_now'],
 ARRAY['cafe','indoor_arena','parking','lessons'],
 4.7, 124, 20),
('barn_sable_ranch', 'Sable Ranch', 'Trail and endurance', 'Sarıyer, TR', 41.0151, 29.0037,
 ARRAY['outdoor_arena','trail','parking','boarding'],
 ARRAY['outdoor_arena','trail','parking','boarding'],
 4.5, 89, 15)
ON CONFLICT (id) DO NOTHING;

-- ─── LESSONS ───────────────────────────────────────────────
INSERT INTO lessons (id, barn_id, title, instructor_name, lesson_date, duration_min, level, price, spots_total, spots_available) VALUES
('lesson_1', 'barn_adin_country', 'Beginner Ride',        'Alice',         '2026-04-05 10:00+03', 60,  'Beginner',    1200, 10, 7),
('lesson_2', 'barn_sable_ranch',  'Trail Basics',          'Bob',           '2026-04-06 14:00+03', 75,  'Intermediate',1450, 8,  5),
('lesson_3', 'barn_adin_country', 'Dressaj Temelleri',     'Ayşe Kaya',    '2026-04-08 09:00+03', 60,  'Beginner',    1300, 8,  5),
('lesson_4', 'barn_sable_ranch',  'Atlama Teknikleri',     'Mehmet Yıldız','2026-04-10 15:00+03', 90,  'Advanced',    1800, 6,  2),
('lesson_5', 'barn_adin_country', 'Western Riding',        'Alice',         '2026-04-12 11:00+03', 60,  'Intermediate',1500, 10, 7),
('lesson_6', 'barn_sable_ranch',  'Endurance Training',    'Bob',           '2026-04-15 14:00+03', 120, 'Advanced',    2200, 4,  4)
ON CONFLICT (id) DO NOTHING;

-- ─── CHALLENGES ────────────────────────────────────────────
INSERT INTO challenges (id, title, title_en, description, description_en, target_value, unit, icon, end_date) VALUES
('challenge_first_ride', 'İlk Sürüş', 'First Ride', 'İlk sürüşünü tamamla', 'Complete your first ride',
  1, 'rides', '🏇', NOW() + INTERVAL '365 days'),
('challenge_10km', '10 km Yol', '10 km Journey', 'Toplam 10 km sürüş tamamla', 'Complete 10 km total',
  10, 'km', '🗺️', NOW() + INTERVAL '60 days'),
('challenge_weekly_5', 'Haftalık 5 Sürüş', 'Weekly 5 Rides', 'Bu hafta 5 sürüş tamamla', 'Complete 5 rides this week',
  5, 'rides', '🎯', NOW() + INTERVAL '7 days'),
('challenge_speed_20', 'Hız Ustası', 'Speed Master', 'Ortalama 20 km/h hıza ulaş', 'Reach avg speed 20 km/h',
  20, 'km/h', '⚡', NOW() + INTERVAL '30 days')
ON CONFLICT (id) DO NOTHING;

-- ─── APP CONTENT (TR) ──────────────────────────────────────
-- Keys must match ContentRepositoryImpl.kt exactly (underscore format)
INSERT INTO app_content (locale, key, value) VALUES
('tr', 'home_hero_title',                   'HorseGallop Pro ile Antrenmanını Geliştir'),
('tr', 'home_hero_subtitle',                'Günlük performansını takip et, hedeflerini yükselt.'),
('tr', 'offline_help',                      'İnternet bağlantısı yok. Veriler bağlantı kurulunca senkronize edilecek.'),
('tr', 'login_title',                       'HorseGallop''a Hoş Geldin'),
('tr', 'login_subtitle',                    'Sürüşlerini takip et, antrenman hedeflerini yükselt.'),
('tr', 'email_login_title',                 'E-posta ile Giriş'),
('tr', 'email_login_subtitle',              'E-posta adresin ve şifrenle devam et.'),
('tr', 'enroll_title',                      'Hesap Oluştur'),
('tr', 'enroll_subtitle',                   'HorseGallop''a katıl ve antrenmanını takip et.'),
('tr', 'forgot_password_subtitle',          'Şifreni sıfırlamak için e-posta adresini gir.'),
('tr', 'onboarding_hero_title',             'Atçılık yolculuğuna profesyonel başla'),
('tr', 'onboarding_hero_subtitle',          'Sürüşlerini kaydet, hedeflerini takip et.'),
('tr', 'onboarding_help_text',              'Her zaman hesabınla devam edebilirsin.'),
('tr', 'ride_live_title',                   'Canlı Sürüş'),
('tr', 'ride_live_subtitle_idle',           'Sürüşe başlamak için hazır'),
('tr', 'ride_live_subtitle_active',         'Sürüş devam ediyor...'),
('tr', 'ride_permission_title',             'Konum İzni Gerekli'),
('tr', 'ride_permission_hint',              'Sürüşünü takip etmek için konum iznine ihtiyacımız var.'),
('tr', 'ride_grant_location_cta',           'Konum İznini Ver'),
('tr', 'settings_theme_subtitle',           'Uygulama görünümünü özelleştir'),
('tr', 'settings_language_subtitle',        'Uygulama dilini seç'),
('tr', 'settings_notifications_subtitle',   'Bildirim tercihlerini yönet'),
('tr', 'settings_privacy_subtitle',         'Gizlilik ve veri ayarları'),
-- English
('en', 'home_hero_title',                   'Train Smarter with HorseGallop Pro'),
('en', 'home_hero_subtitle',                'Track your daily performance and level up.'),
('en', 'offline_help',                      'No internet connection. Data will sync when reconnected.'),
('en', 'login_title',                       'Welcome to HorseGallop'),
('en', 'login_subtitle',                    'Track rides and level up your training goals.'),
('en', 'email_login_title',                 'Sign in with Email'),
('en', 'email_login_subtitle',              'Continue with your email and password.'),
('en', 'enroll_title',                      'Create Account'),
('en', 'enroll_subtitle',                   'Join HorseGallop and start tracking your training.'),
('en', 'forgot_password_subtitle',          'Enter your email to reset your password.'),
('en', 'onboarding_hero_title',             'Start your riding journey like a pro'),
('en', 'onboarding_hero_subtitle',          'Log rides and track your goals.'),
('en', 'onboarding_help_text',              'You can always continue with your account.'),
('en', 'ride_live_title',                   'Live Ride'),
('en', 'ride_live_subtitle_idle',           'Ready to start your ride'),
('en', 'ride_live_subtitle_active',         'Ride in progress...'),
('en', 'ride_permission_title',             'Location Permission Required'),
('en', 'ride_permission_hint',              'We need location access to track your ride.'),
('en', 'ride_grant_location_cta',           'Grant Location Access'),
('en', 'settings_theme_subtitle',           'Customize the app appearance'),
('en', 'settings_language_subtitle',        'Choose your preferred language'),
('en', 'settings_notifications_subtitle',   'Manage your notification preferences'),
('en', 'settings_privacy_subtitle',         'Privacy and data settings')
ON CONFLICT (locale, key) DO UPDATE SET value = EXCLUDED.value;
