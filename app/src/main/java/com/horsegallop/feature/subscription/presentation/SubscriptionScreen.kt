package com.horsegallop.feature.subscription.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? android.app.Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val msgPurchaseFailed = stringResource(R.string.subscription_purchase_failed)
    val msgRestoreFailed = stringResource(R.string.subscription_restore_failed)
    val msgAlreadyPro = stringResource(R.string.subscription_already_pro)

    LaunchedEffect(ui.error) {
        when (ui.error) {
            "purchase_failed" -> snackbarHostState.showSnackbar(msgPurchaseFailed)
            "restore_failed" -> snackbarHostState.showSnackbar(msgRestoreFailed)
            null -> {}
            else -> snackbarHostState.showSnackbar(ui.error!!)
        }
        if (ui.error != null) viewModel.clearError()
    }

    LaunchedEffect(ui.purchaseSuccess) {
        if (ui.purchaseSuccess) onBack()
    }

    LaunchedEffect(ui.status) {
        if (ui.status.tier != SubscriptionTier.FREE && ui.status.isActive) {
            snackbarHostState.showSnackbar(msgAlreadyPro)
        }
    }

    Scaffold(
        containerColor = LocalSemanticColors.current.screenBase,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.subscription_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        SubscriptionContent(
            ui = ui,
            modifier = Modifier.padding(innerPadding),
            onSelectPlan = viewModel::selectPlan,
            onPurchase = { activity?.let { viewModel.purchase(it) } },
            onRestore = viewModel::restorePurchases
        )
    }
}

@Composable
private fun SubscriptionContent(
    ui: SubscriptionUiState,
    modifier: Modifier = Modifier,
    onSelectPlan: (SubscriptionPlan) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val isPro = ui.status.tier != SubscriptionTier.FREE && ui.status.isActive

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(semantic.screenBase),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeroSection() }
        item { FeatureListSection() }

        if (!isPro) {
            item {
                PlanSelectorSection(
                    selectedPlan = ui.selectedPlan,
                    onSelectPlan = onSelectPlan
                )
            }
            item {
                PurchaseButton(
                    selectedPlan = ui.selectedPlan,
                    isPurchasing = ui.isPurchasing,
                    onPurchase = onPurchase
                )
            }
            item { RestoreAndTermsRow(isRestoring = ui.isRestoring, onRestore = onRestore) }
        } else {
            item { AlreadyProSection() }
        }
    }
}

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Text(
            text = stringResource(R.string.subscription_hero_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.subscription_hero_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeatureListSection() {
    val semantic = LocalSemanticColors.current
    val features = listOf<Int>(
        R.string.subscription_feature_gait,
        R.string.subscription_feature_training,
        R.string.subscription_feature_elevation,
        R.string.subscription_feature_calories,
        R.string.subscription_feature_safety,
        R.string.subscription_feature_support
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.subscription_features_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            features.forEach { resId ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = semantic.success,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(resId),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanSelectorSection(
    selectedPlan: SubscriptionPlan,
    onSelectPlan: (SubscriptionPlan) -> Unit
) {
    val semantic = LocalSemanticColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.subscription_pick_plan),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        // Yıllık plan — öne çıkarılmış
        PlanCard(
            title = stringResource(R.string.subscription_plan_yearly_title),
            price = stringResource(R.string.subscription_plan_yearly_price),
            note = stringResource(R.string.subscription_plan_yearly_note),
            badge = stringResource(R.string.subscription_plan_best_value),
            isSelected = selectedPlan == SubscriptionPlan.YEARLY,
            isHighlighted = true,
            onClick = { onSelectPlan(SubscriptionPlan.YEARLY) }
        )
        // Aylık plan
        PlanCard(
            title = stringResource(R.string.subscription_plan_monthly_title),
            price = stringResource(R.string.subscription_plan_monthly_price),
            note = null,
            badge = null,
            isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
            isHighlighted = false,
            onClick = { onSelectPlan(SubscriptionPlan.MONTHLY) }
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    note: String?,
    badge: String?,
    isSelected: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else semantic.cardStroke
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val containerColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else semantic.cardElevated

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (note != null) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PurchaseButton(
    selectedPlan: SubscriptionPlan,
    isPurchasing: Boolean,
    onPurchase: () -> Unit
) {
    val label = if (selectedPlan == SubscriptionPlan.YEARLY) {
        stringResource(R.string.subscription_cta_yearly)
    } else {
        stringResource(R.string.subscription_cta_monthly)
    }
    Button(
        onClick = onPurchase,
        enabled = !isPurchasing,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isPurchasing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RestoreAndTermsRow(
    isRestoring: Boolean,
    onRestore: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRestoring) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.size(8.dp))
        }
        TextButton(onClick = onRestore, enabled = !isRestoring) {
            Text(
                text = stringResource(R.string.subscription_restore),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "·",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = {}) {
            Text(
                text = stringResource(R.string.subscription_terms),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AlreadyProSection() {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.calloutSuccessContainer),
        border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = semantic.success,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = stringResource(R.string.subscription_already_pro_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.subscription_already_pro_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubscriptionScreenPreview() {
    com.horsegallop.ui.theme.AppTheme {
        SubscriptionContent(
            ui = SubscriptionUiState(selectedPlan = SubscriptionPlan.YEARLY),
            onSelectPlan = {},
            onPurchase = {},
            onRestore = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubscriptionScreenProPreview() {
    com.horsegallop.ui.theme.AppTheme {
        SubscriptionContent(
            ui = SubscriptionUiState(
                status = com.horsegallop.domain.subscription.model.SubscriptionStatus(
                    tier = SubscriptionTier.PRO_YEARLY,
                    isActive = true
                )
            ),
            onSelectPlan = {},
            onPurchase = {},
            onRestore = {}
        )
    }
}
