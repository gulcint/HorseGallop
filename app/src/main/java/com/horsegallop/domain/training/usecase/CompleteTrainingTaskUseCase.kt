package com.horsegallop.domain.training.usecase

import com.horsegallop.domain.subscription.repository.SubscriptionRepository
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.repository.TrainingRepository
import javax.inject.Inject

class CompleteTrainingTaskUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(planId: String, taskId: String): Result<Unit> {
        val statusResult = subscriptionRepository.getSubscriptionStatus()
        val status = statusResult.getOrElse { return Result.failure(it) }
        val plans = trainingRepository.getTrainingPlans().getOrElse { return Result.failure(it) }
        val targetPlan = plans.firstOrNull { it.id == planId }
            ?: return Result.failure(IllegalArgumentException("plan_not_found"))
        val needsPro = targetPlan.status == TrainingPlanStatus.LOCKED
        if (needsPro && (!status.isActive || status.tier == SubscriptionTier.FREE)) {
            return Result.failure(IllegalStateException("pro_required"))
        }
        return trainingRepository.completeTrainingTask(planId, taskId)
    }
}
