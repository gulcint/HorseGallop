# T-PAY Researcher Artifact

Tarih: 2026-03-19
Agent: researcher

---

## 1. Problem Ozeti

Google Play Billing altyapisi calisiyor; eksik olan halka server-side purchase token dogrulamasidir.
`startSubscriptionPurchase` tamamen noop, `restorePurchases` ise Play Store sorgusunu yapiyor ancak sonucu
yalnizca yerel state ve direkt `updateUserProfile` ile Supabase'e yazarak dogruluyor — ortada
Google Play Developer API dogrulamasi yok. Guvensiz bir "client trust" akisi mevcut.

---

## 2. Mevcut Repo Gercegi

### BillingManager
Dosya: `app/src/main/java/com/horsegallop/data/billing/BillingManager.kt`

- Satir 36-43: `@Singleton`, `ApplicationContext` kullanir — Activity bagimliligi yok.
- Satir 86: `launchBillingFlow(activity: Activity, productId: String)` — Activity parametreyi disaridan alir, Activity'ye inject edilmez.
- Satir 45-58: `PurchasesUpdatedListener` sinif icinde inline tanimlanmis. Satir 163-164: `handlePurchase` onaylaninca `_purchaseState.value = PurchaseState.Purchased(productId)` atar; ancak `purchase.purchaseToken` bu noktada KULLANILMAZ.
- Kritik eksik: `PurchaseState.Purchased` sadece `productId: String` tasir. `purchaseToken` tasimaz.
  Builder'in bunu degistirmesi veya `BillingManager`'da token'i ayri bir Flow'da expose etmesi gerekir.
- Satir 140-151: `queryActivePurchases()` suspend fonksiyon, `Purchase` listesi doner; `purchase.purchaseToken` burada erisebilir durumda.

### SubscriptionViewModel
Dosya: `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionViewModel.kt`

- Satir 59-75: `billingManager.purchaseState` collect edilir. `PurchaseState.Purchased` gelince satir 63'de `startSubscriptionPurchaseUseCase(state.productId)` cagrisi yapilir. `purchaseToken` buraya ulasmiyor.
- Satir 82-89: `purchase(activity: Activity)` — Activity ViewMode'e inject edilmez, Screen'den parametre olarak gecer. Dogru pattern.
- `startSubscriptionPurchaseUseCase` donus tipi: `Result<Unit>` await edilmeden atesilir (fire-and-forget). Hata dondurse bile UI'a yansimaz — builder bu akisi duzelttmeli.

### SubscriptionRepositoryImpl
Dosya: `app/src/main/java/com/horsegallop/data/subscription/repository/SubscriptionRepositoryImpl.kt`

- Satir 48-50: `startSubscriptionPurchase(productId: String)` tamamen noop, `Result.success(Unit)` doner. purchaseToken parametresi yok.
- Satir 61-87: `restorePurchases()` — `billingManager.queryActivePurchases()` ile aktif satin alimlari alir, ardindan satir 71-77'de `supabaseDataSource.updateUserProfile(mapOf("is_pro" to true, ...))` ile direkt yazar. Server-side dogrulama yok.
- Satir 89-109: `observeBillingPurchases()` da ayni sekilde direkt `updateUserProfile` cagriyor. Guvensiz client trust.
- Satir 142-158: `SupabaseSubscriptionDto.toSubscriptionStatus()` extension fonksiyonu dosya altinda tanimlanmis, `subscriptionTier` ve `isPro` alanlari kullaniliyor.

### SubscriptionScreen Activity Referansi
Dosya: `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionScreen.kt`

- Satir 92: `val activity = LocalContext.current as? Activity` — nullable Activity. Dogru Compose pattern.
- Satir 139: `onPurchase = { activity?.let { viewModel.purchase(it) } }` — Activity null ise satin alma sessizce atlanir. Bu bir risk; null durumunda kullaniciya hata mesaji gosterilmeli.
- `@OptIn(ExperimentalMaterial3Api::class)` eklenmis (satir 85). SemanticColors kullaniliyor (satir 119, 155). Mevcut Screen'de degisiklik minimumda tutulabilir.

### SupabaseDataSource
Dosya: `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDataSource.kt`

