package com.horsegallop.domain.subscription.usecase

import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveSubscriptionStatusUseCaseTest {

    @Test
    fun `observe returns latest repository status`() = runTest {
        val flow = MutableStateFlow(SubscriptionStatus(tier = SubscriptionTier.FREE, isActive = false))
        val repository = object : SubscriptionRepository {
            override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = flow
            override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> = Result.success(flow.value)
            override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> = Result.success(Unit)
            override suspend fun refreshEntitlements(): Result<SubscriptionStatus> = Result.success(flow.value)
        }
        val useCase = ObserveSubscriptionStatusUseCase(repository)

        flow.value = SubscriptionStatus(tier = SubscriptionTier.PRO_MONTHLY, isActive = true)

        assertEquals(SubscriptionTier.PRO_MONTHLY, useCase().first().tier)
    }
}
