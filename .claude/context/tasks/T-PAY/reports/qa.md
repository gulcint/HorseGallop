# QA Raporu — T-PAY (Google Play Billing + verifyPurchase)

**Tarih:** 2026-03-19
**QA Agent:** qa-verifier
**Onceki Rapor:** FAIL (iki blocker: tier mapping hatasi + test dosyasi yok)
**Bu Rapor:** Yeniden degerlendirme (her iki blocker duzeltildi iddiasi uzerine)

---

## Sonuc

PASS — T-PAY

---

## Kontrol Ozeti

| Kontrol | Sonuc | Not |
|---|---|---|
| Semantic Token Taramasi (feature/subscription) | PASS | Color.* ihlali yok |
| enforceSemanticSurfaceTokens | PASS | assembleDebug BUILD SUCCESSFUL |
| assembleDebug | PASS | BUILD SUCCESSFUL (3s, 46 task) |
| lintDebug | PASS | Lint error yok (yalnizca warning) |
| Unit Testler — SubscriptionViewModelTest | PASS | 13/13 gecti, failures=0, errors=0 |
| Unit Testler — SubscriptionRepositoryImplTest | PASS | 11/11 gecti, failures=0, errors=0 |
| verify-purchase/index.ts tier mapping | PASS | PRO_MONTHLY / PRO_YEARLY duzeltildi |
| Android tier parse uyumu | PASS | toSubscriptionStatus() ile tam eslesme |
| String Senkronizasyonu | PASS | 3 dosyada da mevcut (values/, values-tr/, values-en/) |
| stringResource LaunchedEffect kurali | PASS | 94-98. satirlarda pre-compute edilmis |
| activity null korumasi | PASS | setError("payment_unavailable") cagriliyor |
| domain/ Android import | UYARI | Pre-existing ihlal, T-PAY kapsami disinda |
| data/ Composable | PASS | Yok |
| @OptIn(ExperimentalMaterial3Api::class) | PASS | SubscriptionScreen basinda mevcut |
| @Preview | PASS | SubscriptionScreenPreview + SubscriptionScreenProPreview mevcut |
| DataModule binding | PASS | SubscriptionRepositoryImpl bagli |

---

## Blocker Dogrulama

### Blocker 1 — Tier Mapping (DUZELTILDI)

`supabase/functions/verify-purchase/index.ts` satir 137-141:
- `"horsegallop_pro_monthly"` → `"PRO_MONTHLY"` (duzeltildi)
- `"horsegallop_pro_yearly"` → `"PRO_YEARLY"` (duzeltildi)

Android `SubscriptionRepositoryImpl.toSubscriptionStatus()` satir 159-161:
- `"PRO_YEARLY"` → `SubscriptionTier.PRO_YEARLY`
- `"PRO_MONTHLY"` → `SubscriptionTier.PRO_MONTHLY`

Tier string zinciri artik tutarli: Edge Function yazar, Android parse eder.

### Blocker 2 — Unit Testler (DUZELTILDI)

JUnit XML sonuclari:
- `SubscriptionRepositoryImplTest`: tests=11, failures=0, errors=0
- `SubscriptionViewModelTest`: tests=13, failures=0, errors=0

Kapsanan senaryolar onaylandi:
- `startSubscriptionPurchase` basarili dogrulama → `purchaseSuccess` state
- `startSubscriptionPurchase` `verified=false` → tier FREE kalir
- `PurchaseState.Purchased` tetiklenince `startSubscriptionPurchaseUseCase` dogru arguman ile cagriliyor
- `PurchaseState.Error` → `purchase_failed` error state
- `restorePurchases` basarili/basarisiz senaryo
- `refreshEntitlements` exception ve null backend donusu

---

## Uyarilar (FAIL degil)

1. `domain/auth/repository/ProfileRepository.kt:3` ve `domain/auth/usecase/UpdateProfileImageUseCase.kt:3` — `import android.net.Uri` — domain katmaninda pre-existing Android import. T-PAY kapsami disinda, ayri task ile takip edilmeli.

2. `GOOGLE_PLAY_SERVICE_ACCOUNT_KEY` Supabase secret — deploy oncesi Supabase Dashboard'a eklenmeli. Operational gereksinim, kod kalitesi sorunu degil.

3. `restorePurchases` hala client-trust ile calisiyor — MVP kapsam disi olarak belgelenmis.

---

## Degistirilen Dosyalar (onaylanan)

- `/supabase/functions/verify-purchase/index.ts`
- `/app/src/main/java/com/horsegallop/data/billing/BillingManager.kt`
- `/app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt`
- `/app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDtos.kt`
- `/app/src/main/java/com/horsegallop/data/subscription/repository/SubscriptionRepositoryImpl.kt`
- `/app/src/main/java/com/horsegallop/domain/subscription/repository/SubscriptionRepository.kt`
- `/app/src/main/java/com/horsegallop/domain/subscription/usecase/StartSubscriptionPurchaseUseCase.kt`
- `/app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionScreen.kt`
- `/app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionViewModel.kt`
- `/app/src/main/res/values/strings_core.xml`
- `/app/src/main/res/values-tr/strings.xml`
- `/app/src/main/res/values-en/strings.xml`
- `/app/src/test/java/com/horsegallop/data/subscription/repository/SubscriptionRepositoryImplTest.kt`
- `/app/src/test/java/com/horsegallop/feature/subscription/presentation/SubscriptionViewModelTest.kt`
