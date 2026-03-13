package com.horsegallop.data.subscription.repository

import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager
) : SubscriptionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val statusState = MutableStateFlow(
        SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = true,
            expiresAtEpochMillis = null
        )
    )

    init {
        observeBillingPurchases()
    }

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = statusState.asStateFlow()

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> =
        Result.success(statusState.value)

    override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> {
        updateStatusFromProductId(productId)
        return Result.success(Unit)
    }

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> =
        Result.success(statusState.value)

    override suspend fun restorePurchases(): Result<SubscriptionStatus> = runCatching {
        val purchases = billingManager.queryActivePurchases()
        val activePurchase = purchases.firstOrNull()
        if (activePurchase != null) {
            val productId = activePurchase.products.firstOrNull() ?: ""
            updateStatusFromProductId(productId)
        } else {
            statusState.value = SubscriptionStatus(
                tier = SubscriptionTier.FREE,
                isActive = true,
                expiresAtEpochMillis = null
            )
        }
        statusState.value
    }

    private fun observeBillingPurchases() {
        scope.launch {
            billingManager.purchaseState.collect { state ->
                if (state is PurchaseState.Purchased) {
                    updateStatusFromProductId(state.productId)
                }
            }
        }
    }

    private fun updateStatusFromProductId(productId: String) {
        val tier = when {
            productId.contains("year") -> SubscriptionTier.PRO_YEARLY
            productId.contains("month") -> SubscriptionTier.PRO_MONTHLY
            else -> SubscriptionTier.PRO_MONTHLY
        }
        val now = System.currentTimeMillis()
        val expiresAt = when (tier) {
            SubscriptionTier.PRO_YEARLY -> now + 365L * 24L * 60L * 60L * 1000L
            SubscriptionTier.PRO_MONTHLY -> now + 30L * 24L * 60L * 60L * 1000L
            SubscriptionTier.FREE -> null
        }
        statusState.value = SubscriptionStatus(
            tier = tier,
            isActive = true,
            expiresAtEpochMillis = expiresAt
        )
    }
}
