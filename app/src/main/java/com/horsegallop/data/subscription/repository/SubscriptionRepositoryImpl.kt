package com.horsegallop.data.subscription.repository

import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SubscriptionRepositoryImpl @Inject constructor() : SubscriptionRepository {

    private val statusState = MutableStateFlow(
        SubscriptionStatus(
            tier = SubscriptionTier.FREE,
            isActive = true,
            expiresAtEpochMillis = null
        )
    )

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = statusState.asStateFlow()

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> = Result.success(statusState.value)

    override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> {
        val normalized = productId.trim().lowercase()
        val tier = when {
            normalized.contains("year") -> SubscriptionTier.PRO_YEARLY
            normalized.contains("month") -> SubscriptionTier.PRO_MONTHLY
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
        return Result.success(Unit)
    }

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> = Result.success(statusState.value)
}

