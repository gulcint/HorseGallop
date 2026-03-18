package com.horsegallop.data.subscription.repository

import android.content.Context
import com.horsegallop.core.debug.AppLog
import com.horsegallop.data.billing.BillingManager
import com.horsegallop.data.billing.PurchaseState
import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val supabaseDataSource: SupabaseDataSource
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

    override suspend fun startSubscriptionPurchase(productId: String, purchaseToken: String): Result<Unit> {
        return supabaseDataSource.verifyPurchase(purchaseToken, productId).map { dto ->
            if (dto.verified) {
                updateStatusLocalFromProductId(productId)
                // Refresh authoritative state from backend
                runCatching {
                    val backendDto = supabaseDataSource.getSubscriptionStatus()
                    if (backendDto != null) statusState.value = backendDto.toSubscriptionStatus()
                }
            }
        }
    }

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> = runCatching {
        val dto = supabaseDataSource.getSubscriptionStatus()
        val status = dto?.toSubscriptionStatus() ?: statusState.value
        statusState.value = status
        status
    }.onFailure {
        AppLog.e("SubscriptionRepo", "refreshEntitlements failed: ${it.message}")
    }

    override suspend fun restorePurchases(): Result<SubscriptionStatus> = runCatching {
        val purchases = billingManager.queryActivePurchases()
        val activePurchase = purchases.firstOrNull()
        if (activePurchase != null) {
            val productId = activePurchase.products.firstOrNull() ?: ""
            updateStatusLocalFromProductId(productId)
            // Also write isPro flag to Supabase user_profiles
            val uid = supabaseDataSource.currentUserId()
            if (uid != null) {
                runCatching {
                    supabaseDataSource.updateUserProfile(
                        mapOf(
                            "is_pro" to true,
                            "subscription_tier" to if (productId.contains("year")) "PRO_YEARLY" else "PRO_MONTHLY"
                        )
                    )
                }
            }
        } else {
            // Fetch latest status from Supabase
            val dto = supabaseDataSource.getSubscriptionStatus()
            if (dto != null) statusState.value = dto.toSubscriptionStatus()
        }
        statusState.value
    }.onFailure {
        AppLog.e("SubscriptionRepo", "restorePurchases failed: ${it.message}")
    }

    private fun observeBillingPurchases() {
        scope.launch {
            billingManager.purchaseState.collect { state ->
                if (state is PurchaseState.Purchased) {
                    // Optimistically update local state
                    updateStatusLocalFromProductId(state.productId)
                    // Server-side verification via Edge Function
                    supabaseDataSource.verifyPurchase(state.purchaseToken, state.productId)
                        .onSuccess { dto ->
                            if (dto.verified) {
                                AppLog.i("SubscriptionRepo", "Purchase verified: ${state.productId}")
                                // Refresh authoritative state from backend
                                runCatching {
                                    val backendDto = supabaseDataSource.getSubscriptionStatus()
                                    if (backendDto != null) statusState.value = backendDto.toSubscriptionStatus()
                                }
                            } else {
                                AppLog.w("SubscriptionRepo", "Purchase NOT verified by server: ${state.productId}")
                            }
                        }
                        .onFailure {
                            AppLog.e("SubscriptionRepo", "verifyPurchase failed: ${it.message}")
                        }
                }
            }
        }
    }

    private fun refreshFromBackend() {
        scope.launch {
            runCatching {
                val dto = supabaseDataSource.getSubscriptionStatus()
                if (dto != null) statusState.value = dto.toSubscriptionStatus()
            }.onFailure {
                AppLog.w("SubscriptionRepo", "Backend status fetch failed, using local: ${it.message}")
            }
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

private fun com.horsegallop.data.remote.supabase.SupabaseSubscriptionDto.toSubscriptionStatus(): SubscriptionStatus {
    val tier = when (subscriptionTier) {
        "PRO_YEARLY" -> SubscriptionTier.PRO_YEARLY
        "PRO_MONTHLY" -> SubscriptionTier.PRO_MONTHLY
        else -> SubscriptionTier.FREE
    }
    val expiresAt = subscriptionExpiresAt?.let { iso ->
        runCatching {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(iso)?.time
        }.getOrNull()
    }
    return SubscriptionStatus(
        tier = tier,
        isActive = isPro,
        expiresAtEpochMillis = expiresAt
    )
}
