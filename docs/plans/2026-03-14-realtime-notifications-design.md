# Realtime Notifications Design

## Goal

Hardcoded bildirim yaklaşımını tamamen bırakıp mevcut `users/{uid}/notifications` Firestore realtime akışını gerçek backend üreticileriyle beslemek.

İlk tur kapsamı:
- rezervasyon bildirimi
- yaklaşan at sağlığı etkinliği bildirimi
- genel sistem/federasyon duyurusu bildirimi

## Current State

- App tarafında `NotificationRepositoryImpl` zaten `users/{uid}/notifications` alt koleksiyonunu realtime dinliyor.
- `NotificationsScreen` ve `NotificationsViewModel` listeleme + okundu işaretleme davranışını destekliyor.
- Eksik olan taraf, bu koleksiyonu tutarlı şekilde dolduran backend üreticileri ve veri modelinin netleştirilmesi.

## Recommended Approach

Mevcut Firestore tabanlı bildirim mimarisini koru ve backend yazımını tek helper etrafında merkezileştir.

Bu yaklaşımın artıları:
- mevcut app mimarisiyle uyumlu
- düşük riskli
- hızlı değer üretir
- FCM ile ileride genişletilebilir

Eksileri:
- ilk turda tam template sistemi veya ayrı notification service yok

## Data Model

Yol:

`users/{uid}/notifications/{notificationId}`

Alanlar:

- `type`: `general | reservation | lesson | horse_health`
- `title`: string
- `body`: string
- `timestamp`: epoch millis
- `isRead`: boolean
- `targetId`: string?
- `targetRoute`: string?

Not:
- App tarafı eski verilerle uyumlu kalmalı.
- Yeni alanlar opsiyonel okunmalı.

## Producers

### 1. Reservation Notifications

Rezervasyon oluşturma akışında kullanıcıya bildirim yazılacak.

İçerik örneği:
- title: `Rezervasyon oluşturuldu`
- body: `Başlangıç seviye dersin 18 Mart için kaydedildi.`

### 2. Horse Health Reminder Notifications

Yaklaşan sağlık etkinliklerini kontrol eden bir scheduler/helper eklenecek.

İlk tur davranışı:
- yaklaşan 24 saat içindeki etkinlikler için bildirim üret
- aynı etkinlik için tekrar yazmayı önle

İçerik örneği:
- title: `Yaklaşan sağlık randevusu`
- body: `Aşı kontrolü yarın için planlandı.`

### 3. General/Federation Notifications

Genel sistem veya federasyon duyuruları için kontrollü backend entry point olacak.

İlk turda bu iki yoldan biri yeterli:
- callable/admin function
- seed/dev helper

Amaç:
- uygulama ekranını yalnızca rezervasyonlara bağlı bırakmamak
- duyuru tipi veri gösterebilmek

## App Changes

### Repository

`NotificationRepositoryImpl`
- yeni `type` değerlerini map etmeli
- `targetId` ve `targetRoute` alanlarını okuyabilmeli
- eski dokümanlarla geriye uyumlu kalmalı

### Domain Model

`AppNotification`
- `targetId`
- `targetRoute`

`NotificationType`
- `HORSE_HEALTH` eklenecek

### ViewModel / Screen

İlk turda ana UX değişikliği gerekmez.

Opsiyonel küçük polish:
- hata state
- boş state copy iyileştirmesi
- tap sonrası hedefe yönlenmeye hazır alan

## Error Handling

- Firestore listener hata verirse app boş liste yerine log + mevcut state korumalı
- backend helper yazımı `runCatching` yerine kontrollü validation ile yapılmalı
- duplicate reminder üretimi backend tarafında engellenmeli

## Testing

### Backend

- notification payload helper unit test
- horse health reminder duplicate guard test
- reservation flow notification write test

### Android

- repository mapping tests
- yeni type alanı için icon/render testi
- mevcut `NotificationsViewModel` unread count regresyon testi

## Risks

- health reminder scheduler duplicate bildirim üretebilir
- timestamp formatı karışırsa sıralama bozulur
- genel duyuru akışı auth/admin sınırı olmadan açılırsa kötüye kullanılabilir

## Non-Goals

- full notification preferences matrix
- push delivery analytics
- deep-link navigation tamamı
- topic bazlı segmentation

## Success Criteria

- kullanıcı ekranında gerçek backend kaynaklı bildirimler görünmeli
- rezervasyon bildirimi Firestore’a yazılmalı
- yaklaşan horse health etkinliği için tekil reminder üretilebilmeli
- mark as read ve mark all read davranışı çalışmaya devam etmeli
