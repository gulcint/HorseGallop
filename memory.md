# Memory

Bu dosya, repo icinde kalici urun/backlog hafizasi icindir. Yeni oturumlarda once bu dosya, sonra `CLAUDE.md` okunmalidir.

## Current Product Memory

### Recently Completed

- PR merge ve stabilizasyon: `#75`, `#77`, `#78` merge edildi.
- Subscription akisi: `SubscriptionScreen` yeniden tasarimi, toggle, ozellik listesi, CTA, restore purchases ve billing baglantilari eklendi.
- Reusable paywall: `ProGate` bileşeni eklendi.
- Settings backend temeli: `getUserSettings` + `updateUserSettings`, `UserSettings` domain/repository/viewmodel baglantilari eklendi.
- Safety temeli: guvenilen kisi ekleme, canli konum linki, hareketsizlik alarmi ve backend akisi eklendi.
- Horse health temeli: `HorseHealthEvent` modeli, backend CRUD ve `HorseHealthScreen` eklendi.
- Binicilik gundemi temeli: federasyon verisi, agenda akisi ve barn entegrasyonu aktif gelistirme alaninda.
- Auth dark mode polish: login ve sifre yenileme CTA kontrastlari ortak semantic button stiliyle iyilestirildi.

## Backlog

Asagidaki plan urun backlog'u gibi ele alinmali. Her bolumde isler bitince durum guncellenmeli; kapsam degisirse risk ve bagimlilik satirlari da revize edilmeli.

### 1. PR Merge & Stabilize

**Durum:** Tamamlandi

**Icerik:**
- PR `#75`, `#77`, `#78` merge
- catisma cozumu
- temel branch stabilizasyonu

**Sonraki Adimlar:**
- `main` branch uzerinde tam temiz CI kosusunu izlemek
- branch protection ve required checks ayarlarini teyit etmek

**Riskler:**
- yerelde `google-services.json` ve benzeri gizli dosyalar yoksa gate calismasi eksik dogrulanabilir

**Bagimliliklar:**
- GitHub Actions secrets
- Android build secrets

### 2. Harita & Polyline

**Durum:** Kismen tamamlandi

**Icerik:**
- daha iyi ride haritasi goruntusu
- gait renkli segment kalitesini artirma
- rota cizimi ve okunabilirlik iyilestirmeleri
- `RideDetailScreen` icin nokta filtreleme, gait bazli segment cizimi ve start/end marker polish tamamlandi

**Sonraki Adimlar:**
- ayni segment/filter pipeline'ini `RideTrackingScreen` tarafina tasimayi degerlendir
- canli surus haritasinda ayni legend ve okunabilirlik standartlarini uygula
- gercek cihazda GPS gurultusu ile rota filtresini tekrar dogrula

**Riskler:**
- GPS verisi gurultuluysa segment goruntusu kotu olabilir
- performans sorunu olusabilir

**Bagimliliklar:**
- Google Maps
- ride metrics/domain modelleri

### 3. Barn Data

**Durum:** Kismen tamamlandi

**Icerik:**
- federasyon kulup verisi yoksa minimum fallback barn dizini saglama
- ilk yuklemede sessiz bos liste yerine gercek hata/fallback davranisini netlestirme
- `BarnsMapView` ve `Select Barn` akislarinda smoke test kaynakli arama/secim/fallback iyilestirmeleri yapildi

**Sonraki Adimlar:**
- federasyon scrape sonucu geldiginde fallback kullanim oranini olc
- federasyon koordinat senkronu tamamlandikca map fallback gorunumunu yeniden degerlendir

**Riskler:**
- fallback veri gercek federasyon listesinin yerini uzun sure almamali
- scrape/cache bozulursa kullanici demo veri gordugunu anlayamayabilir

**Bagimliliklar:**
- federasyon scrape cache
- barn listing/detail ekranlari

### 4. Gercek Bildirimler

**Durum:** Kismen tamamlandi

**Icerik:**
- hardcoded 5 bildirim yerine Firestore realtime listener
- rezervasyon bildirimi backend uretimi eklendi
- horse health yaklasan etkinlik reminder akisi eklendi
- genel bildirim icin backend entry point eklendi

