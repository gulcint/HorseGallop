# Sprint 1 — Supabase Foundation

**Goal:** Supabase projesi oluşturulmuş, 24 PostgreSQL tablosu ve RLS politikaları hazır, Auth provider'lar (email + Google) konfigüre edilmiş. Android'e tek satır dokunulmaz.
**Features:** 4
**Estimated tasks:** 18

## Feature List

| Feature | Etkilenen Alan | Tahmini Görev |
|---------|---------------|---------------|
| Supabase proje kurulumu | Supabase Dashboard | 2 |
| SQL Schema (24 tablo) | Supabase SQL Editor | 8 |
| RLS Politikaları | Supabase SQL Editor | 5 |
| Auth Setup (email + Google) | Supabase Dashboard + Google Console | 3 |

## Dependencies
- Requires: Firebase projesi (google-services.json, proje ID) — referans için
- Blocks: Sprint 2 (Android SDK), Sprint 3+ (tüm veri katmanı)

## Sprint Sonu Durumu
- Supabase URL ve anon key elde edilmiş
- `supabase_url`, `supabase_anon_key` notlanmış
- Tüm tablolar oluşturulmuş ve RLS açık
- Test kullanıcısı email/password ile oluşturulabiliyor
- Google OAuth test edilmiş
