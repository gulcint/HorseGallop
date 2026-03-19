# Backend Coverage & Curl Test Planı

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Her ekran için Supabase backend'in çalışıp çalışmadığını curl ile doğrulamak ve coverage tablosu üretmek.

**Architecture:** Supabase anon key ile test user oluştur → JWT al → her tablo/endpoint için curl testi → sonuçları tabloya yaz.

**Tech Stack:** curl, Supabase PostgREST REST API, Supabase Auth v1 API, Edge Functions

**Supabase URL:** `https://mnhcyeofrsgoulhpvlfr.supabase.co`
**Anon Key:** `sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI`

---

## Coverage Durumu (Statik Analiz — Tamamlandı)

| Ekran | ViewModel | Backend | Durum |
|-------|-----------|---------|-------|
| Home | HomeViewModel | getRecentActivities, getUserStats, getAppContent, getHorseTips | ✅ |
| Horse List | HorseViewModel | getMyHorses, addHorse, deleteHorse, getBreeds | ✅ |
| Horse Health | HorseHealthViewModel | getHorseHealthEvents, add/update/delete | ✅ |
| Barn List | BarnViewModel | getBarns, toggleFavorite | ✅ |
| Barn Detail | BarnDetailViewModel | getBarnDetail, getLessons, bookLesson | ✅ |
| Schedule | ScheduleViewModel | getLessons, bookLesson, cancelReservation, getMyReservations | ✅ |
| Ride Tracking | RideTrackingViewModel | saveRide, getBarns, getChallenges | ✅ |
| Ride Detail | RideDetailViewModel | getRide | ✅ |
| Health | HealthViewModel | getHealthEvents, save, delete, markCompleted | ✅ |
| Challenges | ChallengeViewModel | getActiveChallenges, getEarnedBadges | ✅ |
| Notifications | NotificationsViewModel | getNotifications, markRead, markAllRead | ✅ |
| AI Coach | AiCoachViewModel | askAiCoach, getRideHistory | ✅ |
| Reviews | ReviewViewModel | submitReview, getMyReviews | ✅ |
| TBF | TbfViewModel | getEventDay, getEventCard, getUpcomingEvents | ✅ |
| Settings | SettingsViewModel | getUserSettings, updateUserSettings | ✅ |
| Barn Management | BarnDashboardViewModel | getBarnStats, getManagedLessons, cancelLesson | ✅ |

**Sonuç: 16/16 ekran implement edilmiş.**

---

## Task 1: Public Endpoint Testi (Auth Gerektirmez)

**Files:** Yok (sadece curl)

- [ ] **Step 1: horse_breeds tablosunu test et**

```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/horse_breeds?select=id,name_tr&limit=3" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Content-Type: application/json" | jq .
```
Beklenen: `[{"id":"breed_arabian","name_tr":"Arap Atı"}, ...]`

---

## Task 2: Test Kullanıcısı Oluştur ve JWT Al

- [ ] **Step 1: Test user signup**

```bash
curl -s -X POST "https://mnhcyeofrsgoulhpvlfr.supabase.co/auth/v1/signup" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Content-Type: application/json" \
  -d '{"email":"test-ci@horsegallop.dev","password":"TestHorse123!"}' | jq '{access_token: .access_token, user_id: .user.id}'
```
Beklenen: `access_token` ve `user_id` gelir.

- [ ] **Step 2: TOKEN değişkenine ata**

```bash
TOKEN=$(curl -s -X POST "https://mnhcyeofrsgoulhpvlfr.supabase.co/auth/v1/token?grant_type=password" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Content-Type: application/json" \
  -d '{"email":"test-ci@horsegallop.dev","password":"TestHorse123!"}' | jq -r '.access_token')
echo "TOKEN: ${TOKEN:0:20}..."
```

---

## Task 3: Authenticated Endpoint Testleri

Her endpoint için: **HTTP 200 + JSON array** beklenir. **401/403** = RLS sorunu, **404** = tablo yok.

- [ ] **Step 1: barns**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/barns?select=id,name&limit=3" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq 'length, .[0].name'
```

- [ ] **Step 2: challenges**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/challenges?select=id,title_tr&limit=3" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

- [ ] **Step 3: horse_tips**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/horse_tips?select=id,title&limit=3" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

- [ ] **Step 4: app_content**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/app_content?select=key,value&limit=5" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

- [ ] **Step 5: horses (kendi atları — boş olmalı)**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/horses?select=id,name" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq 'if type == "array" then "OK: \(length) rows" else . end'
```

- [ ] **Step 6: health_events**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/health_events?select=id,title" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq 'if type == "array" then "OK: \(length) rows" else . end'
```

- [ ] **Step 7: notifications**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/notifications?select=id,title&limit=5" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq 'if type == "array" then "OK: \(length) rows" else . end'
```

- [ ] **Step 8: ai_coach_messages**
```bash
curl -s "https://mnhcyeofrsgoulhpvlfr.supabase.co/rest/v1/ai_coach_messages?select=id&limit=5" \
  -H "apikey: sb_publishable_z86vAxxJCk-IZhNLWlkUuA_JifaT4sI" \
  -H "Authorization: Bearer $TOKEN" | jq 'if type == "array" then "OK: \(length) rows" else . end'
```

---

## Task 4: Edge Function Testi (AI Coach)

- [ ] **Step 1: ai-coach Edge Function'ı çağır**

```bash
curl -s -X POST "https://mnhcyeofrsgoulhpvlfr.supabase.co/functions/v1/ai-coach" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"Merhaba, antrenman önerisi ver","conversation_history":[]}' | jq '{status: .status, reply_preview: (.reply // .error // . | tostring | .[0:100])}'
```
Beklenen: `{"reply":"..."}` veya hata ise `GEMINI_API_KEY eksik` mesajı

---

## Task 5: Sonuç Tablosu

Her testin çıktısı `docs/superpowers/plans/2026-03-20-backend-curl-results.md` dosyasına yazılır:

| Endpoint | HTTP | Sonuç | Not |
|----------|------|-------|-----|
| horse_breeds | 200 | ✅ | - |
| auth/signup | 200 | ✅ | - |
| barns | ? | ? | - |
| ... | | | |