**Sonraki Adimlar:**
- genel bildirim girisini admin/panel akisina bagla
- FCM push ile in-app listeyi ayni event modeli uzerinde birlestir

**Riskler:**
- gereksiz realtime dinleyici pil ve veri tuketimi yaratabilir
- reminder duplicate mantigi uzun vadede tekrar gozden gecirilmeli

**Bagimliliklar:**
- FCM
- Firestore notifications koleksiyonu

### 5. Ayarlar Backend

**Durum:** Kismen tamamlandi

**Icerik:**
- tema/dil/bildirim tercihleri Firestore'a kaydedilsin
- mevcut backend baglantisinin polish edilmesi

**Sonraki Adimlar:**
- `SettingsViewModel` ile ekrani uc uca kontrol et
- offline/geri yukleme davranisini netlestir
- hangi ayarlar local, hangileri remote kararini belgeye yaz
- `weightUnit` ve `distanceUnit` icin UI acilip acilmayacagini netlestir

**Riskler:**
- local ve remote ayar kaynaklari cakisirsa tutarsizlik olur

**Bagimliliklar:**
- `getUserSettings`
- `updateUserSettings`
- auth user context

### 6. Binicilik Gundemi

**Durum:** Kismen tamamlandi

**Icerik:**
- Federasyon duyurulari ve yarismalari icin agenda ekrani polish
- `TJK` modulu kaldirildi, yerine `EquestrianAgenda` geldi
- `Federe Kulüpler` verisi `Barns`, `BarnDetail` ve barn secim akislarina baglandi
- federasyon kulüpleri icin geocoding + cache + arka plan sync eklendi
- response aninda canli scrape yerine Firebase cache/Firestore-first veri akisi benimsendi
- manual/admin federation sync trigger eklendi; agenda ekranindan cache yenileme tetiklenebiliyor
- duyuru ve yarisma kartlari icin uygulama ici preview sheet eklendi

**Sonraki Adimlar:**
- ekran metinleri ve bos durumlari iyilestir
- tam detay sayfasi gerekip gerekmedigini degerlendir
- backend scraping guvenilirligini olc

**Riskler:**
- `binicilik.org.tr` HTML yapisi degisirse scraping kirilir
- rate limit veya gecikme olabilir

**Bagimliliklar:**
- backend proxy
- `binicilik.org.tr` veri kaynagi

### 7. At Saglik & Profil

**Durum:** Kismen tamamlandi

**Icerik:**
- HorseHealth calendar
- CRUD polish
- profil ve at detay akislarini iyilestirme

**Sonraki Adimlar:**
- liste/takvim gecisini UX olarak netlestir
- event tipleri, filtreler ve yaklasan randevu davranisini tamamla
- bildirim mantigini netlestir

**Riskler:**
- tarih/saat mantigi yanlis kurulursa hatali hatirlatma olur

**Bagimliliklar:**
- horse health backend CRUD
- local notification veya FCM stratejisi

### 8. Sosyal & Gamifikasyon

**Durum:** Bekliyor

**Icerik:**
- challenge
- rozet
- aylik mesafe ligi

**Sonraki Adimlar:**
- hangi davranislar odullendirilecek karar ver
- leaderboard kapsam ve anti-cheat kurallari yaz
- MVP'yi sadece tek challenge tipiyle baslat

**Riskler:**
- erken gamification urunun cekirdek degerinden dikkat calabilir

**Bagimliliklar:**
- ride history
- profile/community veri modeli

### 9. B2B Ahir Yonetimi

**Durum:** Bekliyor

**Icerik:**
- barn owner icin ders takvimi
- ogrenci takip

**Sonraki Adimlar:**
- barn owner rol modelini tasarla
- B2C navigasyondan nasil ayrilacagini belirle
- dashboard ve scheduling gereksinimlerini cikart

**Riskler:**
- B2C ve B2B ayni uygulamada hizla karmasiklasabilir

**Bagimliliklar:**
- auth roles
- barn management backend

### 10. AI Asistan & Odeme

**Durum:** Bekliyor

