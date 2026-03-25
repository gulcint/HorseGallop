-- ============================================================
-- Migration: app_content key formatını düzelt
-- Eski format: "home.heroTitle" → Yeni format: "home_hero_title"
-- ContentRepositoryImpl.kt ile eşleşmesi için gerekli
-- ============================================================

-- Eski dot-notation key'leri sil
DELETE FROM app_content WHERE key LIKE '%.%';

-- Doğru key formatıyla ekle (ContentRepositoryImpl.kt map ile eşleşir)
INSERT INTO app_content (locale, key, value) VALUES
-- Turkish
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
