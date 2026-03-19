package com.horsegallop.domain.subscription.repository

import com.horsegallop.domain.subscription.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>
    suspend fun getSubscriptionStatus(): Result<SubscriptionStatus>
    suspend fun startSubscriptionPurchase(productId: String, purchaseToken: String): Result<Unit>
    suspend fun refreshEntitlements(): Result<SubscriptionStatus>
    suspend fun restorePurchases(): Result<SubscriptionStatus>
}
