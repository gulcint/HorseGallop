package com.horsegallop.data.subscription.repository

import android.content.SharedPreferences
import com.horsegallop.data.billing.BillingGateway
import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences,
    private val billingGateway: BillingGateway
) : SubscriptionRepository {

    private val statusFlow = MutableStateFlow(readStatus())

    override fun observeSubscriptionStatus() = statusFlow.asStateFlow()

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> = Result.success(statusFlow.value)

    override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> {
        val launchResult = billingGateway.launchSubscriptionPurchase(productId)
        if (!launchResult.success) {
            return Result.failure(IllegalStateException(launchResult.message ?: "purchase_launch_failed"))
        }

        val entitlement = billingGateway.hasActiveSubscription().getOrElse { false }
        if (entitlement) {
            val tier = if (productId.contains("year", ignoreCase = true)) {
                SubscriptionTier.PRO_YEARLY
            } else {
                SubscriptionTier.PRO_MONTHLY
            }
            val updated = SubscriptionStatus(tier = tier, isActive = true)
            persistStatus(updated)
            statusFlow.value = updated
            return Result.success(Unit)
        }

        return Result.failure(IllegalStateException("purchase_pending_or_not_confirmed"))
    }

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> {
        val active = billingGateway.hasActiveSubscription().getOrElse { false }
        val next = if (active) {
            statusFlow.value.takeIf { it.tier != SubscriptionTier.FREE } ?: SubscriptionStatus(
                tier = SubscriptionTier.PRO_MONTHLY,
                isActive = true
            )
        } else {
            SubscriptionStatus(tier = SubscriptionTier.FREE, isActive = false)
        }
        persistStatus(next)
        statusFlow.update { next }
        return Result.success(next)
    }

    private fun readStatus(): SubscriptionStatus {
        val tierId = prefs.getString(KEY_TIER, SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
        val tier = runCatching { SubscriptionTier.valueOf(tierId) }.getOrDefault(SubscriptionTier.FREE)
        val isActive = prefs.getBoolean(KEY_ACTIVE, false)
        return if (tier == SubscriptionTier.FREE) {
            SubscriptionStatus(tier = SubscriptionTier.FREE, isActive = false)
        } else {
            SubscriptionStatus(tier = tier, isActive = isActive)
        }
    }

    private fun persistStatus(status: SubscriptionStatus) {
        prefs.edit()
            .putString(KEY_TIER, status.tier.name)
            .putBoolean(KEY_ACTIVE, status.isActive)
            .apply()
    }

    companion object {
        private const val KEY_TIER = "subscription.tier"
        private const val KEY_ACTIVE = "subscription.active"
    }
}
