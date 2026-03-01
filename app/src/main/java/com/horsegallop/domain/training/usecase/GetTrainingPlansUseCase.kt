package com.horsegallop.domain.training.usecase

import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.repository.TrainingRepository
import javax.inject.Inject

class GetTrainingPlansUseCase @Inject constructor(
    private val repository: TrainingRepository
) {
    suspend operator fun invoke(): Result<List<TrainingPlan>> = repository.getTrainingPlans()
}
