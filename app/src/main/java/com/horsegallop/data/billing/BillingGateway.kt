package com.horsegallop.data.billing

import com.android.billingclient.api.ProductDetails

data class BillingPurchaseResult(
    val success: Boolean,
    val message: String? = null
)

interface BillingGateway {
    suspend fun queryProductDetails(productIds: List<String>): Result<List<ProductDetails>>
    suspend fun launchSubscriptionPurchase(productId: String): BillingPurchaseResult
    suspend fun hasActiveSubscription(): Result<Boolean>
}
