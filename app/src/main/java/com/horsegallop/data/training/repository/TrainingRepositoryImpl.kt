package com.horsegallop.data.training.repository

import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.model.TrainingTask
import com.horsegallop.domain.training.model.TrainingTaskStatus
import com.horsegallop.domain.training.repository.TrainingRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val functions: FirebaseFunctions
) : TrainingRepository {

    private val plansFlow = MutableStateFlow(defaultPlans())

    override fun observeTrainingPlans(): Flow<List<TrainingPlan>> = plansFlow.asStateFlow()

    override suspend fun getTrainingPlans(): Result<List<TrainingPlan>> {
        return try {
            val response = functions
                .getHttpsCallable("getTrainingPlans")
                .call()
                .await()
            val payload = response.data as? Map<*, *> ?: emptyMap<String, Any?>()
            val rawPlans = payload["plans"] as? List<*> ?: emptyList<Any?>()
            val mappedPlans = rawPlans.mapNotNull(::mapPlan).ifEmpty { plansFlow.value }
            plansFlow.value = mappedPlans
            Result.success(mappedPlans)
        } catch (_: Exception) {
            Result.success(plansFlow.value)
        }
    }

    override suspend fun completeTrainingTask(planId: String, taskId: String): Result<Unit> {
        try {
            functions
                .getHttpsCallable("completeTrainingTask")
                .call(mapOf("planId" to planId, "taskId" to taskId))
                .await()
        } catch (_: Exception) {
            // Keep local-first behavior; sync can be retried from backend later.
        }

        val current = plansFlow.value
        val updated = current.map { plan ->
            if (plan.id != planId) return@map plan
            val tasks = plan.tasks.map { task ->
                if (task.id == taskId && task.status != TrainingTaskStatus.LOCKED) {
                    task.copy(status = TrainingTaskStatus.COMPLETED)
                } else {
                    task
                }
            }
            val completedCount = tasks.count { it.status == TrainingTaskStatus.COMPLETED }
            val progress = if (tasks.isEmpty()) 0 else (completedCount * 100 / tasks.size)
            val status = when {
                progress >= 100 -> TrainingPlanStatus.COMPLETED
                progress > 0 -> TrainingPlanStatus.IN_PROGRESS
                else -> plan.status
            }
            plan.copy(
                tasks = tasks,
                progressPercent = progress,
                status = status,
                streakDays = if (progress > 0) plan.streakDays + 1 else plan.streakDays
            )
        }
        plansFlow.value = updated
        return Result.success(Unit)
    }

    private fun mapPlan(raw: Any?): TrainingPlan? {
        val map = raw as? Map<*, *> ?: return null
        val tasks = (map["tasks"] as? List<*>).orEmpty().mapNotNull(::mapTask)
        return TrainingPlan(
            id = map["id"] as? String ?: return null,
            title = map["title"] as? String ?: return null,
            summary = map["summary"] as? String ?: "",
            weeklyGoal = (map["weeklyGoal"] as? Number)?.toInt() ?: 0,
            progressPercent = (map["progressPercent"] as? Number)?.toInt() ?: 0,
            streakDays = (map["streakDays"] as? Number)?.toInt() ?: 0,
            status = (map["status"] as? String).toPlanStatus(),
            tasks = tasks
        )
    }

    private fun mapTask(raw: Any?): TrainingTask? {
        val map = raw as? Map<*, *> ?: return null
        return TrainingTask(
            id = map["id"] as? String ?: return null,
            title = map["title"] as? String ?: return null,
            description = map["description"] as? String ?: "",
            targetMinutes = (map["targetMinutes"] as? Number)?.toInt() ?: 0,
            status = (map["status"] as? String).toTaskStatus()
        )
    }

    private fun String?.toPlanStatus(): TrainingPlanStatus {
        return when (this) {
            "IN_PROGRESS" -> TrainingPlanStatus.IN_PROGRESS
            "COMPLETED" -> TrainingPlanStatus.COMPLETED
            "LOCKED" -> TrainingPlanStatus.LOCKED
            else -> TrainingPlanStatus.NOT_STARTED
        }
    }

    private fun String?.toTaskStatus(): TrainingTaskStatus {
        return when (this) {
            "IN_PROGRESS" -> TrainingTaskStatus.IN_PROGRESS
            "COMPLETED" -> TrainingTaskStatus.COMPLETED
            "LOCKED" -> TrainingTaskStatus.LOCKED
            else -> TrainingTaskStatus.NOT_STARTED
        }
    }

    private fun defaultPlans(): List<TrainingPlan> {
        return listOf(
            TrainingPlan(
                id = "plan_foundation",
                title = "Foundation Control",
                summary = "Balance, posture and smooth transitions for new-season prep.",
                weeklyGoal = 3,
                progressPercent = 20,
                streakDays = 2,
                status = TrainingPlanStatus.IN_PROGRESS,
                tasks = listOf(
                    TrainingTask("task_1", "Warm-up 20 min", "Walk/trot interval.", 20, TrainingTaskStatus.COMPLETED),
                    TrainingTask("task_2", "Core seat drills", "No-stirrup seated work.", 25, TrainingTaskStatus.IN_PROGRESS),
                    TrainingTask("task_3", "Cooldown", "Light walk and breathing.", 10, TrainingTaskStatus.NOT_STARTED)
                )
            ),
            TrainingPlan(
                id = "plan_endurance_pro",
                title = "Endurance Pro 4W",
                summary = "Distance pacing and heart-zone control for competitions.",
                weeklyGoal = 4,
                progressPercent = 0,
                streakDays = 0,
                status = TrainingPlanStatus.LOCKED,
                tasks = listOf(
                    TrainingTask("task_e1", "Zone 2 Ride", "Steady aerobic base work.", 35, TrainingTaskStatus.LOCKED),
                    TrainingTask("task_e2", "Hill Repeats", "Controlled effort climbs.", 30, TrainingTaskStatus.LOCKED)
                )
            )
        )
    }
}
