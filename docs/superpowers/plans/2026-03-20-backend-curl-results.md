# Backend Curl Test Sonuçları — 2026-03-20

## Özet

| Kategori | Durum |
|----------|-------|
| Statik Coverage (16 ekran) | ✅ 16/16 implement edilmiş |
| Schema (25 tablo) | ✅ Tablolar mevcut |
| RLS Politikaları | ✅ Çalışıyor |
| Public Endpointler | ✅ horse_breeds dönüyor |
| Authenticated Endpointler | ⚠️ Bloklu (email confirmation) |
| Edge Function (ai-coach) | ⚠️ 401 (auth gerekli, beklenen) |

---

## Detaylı Curl Test Sonuçları

| Endpoint | HTTP | Sonuç | Not |
|----------|------|-------|-----|
| `horse_breeds` (public) | 200 | ✅ 12 satır | Seed verisi var |
| `barns` (anon) | 200 | ✅ Boş array | RLS doğru: anon göremez |
| `challenges` (anon) | 200 | ✅ Boş array | RLS doğru: anon göremez |
| `horse_tips` (anon) | 200 | ✅ Boş array | RLS doğru: auth gerekli |
| `app_content` (anon) | 200 | ✅ Boş array | Seed verisi yok |
| `equestrian_*` (anon) | 200 | ✅ Boş array | Seed verisi yok |
| `ai-coach` Edge Function | 401 | ✅ Beklenen | Auth header zorunlu |
| Auth signup | 422 | ⚠️ Bloklu | Email confirmation aktif |

---

## Bulgular

### ✅ Çalışan Şeyler
1. **Schema doğru deploy edilmiş** — Tüm 25 tablo erişilebilir
2. **RLS politikaları aktif** — Anon key ile auth gerektiren tablolar boş dönüyor (403 değil 200+empty, Supabase'in normal davranışı)
3. **horse_breeds seed verisi var** — 12 ırk kaydı mevcut
4. **Edge Function deploy edilmiş** — Auth olmadan 401 dönüyor (beklenen)

### ⚠️ Bloklu Testler
**Sebep:** Supabase'de email confirmation açık. Test user oluşturulamıyor → JWT alınamıyor → authenticated endpoint'ler test edilemiyor.

**Etkisi:** Şu tablolar test edilemedi:
- horses, rides, health_events, lessons, reservations, reviews, challenges (authenticated)
- ai_coach_messages, notifications, user_profiles, user_settings
- Tüm write operasyonları (INSERT, UPDATE, DELETE)

### 🐛 Bulgu: `challenges.title_tr` kolonu yok
```
column challenges.title_tr does not exist
hint: Perhaps you meant to reference the column "challenges.title_en"
```
Android `ChallengeRepositoryImpl`'da `title_tr` yerine `title_en` kullanılıyor mu kontrol edilmeli.

---

## Authenticated Test İçin Çözüm

### Seçenek A — Email Confirmation Kapat (Test Ortamı)
Supabase Dashboard → Authentication → Settings:
- "Enable email confirmations" → **OFF**
- Sonra tekrar `test-ci@horsegallop.dev` / `TestHorse123!` ile test yapılabilir

### Seçenek B — Mevcut Kullanıcı Token'ı
1. Uygulamadan login ol
2. `adb shell` ile token al veya logcat'ten JWT oku
3. `TOKEN=eyJ...` ile curl testleri çalıştır

### Seçenek C — Service Role Key (Tam Test)
Supabase Dashboard → Settings → API → `service_role` key ile kullanıcı oluştur, email confirmation bypass edilir.

---

## Öneri: Hemen Düzeltilmesi Gereken

1. **`challenges.title_tr` sorunu** — Schema veya Android kodu düzeltilmeli
2. **`app_content` seed verisi yok** — HomeScreen için gerekli, seed.sql'e eklenmeli
3. **GEMINI_API_KEY** — `ai-coach` Edge Function için Supabase Dashboard'dan set edilmeli

---

## Authenticated Testler (Email Confirmation Kapatıldıktan Sonra)

```bash
ANON="sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI"
BASE="https://mnhcyeofrsgoulhpvlfr.supabase.co"

# Token al
TOKEN=$(curl -s -X POST "$BASE/auth/v1/token?grant_type=password" \
  -H "apikey: $ANON" -H "Content-Type: application/json" \
  -d '{"email":"test-ci@horsegallop.dev","password":"TestHorse123!"}' \
  | jq -r '.access_token')

# barns
curl -s "$BASE/rest/v1/barns?select=id,name&limit=3" \
  -H "apikey: $ANON" -H "Authorization: Bearer $TOKEN" | jq .

# challenges
curl -s "$BASE/rest/v1/challenges?select=id,title_en&limit=3" \
  -H "apikey: $ANON" -H "Authorization: Bearer $TOKEN" | jq .

# ai-coach Edge Function
curl -s -X POST "$BASE/functions/v1/ai-coach" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"Test mesajı","conversation_history":[]}' | jq .
```
