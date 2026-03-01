package com.horsegallop.domain.training.repository

import com.horsegallop.domain.training.model.TrainingPlan
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeTrainingPlans(): Flow<List<TrainingPlan>>
    suspend fun getTrainingPlans(): Result<List<TrainingPlan>>
    suspend fun completeTrainingTask(planId: String, taskId: String): Result<Unit>
}
