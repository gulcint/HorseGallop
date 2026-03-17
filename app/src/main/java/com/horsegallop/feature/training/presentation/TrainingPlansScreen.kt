package com.horsegallop.feature.training.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.core.components.ProLockedCard
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.model.TrainingTask
import com.horsegallop.domain.training.model.TrainingTaskStatus
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlansScreen(
    onBack: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    viewModel: TrainingPlansViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val semantic = LocalSemanticColors.current
    val snackbarHostState = remember { SnackbarHostState() }
    val msgUpdateFailed = stringResource(id = R.string.training_update_failed)
    val msgUnknown = stringResource(id = R.string.error_unknown)

    LaunchedEffect(ui.error) {
        val message = when (ui.error) {
            "pro_required" -> null // artık ProLockedCard yönlendiriyor
            "plan_not_found", "task_not_found" -> msgUpdateFailed
            null -> null
            else -> msgUnknown
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = semantic.screenBase,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.training_plans_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (ui.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val isPro = ui.subscription.tier != SubscriptionTier.FREE && ui.subscription.isActive
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(semantic.screenBase),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(ui.plans, key = { it.id }) { plan ->
                    if (plan.status == TrainingPlanStatus.LOCKED && !isPro) {
                        ProLockedCard(
                            featureLabel = plan.title,
                            onNavigateToSubscription = onNavigateToSubscription
                        )
                    } else {
                        TrainingPlanCard(
                            plan = plan,
                            isCompleting = ui.isCompleting,
                            onCompleteTask = { taskId -> viewModel.completeTask(plan.id, taskId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingPlanCard(
    plan: TrainingPlan,
    isCompleting: Boolean,
    onCompleteTask: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = plan.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = plan.status)
            }

            LinearProgressIndicator(
                progress = { plan.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(
                    id = R.string.training_progress_format,
                    plan.progressPercent,
                    plan.weeklyGoal,
                    plan.streakDays
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            plan.tasks.forEach { task ->
                TrainingTaskRow(
                    task = task,
                    isCompleting = isCompleting,
                    planLocked = plan.status == TrainingPlanStatus.LOCKED,
                    onCompleteTask = onCompleteTask
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: TrainingPlanStatus) {
    val semantic = LocalSemanticColors.current
    val (text, bg) = when (status) {
        TrainingPlanStatus.NOT_STARTED -> stringResource(id = R.string.training_plan_not_started) to semantic.calloutInfoContainer
        TrainingPlanStatus.IN_PROGRESS -> stringResource(id = R.string.training_plan_in_progress) to semantic.calloutWarningContainer
        TrainingPlanStatus.COMPLETED -> stringResource(id = R.string.training_plan_completed) to semantic.calloutSuccessContainer
        TrainingPlanStatus.LOCKED -> stringResource(id = R.string.training_plan_locked) to semantic.calloutErrorContainer
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text = text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bg,
            labelColor = semantic.calloutOnContainer
        )
    )
}

@Composable
private fun TrainingTaskRow(
    task: TrainingTask,
    isCompleting: Boolean,
    planLocked: Boolean,
    onCompleteTask: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        border = BorderStroke(1.dp, semantic.cardStroke),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        id = R.string.training_task_meta_format,
                        task.targetMinutes,
                        task.description
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val canComplete = !planLocked &&
                !isCompleting &&
                task.status != TrainingTaskStatus.COMPLETED &&
                task.status != TrainingTaskStatus.LOCKED
            if (canComplete) {
                FilledTonalButton(onClick = { onCompleteTask(task.id) }) {
                    Text(text = stringResource(id = R.string.training_mark_done))
                }
            } else {
                Text(
                    text = when (task.status) {
                        TrainingTaskStatus.COMPLETED -> stringResource(id = R.string.training_task_done)
                        TrainingTaskStatus.LOCKED -> stringResource(id = R.string.training_task_locked)
                        TrainingTaskStatus.IN_PROGRESS -> stringResource(id = R.string.training_task_in_progress)
                        TrainingTaskStatus.NOT_STARTED -> stringResource(id = R.string.training_task_pending)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainingPlansScreenPreview() {
    com.horsegallop.ui.theme.AppTheme {
        TrainingPlansScreen(
            onBack = {},
            onNavigateToSubscription = {}
        )
    }
}
