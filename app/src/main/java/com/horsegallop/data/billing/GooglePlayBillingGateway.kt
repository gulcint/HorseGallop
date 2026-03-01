package com.horsegallop.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.Purchase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class GooglePlayBillingGateway @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingGateway {

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { _, _ -> }
            .build()
    }

    private var hostActivity: Activity? = null

    fun bindHostActivity(activity: Activity?) {
        hostActivity = activity
    }

    override suspend fun queryProductDetails(productIds: List<String>): Result<List<ProductDetails>> {
        return withContext(Dispatchers.IO) {
            if (productIds.isEmpty()) return@withContext Result.success(emptyList())
            connectIfNeeded().fold(
                onSuccess = {
                    val products = productIds.map { id ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(id)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(products)
                        .build()
                    suspendCancellableCoroutine<Result<List<ProductDetails>>> { cont ->
                        billingClient.queryProductDetailsAsync(params) { result, details ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                cont.resume(Result.success(details))
                            } else {
                                cont.resume(Result.failure(IllegalStateException(result.debugMessage)))
                            }
                        }
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }
    }

    override suspend fun launchSubscriptionPurchase(productId: String): BillingPurchaseResult {
        val activity = hostActivity
            ?: return BillingPurchaseResult(success = false, message = "activity_not_available")
        val detailsResult = queryProductDetails(listOf(productId))
        val details = detailsResult.getOrElse {
            return BillingPurchaseResult(success = false, message = it.message)
        }.firstOrNull() ?: return BillingPurchaseResult(success = false, message = "product_not_found")

        val offerToken = details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
            ?: return BillingPurchaseResult(success = false, message = "offer_not_found")

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val billingResult = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams)).build()
        )

        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Play purchase UI launched successfully; entitlement will be detected on refresh.
            BillingPurchaseResult(success = true)
        } else {
            BillingPurchaseResult(success = false, message = billingResult.debugMessage)
        }
    }

    override suspend fun hasActiveSubscription(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            connectIfNeeded().fold(
                onSuccess = {
                    suspendCancellableCoroutine<Result<Boolean>> { cont ->
                        val params = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                        billingClient.queryPurchasesAsync(params) { result, purchases ->
                            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                                cont.resume(Result.failure(IllegalStateException(result.debugMessage)))
                                return@queryPurchasesAsync
                            }
                            val active = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                            purchases
                                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                                .forEach { acknowledgePurchase(it) }
                            cont.resume(Result.success(active))
                        }
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { _: BillingResult -> }
    }

    private suspend fun connectIfNeeded(): Result<Unit> {
        if (billingClient.isReady) return Result.success(Unit)
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        cont.resume(Result.success(Unit))
                    } else {
                        cont.resume(Result.failure(IllegalStateException(result.debugMessage)))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) {
                        cont.resume(Result.failure(IllegalStateException("billing_service_disconnected")))
                    }
                }
            })
        }
    }
}
