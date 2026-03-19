# Decision Log — T-PAY

## Entries

- [2026-03-19] Decision: iyzico/Stripe MVP kapsam disi
  - Context: Backlog'da iyzico secenegi vardi
  - Reason: Google Play Billing skeleton hazir, iyzico ayri backend entegrasyonu ve web UI gerektirir
  - Impact: Bu sprint sadece Play Billing tamamlanir

- [2026-03-19] Decision: Server-side purchase token dogrulama zorunlu
  - Context: Mevcut kodda client-trust ile is_pro setleniyor
  - Reason: Guvenlik — purchaseToken olmadan is_pro manipule edilebilir
  - Impact: Supabase Edge Function verify-purchase gerekli, GOOGLE_PLAY_SERVICE_ACCOUNT_KEY secret gerekli

- [2026-03-19] Decision: BarnDetail onContactClick zaten implement edilmis — kapsam disi
  - Context: Brief'te "boş" olarak listelenmisti
  - Reason: Kod incelemesinde ACTION_DIAL implementasyonu bulundu (satir 524-529)
  - Impact: Bu iş yapilmayacak

