package com.horsegallop.domain.subscription.usecase

import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import javax.inject.Inject

class RestorePurchasesUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): Result<SubscriptionStatus> = repository.restorePurchases()
}
