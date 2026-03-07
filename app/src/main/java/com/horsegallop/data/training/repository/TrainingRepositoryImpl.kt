package com.horsegallop.data.training.repository

import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.model.TrainingTask
import com.horsegallop.domain.training.model.TrainingTaskStatus
import com.horsegallop.domain.training.repository.TrainingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TrainingRepositoryImpl @Inject constructor() : TrainingRepository {

    private val plansState = MutableStateFlow(defaultPlans())

    override fun observeTrainingPlans(): Flow<List<TrainingPlan>> = plansState.asStateFlow()

    override suspend fun getTrainingPlans(): Result<List<TrainingPlan>> = Result.success(plansState.value)

    override suspend fun completeTrainingTask(planId: String, taskId: String): Result<Unit> {
        val current = plansState.value
        val targetPlan = current.firstOrNull { it.id == planId }
            ?: return Result.failure(IllegalArgumentException("plan_not_found"))
        if (targetPlan.status == TrainingPlanStatus.LOCKED) {
            return Result.failure(IllegalStateException("pro_required"))
        }

        var updated = false
        val next = current.map { plan ->
            if (plan.id != planId) return@map plan
            val tasks = plan.tasks.map { task ->
                if (task.id == taskId) {
                    updated = true
                    task.copy(status = TrainingTaskStatus.COMPLETED)
                } else task
            }
            val completed = tasks.count { it.status == TrainingTaskStatus.COMPLETED }
            val progress = ((completed.toFloat() / tasks.size.toFloat()) * 100f).toInt().coerceIn(0, 100)
            val status = when {
                progress >= 100 -> TrainingPlanStatus.COMPLETED
                progress > 0 -> TrainingPlanStatus.IN_PROGRESS
                else -> TrainingPlanStatus.NOT_STARTED
            }
            plan.copy(tasks = tasks, progressPercent = progress, status = status)
        }

        if (!updated) return Result.failure(IllegalArgumentException("task_not_found"))
        plansState.value = next
        return Result.success(Unit)
    }

    private fun defaultPlans(): List<TrainingPlan> = listOf(
        TrainingPlan(
            id = "today_plan",
            title = "Bugunku Karma Plan",
            summary = "Isinma, ana calisma ve soguma ile dengeli seans",
            weeklyGoal = 5,
            progressPercent = 0,
            streakDays = 0,
            status = TrainingPlanStatus.NOT_STARTED,
            tasks = listOf(
                TrainingTask(
                    id = "warmup",
                    title = "Isinma",
                    description = "Hafif tempoda kontrollu baslangic",
                    targetMinutes = 12,
                    status = TrainingTaskStatus.NOT_STARTED
                ),
                TrainingTask(
                    id = "main",
                    title = "Ana Calisma",
                    description = "Karma interval seti (teknik + dayaniklilik)",
                    targetMinutes = 25,
                    status = TrainingTaskStatus.NOT_STARTED
                ),
                TrainingTask(
                    id = "cooldown",
                    title = "Soguma",
                    description = "Dusuk tempoda kontrollu bitis",
                    targetMinutes = 8,
                    status = TrainingTaskStatus.NOT_STARTED
                )
            )
        ),
        TrainingPlan(
            id = "pro_endurance",
            title = "Pro Dayaniklilik",
            summary = "Uzun interval ve tempo stabilitesi odakli",
            weeklyGoal = 4,
            progressPercent = 0,
            streakDays = 0,
            status = TrainingPlanStatus.LOCKED,
            tasks = listOf(
                TrainingTask(
                    id = "pro_endurance_1",
                    title = "Uzun Isinma",
                    description = "Kademeli tempo artisi ile 15 dakika",
                    targetMinutes = 15,
                    status = TrainingTaskStatus.LOCKED
                ),
                TrainingTask(
                    id = "pro_endurance_2",
                    title = "Uzun Ana Set",
                    description = "6x4 dakika orta-yuksek tempo",
                    targetMinutes = 30,
                    status = TrainingTaskStatus.LOCKED
                ),
                TrainingTask(
                    id = "pro_endurance_3",
                    title = "Toparlanma",
                    description = "Nefes duzeni ve dusuk tempo",
                    targetMinutes = 10,
                    status = TrainingTaskStatus.LOCKED
                )
            )
        )
    )
}

