package com.horsegallop.data.subscription.repository

import com.android.billingclient.api.Purchase
import com.horsegallop.core.debug.AppLog
import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.data.remote.dto.VerifyPurchaseFunctionsDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
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
    private val billingManager: BillingManager,
    private val functionsDataSource: AppFunctionsDataSource
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
        refreshFromBackend()
    }

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = statusState.asStateFlow()

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> =
        Result.success(statusState.value)

    override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> = runCatching {
        val dto = functionsDataSource.getSubscriptionStatus()
        val status = dto.toSubscriptionStatus()
        statusState.value = status
        status
    }.onFailure {
        AppLog.e("SubscriptionRepo", "refreshEntitlements failed: ${it.message}")
    }

    override suspend fun restorePurchases(): Result<SubscriptionStatus> = runCatching {
        val purchases = billingManager.queryActivePurchases()
        val activePurchase = purchases.firstOrNull()
        if (activePurchase != null) {
            verifyAndUpdateFromPurchase(activePurchase)
        } else {
            // Backend'den son durumu çek — Play Store'da aktif purchase yoksa FREE
            val dto = functionsDataSource.getSubscriptionStatus()
            statusState.value = dto.toSubscriptionStatus()
        }
        statusState.value
    }.onFailure {
        AppLog.e("SubscriptionRepo", "restorePurchases failed: ${it.message}")
    }

    private fun observeBillingPurchases() {
        scope.launch {
            billingManager.purchaseState.collect { state ->
                if (state is PurchaseState.Purchased) {
                    // purchaseToken'ı backend'e gönder, Firestore'da isPro set et
                    val purchases = billingManager.queryActivePurchases()
                    val purchase = purchases.firstOrNull { p ->
                        p.products.contains(state.productId)
                    }
                    if (purchase != null) {
                        verifyAndUpdateFromPurchase(purchase)
                    } else {
                        // Token erişilemiyor ama purchase acknowledge edildi — local güncelle
                        updateStatusLocalFromProductId(state.productId)
                    }
                }
            }
        }
    }

    private fun refreshFromBackend() {
        scope.launch {
            runCatching {
                val dto = functionsDataSource.getSubscriptionStatus()
                statusState.value = dto.toSubscriptionStatus()
            }.onFailure {
                AppLog.w("SubscriptionRepo", "Backend status fetch failed, using local: ${it.message}")
            }
        }
    }

    private suspend fun verifyAndUpdateFromPurchase(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        runCatching {
            val dto = functionsDataSource.verifyPurchase(
                purchaseToken = purchase.purchaseToken,
                productId = productId
            )
            if (dto.success) {
                statusState.value = dto.toSubscriptionStatus()
                AppLog.i("SubscriptionRepo", "Purchase verified via backend: isPro=${dto.isPro}, tier=${dto.tier}")
            } else {
                updateStatusLocalFromProductId(productId)
            }
        }.onFailure {
            AppLog.e("SubscriptionRepo", "Backend purchase verification failed: ${it.message}")
            // Fallback: local update (backend'e ulaşılamadı)
            updateStatusLocalFromProductId(productId)
        }
    }

    private fun updateStatusLocalFromProductId(productId: String) {
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

private fun VerifyPurchaseFunctionsDto.toSubscriptionStatus(): SubscriptionStatus {
    val tier = when (this.tier) {
        "PRO_YEARLY" -> SubscriptionTier.PRO_YEARLY
        "PRO_MONTHLY" -> SubscriptionTier.PRO_MONTHLY
        else -> SubscriptionTier.FREE
    }
    return SubscriptionStatus(
        tier = tier,
        isActive = this.isPro,
        expiresAtEpochMillis = this.expiresAt
    )
}
