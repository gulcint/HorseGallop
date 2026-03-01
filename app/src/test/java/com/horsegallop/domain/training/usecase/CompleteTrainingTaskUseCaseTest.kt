package com.horsegallop.domain.training.usecase

import com.horsegallop.domain.subscription.model.SubscriptionStatus
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.model.TrainingTask
import com.horsegallop.domain.training.model.TrainingTaskStatus
import com.horsegallop.domain.training.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteTrainingTaskUseCaseTest {

    @Test
    fun `free user cannot complete locked pro task`() = runTest {
        val trainingRepo = FakeTrainingRepository(
            plans = listOf(
                TrainingPlan(
                    id = "pro_plan",
                    title = "Pro",
                    summary = "Pro plan",
                    weeklyGoal = 4,
                    progressPercent = 0,
                    streakDays = 0,
                    status = TrainingPlanStatus.LOCKED,
                    tasks = listOf(
                        TrainingTask(
                            id = "task_1",
                            title = "Task",
                            description = "",
                            targetMinutes = 20,
                            status = TrainingTaskStatus.LOCKED
                        )
                    )
                )
            )
        )
        val subscriptionRepo = FakeSubscriptionRepository(
            SubscriptionStatus(SubscriptionTier.FREE, isActive = false)
        )
        val useCase = CompleteTrainingTaskUseCase(trainingRepo, subscriptionRepo)

        val result = useCase(planId = "pro_plan", taskId = "task_1")

        assertTrue(result.isFailure)
        assertFalse(trainingRepo.completeCalled)
    }

    @Test
    fun `free user can complete non locked plan task`() = runTest {
        val trainingRepo = FakeTrainingRepository(
            plans = listOf(
                TrainingPlan(
                    id = "free_plan",
                    title = "Free",
                    summary = "Free plan",
                    weeklyGoal = 2,
                    progressPercent = 0,
                    streakDays = 0,
                    status = TrainingPlanStatus.IN_PROGRESS,
                    tasks = listOf(
                        TrainingTask(
                            id = "task_1",
                            title = "Task",
                            description = "",
                            targetMinutes = 20,
                            status = TrainingTaskStatus.NOT_STARTED
                        )
                    )
                )
            )
        )
        val subscriptionRepo = FakeSubscriptionRepository(
            SubscriptionStatus(SubscriptionTier.FREE, isActive = false)
        )
        val useCase = CompleteTrainingTaskUseCase(trainingRepo, subscriptionRepo)

        val result = useCase(planId = "free_plan", taskId = "task_1")

        assertTrue(result.isSuccess)
        assertTrue(trainingRepo.completeCalled)
    }
}

private class FakeTrainingRepository(
    private val plans: List<TrainingPlan>
) : TrainingRepository {
    var completeCalled: Boolean = false

    override fun observeTrainingPlans(): Flow<List<TrainingPlan>> = MutableStateFlow(plans)

    override suspend fun getTrainingPlans(): Result<List<TrainingPlan>> = Result.success(plans)

    override suspend fun completeTrainingTask(planId: String, taskId: String): Result<Unit> {
        completeCalled = true
        return Result.success(Unit)
    }
}

private class FakeSubscriptionRepository(
    private val status: SubscriptionStatus
) : SubscriptionRepository {
    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> = MutableStateFlow(status)

    override suspend fun getSubscriptionStatus(): Result<SubscriptionStatus> = Result.success(status)

    override suspend fun startSubscriptionPurchase(productId: String): Result<Unit> = Result.success(Unit)

    override suspend fun refreshEntitlements(): Result<SubscriptionStatus> = Result.success(status)
}
