package com.horsegallop.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.ui.tooling.preview.Preview
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TileMode
import com.horsegallop.theme.AppColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import kotlinx.coroutines.delay
 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onStart: () -> Unit = {}, onSkip: () -> Unit = {}) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val warmClay = AppColors.WarmClay
    val toastedAlmond = AppColors.ToastedAlmond
    val softSand = AppColors.SoftSand
    val pages: List<OnboardingPage> = remember(primary, secondary, warmClay, toastedAlmond, softSand) {
        listOf(
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_ride_tracking,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_ride_tracking,
                gradient = listOf(warmClay, toastedAlmond),
                features = listOf(
                    FeatureRes(Icons.Filled.Navigation, com.horsegallop.core.R.string.onboarding_feature_gps),
                    FeatureRes(Icons.Filled.LocalFireDepartment, com.horsegallop.core.R.string.onboarding_feature_calorie),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.core.R.string.onboarding_feature_trends)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_watch,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_watch,
                gradient = listOf(warmClay, primary),
                features = listOf(
                    FeatureRes(Icons.Filled.MonitorHeart, com.horsegallop.core.R.string.onboarding_feature_heart),
                    FeatureRes(Icons.AutoMirrored.Filled.DirectionsRun, com.horsegallop.core.R.string.onboarding_feature_control),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.core.R.string.onboarding_feature_sync)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_dashboard,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_dashboard,
                gradient = listOf(toastedAlmond, primary),
                features = listOf(
                    FeatureRes(Icons.Filled.Insights, com.horsegallop.core.R.string.onboarding_feature_durations),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.core.R.string.onboarding_feature_charts),
                    FeatureRes(Icons.Filled.EmojiEvents, com.horsegallop.core.R.string.onboarding_feature_goals)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_community,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_community,
                gradient = listOf(primary, softSand),
                features = listOf(
                    FeatureRes(Icons.Filled.Groups, com.horsegallop.core.R.string.onboarding_feature_sharing),
                    FeatureRes(Icons.Filled.EmojiEvents, com.horsegallop.core.R.string.onboarding_feature_leaderboard),
                    FeatureRes(Icons.Filled.Navigation, com.horsegallop.core.R.string.onboarding_feature_nearby)
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Theme-based animated gradient background instead of external images
        ThemedAnimatedBackground(gradient = pages[pagerState.currentPage].gradient)
        // Back button exits app on onboarding
        val activity = LocalContext.current as? Activity
        BackHandler(enabled = true) { activity?.finish() }
        // Pager - Full screen
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContentAnimated(pages[page])
        }

        // Progress indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = stringResource(
                    id = com.horsegallop.core.R.string.onboarding_progress,
                    pagerState.currentPage + 1,
                    pages.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        

        // Bottom controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicators
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(pages.size) { index ->
                        val isActive: Boolean = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                                .size(if (isActive) 24.dp else 8.dp, 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLast: Boolean = pagerState.currentPage == pages.lastIndex
                    TextButton(onClick = onSkip) { Text(stringResource(com.horsegallop.core.R.string.onboarding_skip), color = MaterialTheme.colorScheme.onPrimary) }
                    Button(
                        onClick = {
                            if (isLast) onStart() else {
                                scope.launch {
                                    val next = (pagerState.currentPage + 1).coerceAtMost(pages.lastIndex)
                                    pagerState.animateScrollToPage(next)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            if (isLast) stringResource(com.horsegallop.core.R.string.onboarding_start)
                            else stringResource(com.horsegallop.core.R.string.onboarding_next)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ThemedAnimatedBackground(gradient: List<Color>) {
    // Simplified static background for better performance
    val colors = if (gradient.isNotEmpty()) gradient else listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors[0].copy(alpha = 0.95f),
                        colors.getOrElse(1) { colors[0] }.copy(alpha = 0.85f)
                    )
                )
            )
    )
}

@Composable
private fun AnimatedCoffeeOverlay() {
    // Simplified static overlay for better performance
    val softCoffee1 = AppColors.LightCoffee.copy(alpha = 0.55f)
    val softCoffee2 = AppColors.LightCoffee.copy(alpha = 0.35f)
    val softCoffee3 = Color.White.copy(alpha = 0.30f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(softCoffee1, softCoffee2, softCoffee3)
                )
            )
    )
}


@Composable
private fun OnboardingPageContentAnimated(page: OnboardingPage) {
    // Simplified without continuous pulse animation for better performance
    var showContent by remember(page) { mutableStateOf(false) }
    val featureVisibility: MutableList<Boolean> = remember(page) {
        mutableStateListOf<Boolean>().apply { repeat(page.features.size) { add(false) } }
    }
    LaunchedEffect(page) {
        showContent = false
        repeat(page.features.size) { featureVisibility[it] = false }
        delay(150)
        showContent = true
        page.features.indices.forEach { i ->
            delay(140)
            featureVisibility[i] = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
            .graphicsLayer { alpha = 1f },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title and subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(550)) +
                        slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(550)),
                exit = fadeOut(animationSpec = tween(300)) +
                        slideOutVertically(targetOffsetY = { -40 }, animationSpec = tween(300))
            ) {
                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Callout centered between title and features
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(550)) +
                    slideInVertically(initialOffsetY = { 28 }, animationSpec = tween(550)),
            exit = fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(targetOffsetY = { 28 }, animationSpec = tween(300))
        ) {
            EngagingCallout(titleRes = page.titleRes, subtitleRes = page.subtitleRes, gradient = page.gradient)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feature bullets
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            page.features.forEachIndexed { i, feature ->
                AnimatedVisibility(
                    visible = featureVisibility.getOrNull(i) == true,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(initialOffsetY = { 24 }, animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(250)) +
                            slideOutVertically(targetOffsetY = { 24 }, animationSpec = tween(250))
                ) {
                    FeatureBullet(icon = feature.icon, text = stringResource(id = feature.textRes))
                }
            }
        }
    }
}

private data class FeatureRes(val icon: androidx.compose.ui.graphics.vector.ImageVector, val textRes: Int)

private data class OnboardingPage(
    val titleRes: Int,
    val subtitleRes: Int,
    val gradient: List<Color>,
    val features: List<FeatureRes> = emptyList()
)


@Composable
private fun EngagingCallout(titleRes: Int, subtitleRes: Int, gradient: List<Color>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFE9ECEF)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🐴", 
                    style = MaterialTheme.typography.headlineMedium, 
                    color = Color(0xFF8B4513)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val subtitle = stringResource(id = subtitleRes)
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x7F2C3E50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureBullet(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
        Text(text, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen(
            onStart = {},
            onSkip = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun EngagingCalloutPreview() {
    MaterialTheme {
        EngagingCallout(
            titleRes = com.horsegallop.core.R.string.onboarding_title_ride_tracking,
            subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_ride_tracking,
            gradient = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
        )
    }
}
