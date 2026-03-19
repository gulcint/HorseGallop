# T-PAY Android-Feature Artifact

Tarih: 2026-03-19
Agent: android-feature

---

## Ozet

`purchaseToken` akisi `BillingManager.PurchaseState.Purchased` → `SubscriptionRepositoryImpl` → `SupabaseDataSource.verifyPurchase()` zinciri boyunca tam olarak kuruldu. `startSubscriptionPurchase` artik gercek sunucu dogrulamasi yapiyor, `SubscriptionViewModel` sonucu await ediyor ve hata UI'a yansitiliyor.

---

## Degistirilen Dosyalar

### 1. `app/src/main/java/com/horsegallop/data/billing/BillingManager.kt`

- `PurchaseState.Purchased(val productId: String)` → `PurchaseState.Purchased(val productId: String, val purchaseToken: String)`
- `handlePurchase` icinde her iki emit noktasina (`acknowledgePurchase` callback ve else dali) `purchase.purchaseToken` eklendi

### 2. `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDtos.kt`

Yeni DTO eklendi:

```kotlin
@Serializable
data class VerifyPurchaseResponseDto(
    val verified: Boolean = false,
    val tier: String = "FREE",
    @SerialName("expires_at") val expiresAt: String? = null
)
```

### 3. `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt`

Yeni metod eklendi — `ai-coach` Edge Function pattern'i kullanildi:

```kotlin
suspend fun verifyPurchase(purchaseToken: String, productId: String): Result<VerifyPurchaseResponseDto>
```

`currentUserId()` null ise erken `Result.failure` donuyor. `verify-purchase` Edge Function'ini `buildJsonObject` ile cagiriyor.

### 4. `app/src/main/java/com/horsegallop/domain/subscription/repository/SubscriptionRepository.kt`

```kotlin
suspend fun startSubscriptionPurchase(productId: String, purchaseToken: String): Result<Unit>
```

### 5. `app/src/main/java/com/horsegallop/domain/subscription/usecase/StartSubscriptionPurchaseUseCase.kt`

```kotlin
suspend operator fun invoke(productId: String, purchaseToken: String): Result<Unit>
```

### 6. `app/src/main/java/com/horsegallop/data/subscription/repository/SubscriptionRepositoryImpl.kt`

- `startSubscriptionPurchase(productId, purchaseToken)` artik `supabaseDataSource.verifyPurchase(purchaseToken, productId)` cagiriyor. Dogrulanirsa backend'den yetkili state yenileniyor.
- `observeBillingPurchases()` icinde noop `updateUserProfile` kaldirild. Artik `verifyPurchase` cagiriyor; basarili olursa `getSubscriptionStatus()` ile state yenileniyor, basarisiz olursa log atiliyor.

### 7. `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionViewModel.kt`

- `PurchaseState.Purchased` handler'i fire-and-forget'ten `viewModelScope.launch` + await'e donusturuldu.
- `.onSuccess` → `isPurchasing = false, purchaseSuccess = true`
- `.onFailure` → `isPurchasing = false, error = "purchase_verification_failed"`
- Yeni `setError(errorKey: String)` metodu eklendi (activity null guard icin kullaniliyor).

### 8. `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionScreen.kt`

- `onPurchase` lambda'si activity null kontrolune alindi:
  ```kotlin
  val act = activity
  if (act != null) viewModel.purchase(act)
  else viewModel.setError("payment_unavailable")
  ```
- `msgVerificationFailed` ve `msgPaymentUnavailable` string'leri `LaunchedEffect` disinda hesaplandi.
- `LaunchedEffect(ui.error)` icine `"purchase_verification_failed"` ve `"payment_unavailable"` case'leri eklendi.

### 9. String kaynaklari (3 dosya)

`values/strings_core.xml`, `values-tr/strings.xml`, `values-en/strings.xml` dosyalarina eklendi:
- `subscription_verification_failed`
- `subscription_payment_unavailable`

Ayrica bu dosyalarda bulunan pre-existing git merge conflict marker'lari temizlendi (konu disi ama build engelleyiciydi).

---

## Build ve Test Sonuclari

- `./gradlew :app:compileDebugKotlin` — BASARILI
- `./gradlew :app:kaptDebugKotlin` — BASARILI
- `./gradlew :app:hiltJavaCompileDebug` — BASARILI
- `./gradlew testDebugUnitTest` — BASARILI (tum mevcut unit testler gecti)
- `./gradlew assembleDebug` — Kotlin/Hilt derleme BASARILI; `mergeExtDexDebug` adiminda pre-existing DexArchiveMergerException olustu (bu hata T-PAY degisiklikleri oncesinde de mevcut, Supabase migration/branch durumundan kaynakli).

---

## Acik Maddeler (Kapsam Disi)

1. `supabase/functions/verify-purchase/index.ts` — operator agent tarafindan olusturulacak.
2. `GOOGLE_PLAY_SERVICE_ACCOUNT_KEY` Supabase secret — deploy oncesi eklenmeli.
3. `restorePurchases` hala client-trust ile calisiyor — MVP kapsam disi.
4. `mergeExtDexDebug` DexMerger hatasi — pre-existing, T-PAY kapsam disi.
