package com.horsegallop.data.training.repository

import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.model.TrainingTaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrainingRepositoryImplTest {

    private lateinit var repository: TrainingRepositoryImpl

    @Before
    fun setUp() {
        repository = TrainingRepositoryImpl()
    }

    // ─── getTrainingPlans / observeTrainingPlans ──────────────────────────────

    @Test
    fun `getTrainingPlans returns default plans`() = runTest {
        val result = repository.getTrainingPlans()

        assertTrue(result.isSuccess)
        val plans = result.getOrNull()!!
        assertTrue(plans.isNotEmpty())
    }

    @Test
    fun `observeTrainingPlans emits same plans as getTrainingPlans`() = runTest {
        val fromFlow = repository.observeTrainingPlans().first()
        val fromGet = repository.getTrainingPlans().getOrNull()!!

        assertEquals(fromGet.size, fromFlow.size)
        assertEquals(fromGet.map { it.id }, fromFlow.map { it.id })
    }

    @Test
    fun `default plan today_plan has NOT_STARTED status`() = runTest {
        val plans = repository.getTrainingPlans().getOrNull()!!
        val todayPlan = plans.first { it.id == "today_plan" }

        assertEquals(TrainingPlanStatus.NOT_STARTED, todayPlan.status)
        assertEquals(0, todayPlan.progressPercent)
    }

    @Test
    fun `default plan pro_endurance has LOCKED status`() = runTest {
        val plans = repository.getTrainingPlans().getOrNull()!!
        val proPlan = plans.first { it.id == "pro_endurance" }

        assertEquals(TrainingPlanStatus.LOCKED, proPlan.status)
    }

    // ─── completeTrainingTask ─────────────────────────────────────────────────

    @Test
    fun `completeTrainingTask marks task as COMPLETED`() = runTest {
        val result = repository.completeTrainingTask("today_plan", "warmup")

        assertTrue(result.isSuccess)
        val plan = repository.getTrainingPlans().getOrNull()!!.first { it.id == "today_plan" }
        val task = plan.tasks.first { it.id == "warmup" }
        assertEquals(TrainingTaskStatus.COMPLETED, task.status)
    }

    @Test
    fun `completeTrainingTask updates progressPercent after completing one of three tasks`() = runTest {
        repository.completeTrainingTask("today_plan", "warmup")

        val plan = repository.getTrainingPlans().getOrNull()!!.first { it.id == "today_plan" }
        assertEquals(33, plan.progressPercent)
    }

    @Test
    fun `completeTrainingTask sets status to IN_PROGRESS after first task`() = runTest {
        repository.completeTrainingTask("today_plan", "warmup")

        val plan = repository.getTrainingPlans().getOrNull()!!.first { it.id == "today_plan" }
        assertEquals(TrainingPlanStatus.IN_PROGRESS, plan.status)
    }

    @Test
    fun `completeTrainingTask sets status to COMPLETED when all tasks done`() = runTest {
        repository.completeTrainingTask("today_plan", "warmup")
        repository.completeTrainingTask("today_plan", "main")
        repository.completeTrainingTask("today_plan", "cooldown")

        val plan = repository.getTrainingPlans().getOrNull()!!.first { it.id == "today_plan" }
        assertEquals(TrainingPlanStatus.COMPLETED, plan.status)
        assertEquals(100, plan.progressPercent)
    }

    @Test
    fun `completeTrainingTask returns failure for unknown planId`() = runTest {
        val result = repository.completeTrainingTask("nonexistent_plan", "warmup")

        assertTrue(result.isFailure)
        assertEquals("plan_not_found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `completeTrainingTask returns failure for LOCKED plan`() = runTest {
        val result = repository.completeTrainingTask("pro_endurance", "pro_endurance_1")

        assertTrue(result.isFailure)
        assertEquals("pro_required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `completeTrainingTask returns failure for unknown taskId`() = runTest {
        val result = repository.completeTrainingTask("today_plan", "nonexistent_task")

        assertTrue(result.isFailure)
        assertEquals("task_not_found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `observeTrainingPlans emits updated state after completeTrainingTask`() = runTest {
        repository.completeTrainingTask("today_plan", "warmup")

        val plan = repository.observeTrainingPlans().first().first { it.id == "today_plan" }
        val task = plan.tasks.first { it.id == "warmup" }
        assertEquals(TrainingTaskStatus.COMPLETED, task.status)
    }
}
