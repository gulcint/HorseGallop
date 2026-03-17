package com.horsegallop.feature.subscription.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.domain.subscription.model.SubscriptionTier
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

// ─── Feature metadata ────────────────────────────────────────────────────────

private data class FeatureItem(val emoji: String, val resId: Int)

private val FEATURE_ITEMS = listOf(
    FeatureItem("🏇", R.string.subscription_feature_gait),
    FeatureItem("📋", R.string.subscription_feature_training),
    FeatureItem("📊", R.string.subscription_feature_elevation),
    FeatureItem("🔥", R.string.subscription_feature_calories),
    FeatureItem("🛡️", R.string.subscription_feature_safety),
    FeatureItem("💬", R.string.subscription_feature_support)
)

// ─── Entry composable ─────────────────────────────────────────────────────────

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LocalSemanticColors.current.screenTopBar
                )
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

// ─── Content ─────────────────────────────────────────────────────────────────

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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeroSection() }
        item { FeatureListSection() }

        if (!isPro) {
            item { ProFeaturesList() }
            item {
                PlanToggleSection(
                    selectedPlan = ui.selectedPlan,
                    onSelectPlan = onSelectPlan
                )
            }
            item {
                AnimatedPurchaseButton(
                    selectedPlan = ui.selectedPlan,
                    isPurchasing = ui.isPurchasing,
                    onPurchase = onPurchase
                )
            }
            item { GuaranteeRow() }
            item { RestoreAndTermsRow(isRestoring = ui.isRestoring, onRestore = onRestore) }
        } else {
            item { AlreadyProSection() }
        }
    }
}

// ─── Hero banner ─────────────────────────────────────────────────────────────

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🐴🏆",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = stringResource(R.string.subscription_hero_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.subscription_hero_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Feature list ─────────────────────────────────────────────────────────────

@Composable
private fun FeatureListSection() {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.subscription_features_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FEATURE_ITEMS.forEach { feature ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feature.emoji,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(feature.resId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = LocalSemanticColors.current.success,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Pro features preview ────────────────────────────────────────────────────

@Composable
private fun ProFeaturesList() {
    val semantic = LocalSemanticColors.current
    val features = listOf<Pair<Int, androidx.compose.ui.graphics.vector.ImageVector>>(
        Pair(R.string.pro_feature_gait, Icons.Filled.TrendingUp),
        Pair(R.string.pro_feature_training, Icons.Filled.EmojiEvents),
        Pair(R.string.pro_feature_calories, Icons.Filled.Star),
        Pair(R.string.pro_feature_challenges, Icons.Filled.Shield)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.pro_features_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            features.forEach { (textRes, icon) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(textRes),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ─── Plan toggle ──────────────────────────────────────────────────────────────

@Composable
private fun PlanToggleSection(
    selectedPlan: SubscriptionPlan,
    onSelectPlan: (SubscriptionPlan) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val yearlySavings = stringResource(R.string.subscription_plan_yearly_savings)
    val yearlyTitle = stringResource(R.string.subscription_plan_yearly_title)
    val yearlyPrice = stringResource(R.string.subscription_plan_yearly_price)
    val yearlyNote = stringResource(R.string.subscription_plan_yearly_note)
    val monthlyTitle = stringResource(R.string.subscription_plan_monthly_title)
    val monthlyPrice = stringResource(R.string.subscription_plan_monthly_price)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.subscription_pick_plan),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        // Toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(semantic.cardSubtle)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Monthly
            PlanTab(
                title = monthlyTitle,
                price = monthlyPrice,
                badge = null,
                isSelected = selectedPlan == SubscriptionPlan.MONTHLY,
                onClick = { onSelectPlan(SubscriptionPlan.MONTHLY) },
                modifier = Modifier.weight(1f)
            )
            // Yearly (highlighted)
            PlanTab(
                title = yearlyTitle,
                price = yearlyPrice,
                badge = yearlySavings,
                isSelected = selectedPlan == SubscriptionPlan.YEARLY,
                onClick = { onSelectPlan(SubscriptionPlan.YEARLY) },
                modifier = Modifier.weight(1f)
            )
        }

        // Detail note for yearly plan
        if (selectedPlan == SubscriptionPlan.YEARLY) {
            Text(
                text = yearlyNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun PlanTab(
    title: String,
    price: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
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
            Text(
                text = price,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Animated CTA ─────────────────────────────────────────────────────────────

@Composable
private fun AnimatedPurchaseButton(
    selectedPlan: SubscriptionPlan,
    isPurchasing: Boolean,
    onPurchase: () -> Unit
) {
    val label = if (selectedPlan == SubscriptionPlan.YEARLY) {
        stringResource(R.string.subscription_cta_yearly)
    } else {
        stringResource(R.string.subscription_cta_monthly)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sub_cta_pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPurchasing) 1f else 1.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .clickable(enabled = !isPurchasing, onClick = onPurchase),
        contentAlignment = Alignment.Center
    ) {
        if (isPurchasing) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Guarantee row ────────────────────────────────────────────────────────────

@Composable
private fun GuaranteeRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            tint = LocalSemanticColors.current.success,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.subscription_guarantee),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Restore + Terms ─────────────────────────────────────────────────────────

@Composable
private fun RestoreAndTermsRow(
    isRestoring: Boolean,
    onRestore: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val termsUrl = stringResource(R.string.subscription_terms_url)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRestoring) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(6.dp))
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
        TextButton(onClick = { uriHandler.openUri(termsUrl) }) {
            Text(
                text = stringResource(R.string.subscription_terms),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ─── Already Pro ─────────────────────────────────────────────────────────────

@Composable
private fun AlreadyProSection() {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.calloutSuccessContainer),
        border = BorderStroke(1.dp, semantic.success.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🎉", style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(R.string.subscription_already_pro_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
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

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SubscriptionScreenPreview() {
    AppTheme {
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
    AppTheme {
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
