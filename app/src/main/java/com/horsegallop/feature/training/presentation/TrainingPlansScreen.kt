@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.horsegallop.feature.training.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.domain.training.model.TrainingPlan
import com.horsegallop.domain.training.model.TrainingPlanStatus
import com.horsegallop.domain.training.model.TrainingTaskStatus
import com.horsegallop.ui.theme.LocalSemanticColors

private const val PRODUCT_PRO_MONTHLY = "horsegallop_pro_monthly"
private const val PRODUCT_PRO_YEARLY = "horsegallop_pro_yearly"

@Composable
fun TrainingPlansRoute(
    onBack: () -> Unit,
    viewModel: TrainingPlansViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val feedback = LocalAppFeedbackController.current

    LaunchedEffect(state.infoMessageResId, state.errorMessageResId) {
        state.infoMessageResId?.let { feedback.showSuccess(it) }
        state.errorMessageResId?.let { feedback.showError(it) }
        if (state.infoMessageResId != null || state.errorMessageResId != null) {
            viewModel.consumeMessages()
        }
    }

    TrainingPlansScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::loadPlans,
        onCompleteTask = viewModel::onCompleteTask,
        onPurchaseMonthly = { viewModel.onStartPurchase(PRODUCT_PRO_MONTHLY) },
        onPurchaseYearly = { viewModel.onStartPurchase(PRODUCT_PRO_YEARLY) }
    )
}

@Composable
fun TrainingPlansScreen(
    state: TrainingPlansUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCompleteTask: (String, String) -> Unit,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.training_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.subscriptionStatus.tier == SubscriptionTier.FREE) {
                    item {
                        PaywallCard(
                            isPurchasing = state.isPurchasing,
                            onPurchaseMonthly = onPurchaseMonthly,
                            onPurchaseYearly = onPurchaseYearly
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, semantic.cardStroke)
                    ) {
                        Text(text = stringResource(id = R.string.retry))
                    }
                }

                items(state.plans, key = { it.id }) { plan ->
                    TrainingPlanCard(
                        plan = plan,
                        isPro = state.subscriptionStatus.isActive,
                        onCompleteTask = onCompleteTask
                    )
                }
            }
        }
    }
}

@Composable
private fun PaywallCard(
    isPurchasing: Boolean,
    onPurchaseMonthly: () -> Unit,
    onPurchaseYearly: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(id = R.string.training_pro_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = stringResource(id = R.string.training_pro_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isPurchasing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPurchaseMonthly,
                    enabled = !isPurchasing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.subscription_monthly))
                }
                OutlinedButton(
                    onClick = onPurchaseYearly,
                    enabled = !isPurchasing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, semantic.cardStroke)
                ) {
                    Text(text = stringResource(id = R.string.subscription_yearly))
                }
            }
        }
    }
}

@Composable
private fun TrainingPlanCard(
    plan: TrainingPlan,
    isPro: Boolean,
    onCompleteTask: (String, String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val isLocked = plan.status == TrainingPlanStatus.LOCKED && !isPro

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (isLocked) {
                                stringResource(id = R.string.training_status_locked)
                            } else {
                                stringResource(id = R.string.training_progress_percent, plan.progressPercent)
                            }
                        )
                    },
                    leadingIcon = if (isLocked) {
                        {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        }
                    } else {
                        null
                    }
                )
            }

            LinearProgressIndicator(
                progress = { (plan.progressPercent.coerceIn(0, 100) / 100f) },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            plan.tasks.take(3).forEach { task ->
                val canComplete = !isLocked && task.status != TrainingTaskStatus.COMPLETED
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { onCompleteTask(plan.id, task.id) },
                        enabled = canComplete,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, semantic.cardStroke)
                    ) {
                        Text(
                            text = if (task.status == TrainingTaskStatus.COMPLETED) {
                                stringResource(id = R.string.training_status_completed)
                            } else {
                                stringResource(id = R.string.training_mark_done)
                            }
                        )
                    }
                }
            }
        }
    }
}
