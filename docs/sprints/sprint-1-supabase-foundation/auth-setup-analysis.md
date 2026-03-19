# Feature: Auth Setup (Email + Google OAuth)

## Summary
Supabase Auth'ta email/password ve Google OAuth provider konfigüre edilir. Kullanıcı kaydolunca `user_profiles` tablosuna otomatik kayıt oluşturacak trigger yazılır.

## Auth Trigger
Firebase'deki pattern: kullanıcı oluşunca Firestore'da profil document oluşur.
Supabase'deki karşılık: `auth.users` üzerinde `AFTER INSERT` trigger.

## SQL Migration Dosyası

Oluşturulacak dosya: `supabase/migrations/20260318000003_auth_trigger.sql`

```sql
-- ============================================================
-- HorseGallop — Auth Trigger
-- Kullanıcı kayıt olunca otomatik profil + settings oluştur
-- ============================================================

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER SET search_path = public
AS $$
BEGIN
    -- Profil oluştur
    INSERT INTO public.user_profiles (id, email, first_name, last_name)
    VALUES (
        NEW.id,
        COALESCE(NEW.email, ''),
        COALESCE(NEW.raw_user_meta_data->>'first_name', ''),
        COALESCE(NEW.raw_user_meta_data->>'last_name', '')
    )
    ON CONFLICT (id) DO NOTHING;

    -- Varsayılan ayarlar oluştur
    INSERT INTO public.user_settings (user_id)
    VALUES (NEW.id)
    ON CONFLICT (user_id) DO NOTHING;

    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();
```

## Supabase Dashboard Adımları

### Email Auth
1. Dashboard → Authentication → Providers → Email
2. `Enable Email provider` → ON
3. `Confirm email` → isteğe bağlı (dev için kapalı bırak)

### Google OAuth
1. Dashboard → Authentication → Providers → Google
2. `Enable Google provider` → ON
3. `Client ID` ve `Client Secret` gir:
   - Google Cloud Console → `com-horse-gallop` projesi → OAuth 2.0 credentials
   - Authorized redirect URIs: `https://<project-ref>.supabase.co/auth/v1/callback`
4. Android'de deep link: Firebase'deki `horsegallop.page.link` yerine Supabase callback URL

### Deep Link (Android)
Firebase'de: `horsegallop.page.link/reset-password`
Supabase'de: `io.horsegallop://reset-callback` (Android scheme)
- AndroidManifest'te intent-filter eklenecek (Sprint 2)

## Tasks

**Layer 1 — Trigger Migration**
- [ ] `supabase/migrations/20260318000003_auth_trigger.sql` oluştur
- [ ] `supabase db push` ile uygula
- [ ] Supabase Dashboard'da trigger'ı doğrula (Database → Functions → `handle_new_user`)

**Layer 2 — Email Auth Test**
- [ ] Supabase Dashboard → Authentication → Users → "Add user"
- [ ] Email/password ile kullanıcı oluştur
- [ ] `user_profiles` tablosunda otomatik kayıt oluştuğunu doğrula
- [ ] `user_settings` tablosunda varsayılan ayarların oluştuğunu doğrula

**Layer 3 — Google OAuth**
- [ ] Google Cloud Console'da OAuth Client ID oluştur (Android + Web)
- [ ] Supabase Dashboard'a Google Client ID/Secret gir
- [ ] Web tarayıcısından Google login test et

## Acceptance Criteria
- [ ] Email/password kayıt → `user_profiles` otomatik oluşuyor
- [ ] Email/password kayıt → `user_settings` otomatik oluşuyor
- [ ] Google OAuth callback URL Supabase'de tanımlı
- [ ] `auth.users` tablosunda test kullanıcısı görünüyor
- [ ] `supabase db push` 3 migration hatasız tamamlanıyor

## Edge Cases
- Kullanıcı trigger'dan önce elle `user_profiles` INSERT ederse → `ON CONFLICT DO NOTHING` ile güvenli
- Google login'de email zaten varsa → Supabase otomatik merge eder