- Satir 381-390: `getSubscriptionStatus()` — `user_profiles` tablosundan `is_pro`, `subscription_tier`, `subscription_expires_at` alanlarini okur. Mevcut ve calisir durumda.
- Satir 73-77: `updateUserProfile(updates: Map<String, Any?>)` — su an restorePurchases ve observeBillingPurchases tarafindan client trust ile kullaniliyor.
- Satir 462: `supabase.functions.invoke(function = "ai-coach", body = body)` — Edge Function cagri pattern'i. `verify-purchase` icin ayni pattern kullanilmali.
- `verifyPurchase` metodu MEVCUT DEGIL. Builder tarafindan eklenmesi gerekiyor.

### SupabaseDtos
Dosya: `app/src/main/java/com/horsegallop/data/remote/supabase/SupabaseDtos.kt`

- Satir 10-26: `SupabaseUserProfileDto` — `is_pro` (satir 21), `subscription_tier` (satir 22), `subscription_expires_at` (satir 23) alanlari MEVCUT.
- Satir 298-302: `SupabaseSubscriptionDto` — `is_pro`, `subscription_tier`, `subscription_expires_at` alanlari mevcut. Bu DTO `getSubscriptionStatus()` donusu icin kullaniliyor.
- `VerifyPurchaseResponseDto` MEVCUT DEGIL. Builder tarafindan eklenmesi gerekiyor.

---

## 3. Dis Kaynaklar ve Kontratlar

### ai-coach Edge Function Pattern
Dosya: `supabase/functions/ai-coach/index.ts`

Tam calisir bir Edge Function pattern'i sunuyor:
- Deno `serve()` wrapper
- CORS headers (`corsHeaders` objesi, OPTIONS preflight handler)
- `req.json()` ile body parse
- Disaridan API cagirma (Groq)
- `SUPABASE_URL` + `SUPABASE_SERVICE_ROLE_KEY` ile Supabase client olusturma
- Supabase tablosuna yazma
- JSON response: `{ reply, success: true }` veya `{ error, success: false }`

`verify-purchase` fonksiyonu ayni scaffold'u kullanmali.

### verify-purchase Edge Function Gereksinimleri

Beklenen input: `{ purchaseToken: string, productId: string, userId: string }`
Yapilacaklar:
1. `GOOGLE_PLAY_SERVICE_ACCOUNT_KEY` secret'tan service account JSON'ini al
2. Google Play Developer API'ye JWT ile authenticate ol
3. `purchases.subscriptions.get(packageName, subscriptionId, purchaseToken)` cagir
4. `paymentState == 1` (odeme alindi) ve `expiryTimeMillis` gelecek mi kontrol et
5. Dogrulanirsa Supabase'de `user_profiles` tablosunu `is_pro=true`, `subscription_tier`, `subscription_expires_at` ile guncelle
6. Response: `{ verified: boolean, tier: string, expiresAt: string | null }`

Google Play Developer API endpoint:
`GET https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{packageName}/purchases/subscriptions/{subscriptionId}/tokens/{token}`

Package name: `com.horsegallop`

### Supabase Edge Function Deploy Komutu
`supabase functions deploy verify-purchase --project-ref <ref>`

Secret set: `supabase secrets set GOOGLE_PLAY_SERVICE_ACCOUNT_KEY='...' --project-ref <ref>`

---

## 4. Riskler ve Bilinmeyenler

1. **purchaseToken BillingManager'dan akmıyor**: `PurchaseState.Purchased(productId)` sadece productId tasir. Token'i akista tasimak icin ya `PurchaseState.Purchased` data class'ina `purchaseToken: String` alani eklenmeli ya da `BillingManager`'da ayri bir `StateFlow<String?>` expose edilmeli. Bu degisiklik `BillingManager`, `SubscriptionViewModel` ve `SubscriptionRepositoryImpl`'i etkiler.

2. **activity null durumu sessiz**: `SubscriptionScreen` satir 139'da `activity?.let` null guard var. Activity null ise hicbir sey olmaz, kullaniciya bilgi verilmez. Minimal fix: null ise `viewModel.setError("activity_unavailable")` cagrisi.

3. **startSubscriptionPurchaseUseCase fire-and-forget**: `ViewModel` satir 63'te `await` etmeden cagriliyor. Dogrulama hatasi UI'a yansimaz. `viewModelScope.launch` ile await yapilmali ve sonuca gore state guncellenmeli.

4. **GOOGLE_PLAY_SERVICE_ACCOUNT_KEY**: Supabase secrets'ta henuz set edilmedi. Deploy oncesi mutlaka eklenmeli. Google Cloud Console'dan service account key JSON dosyasi alinmasi gerekiyor.

