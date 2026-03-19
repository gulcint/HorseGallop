# T-PAY — Payments: Google Play Billing + Supabase Verification

## Goal
Google Play Billing akışını uçtan uca tamamla. Satın alma sonrası purchase token'ı Supabase Edge Function ile server-side doğrula; `user_profiles.is_pro` ve `subscription_tier` alanlarını güvenilir şekilde set et.

## Mevcut Durum
- `BillingManager` (@Singleton) tam çalışıyor: connect, launchBillingFlow, acknowledge
- `SubscriptionViewModel.purchase(activity)` BillingManager'a delege ediyor
- `SubscriptionRepositoryImpl.startSubscriptionPurchase` → noop (`Result.success(Unit)`)
- Supabase'de sadece `ai-coach` Edge Function var; `verify-purchase` yok
- `restorePurchases` local trust ile çalışıyor (Play Store sorgusu var ama server verify yok)

## Asıl Eksikler
1. `startSubscriptionPurchase` gerçek Supabase çağrısı yapmıyor
2. Server-side purchase token doğrulaması yok (Google Play Developer API çağrısı yok)
3. `SubscriptionScreen`'de Activity referansı alımı kontrol edilmeli

## Kapsam (MVP)
- `supabase/functions/verify-purchase/index.ts`: purchaseToken + productId alır, Google Play Developer API'yi çağırır (RTDN), `user_profiles` günceller
- `SupabaseDataSource.verifyPurchase(purchaseToken: String, productId: String)` metodu
- `SubscriptionRepositoryImpl.startSubscriptionPurchase` içinde `verifyPurchase` çağrısı
- `SubscriptionScreen` Activity ref doğrulaması

## Kapsam Dışı
- iyzico/Stripe
- Webhook retry
- Grace period / billing retry

## Constraints
- Supabase migration tamamlandı; Firebase çağrısı yok
- `SupabaseDataSource` üzerinden geç
- SemanticColors zorunlu (SubscriptionScreen değişirse)

## Relevant Paths
- `supabase/functions/verify-purchase/` (YENİ)
- `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt`
- `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDtos.kt`
- `app/src/main/java/com/horsegallop/data/subscription/repository/SubscriptionRepositoryImpl.kt`
- `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionScreen.kt`
- `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionViewModel.kt`
- `app/src/main/java/com/horsegallop/data/billing/BillingManager.kt`

## Agent Sırası
1. `researcher` → SubscriptionScreen Activity ref + SupabaseDataSource method signatures
2. `operator` → Supabase Edge Function `verify-purchase`
3. `android-feature` → SupabaseDataSource + SubscriptionRepositoryImpl
4. `qa-verifier` → PASS/FAIL

## Acceptance Notes
- PurchaseState.Purchased tetiklenince verifyPurchase çağrılmalı
- verifyPurchase başarısız olursa error state UI'a yansımalı
- Google Play Developer API service account key Supabase secret'larına eklenmeli (GOOGLE_PLAY_SERVICE_ACCOUNT_KEY)