**Icerik:**
- Turkce Gemini coach
- iyzico/Stripe entegrasyonu

**Sonraki Adimlar:**
- AI asistanin gorev alanini sinirla
- odeme saglayicisi secimini netlestir
- mevcut Google Play Billing ile cakisma/rol farkini belirle

**Riskler:**
- AI kapsam kayarsa feature bloat olur
- odeme entegrasyonunda uyumluluk ve hukuki gereksinimler artar

**Bagimliliklar:**
- LLM provider karari
- payment provider karari

## Feature Audit Notes

Asagidaki maddeler "tamamlandi sanilan ama polish/validation ihtiyaci olan" alanlardir:

- Subscription screen ve Pro gate: final copy, analytics eventleri, failure state UX tekrar gozden gecirilmeli.
- Billing restore flow: sandbox ve real purchase callback dogrulamasi yapilmali.
- Safety flow: FCM + dynamic links + inactivity trigger uc uca test edilmeli.
- Horse health: CRUD, reminder mantigi ve tarih bazli empty states tekrar test edilmeli.
- Binicilik gundemi: scraping dayanikliligi ve fallback mesajlari polish edilmeli.
- Settings sync: local/remote precedence belgelendirilmeli.

## Engineering Decisions

### Onboarding Copy Direction

**Karar:** Onboarding metin tonu `guven veren / profesyonel` olacak.

**Neden:**
- Uygulamanin cekirdek degeri sadece ilham veya lifestyle degil; `ride tracking`, `safety`, `horse health`, `training plans`, `barns`, `federasyon gundemi` gibi daha ciddi ve guven odakli alanlari kapsiyor.
- Fazla `premium / iddiali` ton satis odakli hissedebilir.
- Fazla `samimi / motive edici` ton guvenlik ve takip ozelliklerini hafif gosterebilir.

**Uygulama Notu:**
- Basliklar profesyonel ve net olmali.
- Alt metinlerde hafif motive edici sicaklik olabilir.
- Onboarding copy, gercekten var olan feature'lari one cikarmali:
  - ride tracking
  - training plans
  - safety tracking
  - horse health
  - federation announcements and competitions
  - verified barns / reservations

### Hook and CI Policy

**Karar:** PR acilmadan once yerelde zorunlu gate calisacak; GitHub Actions basarili olmadan merge olmayacak.

**Yerel Kurallar:**
- `bash scripts/setup-hooks.sh` ile git hook kurulumu yapilir.
- `pre-commit` hizli kontrol icindir:
  - conflict marker kontrolu
  - shell syntax kontrolu
- `pre-push` zorunlu gate'tir:
  - `lintDebug`
  - `testDebugUnitTest`
- PR acmak icin standart yol:
  - `bash scripts/pr-pipeline-merge.sh`

**PR Kurallari:**
- Yerel gate gecmeden PR acilmaz.
- PR scripti branch'i push eder, PR acar veya mevcut PR'i kullanir.
- Sonrasinda `gh pr merge --auto --squash --delete-branch` ile auto-merge aktif edilir.

**CI Kurallari:**
- PR workflow:
  - Android gate (`lintDebug`, `testDebugUnitTest`, `assembleDebug`)
  - backend build
- Main workflow:
  - Android gate (`lintDebug`, `testDebugUnitTest`, `assembleDebug`)
  - backend build

**Neden:**
- Kirik test/lint ile PR acilmasini engellemek
- Merge kararini yalnizca yerel ortama birakmamak
- CI yapisini sade, okunur ve zorunlu tutmak

**Operasyonel Not:**
- Yerel gate icin su dosyalar/ayarlar gerekebilir:
  - `app/google-services.json`
  - `GOOGLE_MAPS_API_KEY`
  - Android SDK (`ANDROID_HOME` veya `local.properties`)

## Working Rules For Future Sessions

- Yeni urun/backlog kararlari once bu dosyaya islenmeli.
- Bir bolumde is yapildiginda en az `Durum` ve `Sonraki Adimlar` guncellenmeli.
- "Kismen tamamlandi" durumundaki bolumler, yeni feature eklemekten once polish ve verification acisindan tekrar kontrol edilmeli.
