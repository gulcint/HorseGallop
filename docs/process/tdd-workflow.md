# HorseGallop TDD Workflow

Bu dokuman, `codex-toolkit 2` kurallarinin HorseGallop uygulamasina pratik uyarlamasidir.

## 1) Zorunlu Dongu

Her gelistirme isinde su dongu kullanilir:

1. RED: Failing test yazilir, implementasyon dosyasi degismez.
2. GREEN: Testi gecirecek minimum kod yazilir.
3. REFACTOR: Davranisi degistirmeden kod temizlenir.

## 2) Oturum Kurallari

1. Bir oturumda en fazla 4 test fonksiyonu yaz.
2. RED asamasinda implementation degisikligi yapma.
3. GREEN asamasinda kapsam genisletme yapma; sadece failing testi gecir.
4. REFACTOR sonunda testler yeniden yesil olmalidir.

## 3) Komut Eslemesi

```bash
./tdd-hooks.sh red --ticket HG-123
./tdd-hooks.sh green --ticket HG-123
./tdd-hooks.sh refactor --ticket HG-123
./tdd-hooks.sh check
```

Gun sonu veya PR oncesi kalite dogrulamasi:

```bash
./dev-hooks.sh --ticket HG-123
```

## 4) PR Kalite Kapisi

PR acmadan once asgari kontrol:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon
```

Zorunlu kurallar:

1. Build green olmadan PR acilmaz.
2. Lint hatalari baseline disinda artirilmaz.
3. Unit testler green olmadan merge edilmez.

## 5) Fail Durumunda Recovery

1. RED fazinda test geciyorsa: test kapsamini sikilastir, hedeflenen davranisi yeniden tanimla.
2. GREEN fazinda test gecmiyorsa: yeni ozellik ekleme, sadece failing noktayi duzelt.
3. REFACTOR fazinda test kirildiysa: son degisikligi geri al, daha kucuk adimlarla tekrar et.
4. CI fail olursa once lokalde ayni komutu calistir, sonra duzeltme commit'i at.

## 6) Branch ve Commit Oruntusu

1. Branch adlari: `codex/feat-*`, `codex/fix-*`, `codex/refactor-*`.
2. Commit formati: `<type>: <description>`.
3. TDD fazlarina uygun commit:
   - `test: RED ...`
   - `feat: GREEN ...`
   - `refactor: ...`

## 7) HorseGallop Ozel Notlar

1. Yeni Gradle modulu acilmaz; tum gelistirme `:app` ve gerekirse `backend/` altinda yapilir.
2. UI tarafinda statik renk kullanimi yasak; tema tokenlari disina cikilmaz.
3. Ride ve Profile gibi kritik akislarda regresyon riski varsa once test yazilir.
