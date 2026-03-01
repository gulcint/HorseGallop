package com.horsegallop.domain.subscription.usecase

import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSubscriptionStatusUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<SubscriptionStatus> = repository.observeSubscriptionStatus()
}
