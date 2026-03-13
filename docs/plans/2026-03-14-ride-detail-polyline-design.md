# Ride Detail Polyline Design

## Goal

`RideDetailScreen` icindeki rota gorunumunu daha okunur ve guvenilir hale getirmek.

Ilk tur kapsam:
- path kalite filtresi
- gait renkli segmentler
- baslangic / bitis markerlari
- daha iyi camera fit
- kucuk bir gait legend

## Current State

- `RideDetailScreen` mevcut rota noktalariyla dogrudan polyline ciziyor.
- Veride GPS jitter veya cok yakin nokta tekrarlarinin rota goruntusunu bozma ihtimali var.
- Kullanici rota tiplerini veya gait gecislerini haritada yeterince net okuyamiyor.

## Recommended Approach

Canli surus ekranina dokunmadan, sadece detay ekrani icin okunabilir bir segment pipeline olustur.

Temel fikir:
- `RideSession.pathPoints` listesinden temizlenmis bir nokta listesi uret
- bu noktalari gait bazli segmentlere ayir
- her segmenti kendi rengiyle ciz

Bu yaklasimin artisi:
- dusuk risk
- geri donusu kolay
- kullaniciya gorunur kalite farki yaratir

Eksisi:
- live tracking ile ortak pipeline henuz olusmaz

## Data Processing

### Point Filtering

Asagidaki filtreler uygulanmali:
- ard arda ayni / neredeyse ayni noktalari ele
- fiziksel olarak imkansiz ani ziplamalari ele
- iki nokta arasi cok kisa mesafeyi tekilleştir

Amaç:
- haritada tirtili rota yerine daha temiz bir cizgi

### Segment Building

Temiz noktalardan ardışık segmentler üretilmeli.

Her segment icin:
- `start`
- `end`
- `gait`
- `color`

Gait degisirse yeni segment acilmali.

## Map Presentation

### Polyline

- stroke mevcut halden daha okunur olmali
- cap/join yumusak kalmali
- renkler semantic ve ayirt edilebilir olmali

### Camera

- tum rota bounds ile fit edilmeli
- padding ekran kart yapisina uygun olmali

### Markers

- baslangic ve bitis markerlari acik secik gosterilmeli
- tek noktalik / cok kisa rota durumunda guvenli fallback olmali

## Legend

Map kartinin altinda veya ustunde kompakt bir legend:
- walk
- trot
- canter
- gallop

Legend sade olmali; haritayi kalabaliklastirmamali.

## Error / Edge Cases

- nokta sayisi 0 ise bos state
- nokta sayisi 1 ise tek marker
- filtre sonucu tum rota cok kisalirsa degrade gracefully
- gait bilgisi eksikse varsayilan renk kullan

## Testing

### Unit-Level

- segment builder testleri
- point filtering testleri
- gait degisim testleri

### UI-Level

- compile + lint
- manual smoke:
  - rota gorunuyor mu
  - markerlar dogru mu
  - legend okunuyor mu

## Non-Goals

- live tracking ekranini ayni turda yeniden yazmak
- backend ride data modelini degistirmek
- elevation profile mantigini degistirmek

## Success Criteria

- rota cizgisi daha temiz gorunmeli
- gait degisimleri haritada fark edilebilmeli
- baslangic ve bitis net secilmeli
- `RideDetailScreen` compile/lint/test kapisindan gecmeli
