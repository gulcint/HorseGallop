package com.horsegallop.domain.subscription.model

enum class SubscriptionTier {
    FREE,
    PRO_MONTHLY,
    PRO_YEARLY
}

data class SubscriptionStatus(
    val tier: SubscriptionTier,
    val isActive: Boolean,
    val expiresAtEpochMillis: Long? = null
)

fun SubscriptionStatus.isPro(): Boolean = isActive && tier != SubscriptionTier.FREE
