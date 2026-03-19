# Feature: Supabase Proje Kurulumu

## Summary
Supabase'de yeni proje oluşturulur, proje URL ve API anahtarları not edilir, `supabase` CLI kurulur, proje dizini hazırlanır.

## Tasks

**Layer 1 — Supabase Dashboard**
- [ ] supabase.com'da yeni proje oluştur: `horsegallop`, region: `eu-central-1` (Frankfurt — Türkiye'ye yakın)
- [ ] Proje oluşunca `Project URL` ve `anon public key`'i not et → `supabase/config.md` dosyasına yaz

**Layer 2 — CLI & Proje Dizini**
- [ ] `npm install -g supabase` ile CLI kur (veya `brew install supabase/tap/supabase`)
- [ ] Proje kökünde `supabase init` çalıştır → `supabase/` dizini oluşur
- [ ] `supabase link --project-ref <project-ref>` ile projeyi bağla
- [ ] `supabase/migrations/` dizinini oluştur — tüm SQL migration'lar buraya gidecek

## Acceptance Criteria
- [ ] `supabase status` komutu proje bilgilerini gösteriyor
- [ ] Dashboard'da proje görünüyor, status "Active"
- [ ] `supabase/` dizini git'e commit edilebilir halde
