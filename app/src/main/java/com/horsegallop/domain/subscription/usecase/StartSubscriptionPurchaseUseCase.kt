package com.horsegallop.domain.subscription.usecase

import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import javax.inject.Inject

class StartSubscriptionPurchaseUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(productId: String): Result<Unit> = repository.startSubscriptionPurchase(productId)
}