5. **restorePurchases guvensiz**: Simdilik client trust ile calisiyor. Ideal olan restore akininda da `verifyPurchase` cagirmak, ancak brief MVP kapsaminda bu istege bagli.

6. **Supabase functions dizini**: Sadece `ai-coach` var. `verify-purchase` yeni dizin olarak olusturulacak: `supabase/functions/verify-purchase/index.ts`.

7. **`supabase/functions/` altinda `_shared/` yok**: ai-coach CORS headers'i inline tanimliyor. `verify-purchase` de ayni sekilde kendi CORS headers'ini tanimlamali.

---

## 5. Builder ve Operator Icin Net Girdiler

### Operator (verify-purchase Edge Function)
- Yeni dosya olustur: `/Users/gulcintas/HorseGallopProject/horsegallop/supabase/functions/verify-purchase/index.ts`
- ai-coach scaffold'unu baz al (CORS, serve, req.json, Supabase client pattern)
- Input: `{ purchaseToken: string, productId: string, userId: string }`
- Google Play Developer API JWT auth: `googleapis` npm paketi Deno'da kullanilabilir veya manuel JWT imzalama
- Dogrulama sonrasi `user_profiles` guncellemesi servis rolundan yapilmali
- Response: `{ verified: boolean, tier: string, expiresAt: string | null }`
- Secret: `GOOGLE_PLAY_SERVICE_ACCOUNT_KEY` (string formatinda JSON)
- `SUPABASE_URL` ve `SUPABASE_SERVICE_ROLE_KEY` otomatik inject edilir

### Android-Feature Builder
Degisiklik 1 — `BillingManager.kt` satir 33:
`data class Purchased(val productId: String)` →
`data class Purchased(val productId: String, val purchaseToken: String)`
Satir 163-164 ve 169'da token parametresi eklenmeli.

Degisiklik 2 — `SupabaseDtos.kt` (yeni DTO):
```kotlin
@Serializable
data class VerifyPurchaseResponseDto(
    val verified: Boolean = false,
    val tier: String = "FREE",
    @SerialName("expires_at") val expiresAt: String? = null
)
```

Degisiklik 3 — `SupabaseDataSource.kt` (yeni metod):
```kotlin
suspend fun verifyPurchase(purchaseToken: String, productId: String): VerifyPurchaseResponseDto {
    val userId = currentUserId() ?: return VerifyPurchaseResponseDto()
    val body = buildJsonObject {
        put("purchaseToken", purchaseToken)
        put("productId", productId)
        put("userId", userId)
    }
    val response = supabase.functions.invoke(function = "verify-purchase", body = body)
    return Json.decodeFromString<VerifyPurchaseResponseDto>(response.bodyAsText())
}
```

Degisiklik 4 — `SubscriptionRepositoryImpl.kt`:
- `startSubscriptionPurchase(productId: String)` -> imzasi `startSubscriptionPurchase(productId: String, purchaseToken: String)` olmali ya da purchaseToken ayri parametre olarak domain interface'e eklenmeli
- `observeBillingPurchases()` icinde `PurchaseState.Purchased(productId, purchaseToken)` gelince `verifyPurchase` cagrisi yapilmali; basarili donerse `updateUserProfile` ile state yazilmali

Degisiklik 5 — `SubscriptionViewModel.kt` satir 63:
```kotlin
is PurchaseState.Purchased -> {
    viewModelScope.launch {
        startSubscriptionPurchaseUseCase(state.productId, state.purchaseToken)
            .onFailure { _ui.update { it.copy(isPurchasing = false, error = "purchase_failed") } }
            .onSuccess { _ui.update { it.copy(isPurchasing = false, purchaseSuccess = true) } }
    }
}
```

Degisiklik 6 — `SubscriptionScreen.kt` satir 139:
```kotlin
onPurchase = {
    val act = activity
    if (act != null) viewModel.purchase(act)
    else viewModel.setError("activity_unavailable")
}
```
`setError` metodu ViewModel'e eklenmeli ya da mevcut `clearError`/`error` mekanizmasi kullanilmali.

### Domain Interface Etkisi
`domain/subscription/repository/SubscriptionRepository.kt` — `startSubscriptionPurchase` imzasi degisecek.
`domain/subscription/usecase/StartSubscriptionPurchaseUseCase.kt` — parametre eklenmesi gerekiyor.
Bu dosyalar builder tarafindan da guncellenmeli.
