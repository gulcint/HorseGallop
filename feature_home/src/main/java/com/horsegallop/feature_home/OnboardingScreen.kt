package com.horsegallop.feature_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
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
import androidx.compose.ui.tooling.preview.Preview
 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onStart: () -> Unit = {}, onSkip: () -> Unit = {}) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val pages: List<OnboardingPage> = remember(primary, secondary) {
        listOf(
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_ride_tracking,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_ride_tracking,
                gradient = listOf(AppColors.WarmClay, AppColors.ToastedAlmond),
                features = listOf(
                    FeatureRes(Icons.Filled.Navigation, com.horsegallop.core.R.string.onboarding_feature_gps),
                    FeatureRes(Icons.Filled.LocalFireDepartment, com.horsegallop.core.R.string.onboarding_feature_calorie),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.core.R.string.onboarding_feature_trends)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_watch,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_watch,
                gradient = listOf(AppColors.WarmClay, primary),
                features = listOf(
                    FeatureRes(Icons.Filled.MonitorHeart, com.horsegallop.core.R.string.onboarding_feature_heart),
                    FeatureRes(Icons.Filled.DirectionsRun, com.horsegallop.core.R.string.onboarding_feature_control),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.core.R.string.onboarding_feature_sync)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_dashboard,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_dashboard,
                gradient = listOf(AppColors.ToastedAlmond, primary),
                features = listOf(
                    FeatureRes(Icons.Filled.Insights, com.horsegallop.core.R.string.onboarding_feature_durations),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.core.R.string.onboarding_feature_charts),
                    FeatureRes(Icons.Filled.EmojiEvents, com.horsegallop.core.R.string.onboarding_feature_goals)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.core.R.string.onboarding_title_community,
                subtitleRes = com.horsegallop.core.R.string.onboarding_subtitle_community,
                gradient = listOf(primary, AppColors.SoftSand),
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
                .padding(top = 60.dp)
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
                .padding(24.dp)
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

                Spacer(modifier = Modifier.height(24.dp))

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

@Preview(showBackground = true, name = "Onboarding - First Page")
@Composable
private fun PreviewOnboardingScreen() {
    MaterialTheme {
        OnboardingScreen()
    }
}
@Composable
private fun ThemedAnimatedBackground(gradient: List<Color>) {
    val transition = rememberInfiniteTransition(label = "bg")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )
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
                    ),
                    start = Offset(0f, shift),
                    end = Offset(shift, 0f)
                )
            )
    )
}

@Composable
private fun AnimatedCoffeeOverlay() {
    val transition = rememberInfiniteTransition(label = "beige")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )
    val softCoffee1 = AppColors.LightCoffee.copy(alpha = 0.55f)
    val softCoffee2 = AppColors.LightCoffee.copy(alpha = 0.35f)
    val softCoffee3 = Color.White.copy(alpha = 0.30f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(softCoffee1, softCoffee2, softCoffee3),
                    start = Offset(0f, shift),
                    end = Offset(shift, 0f),
                    tileMode = TileMode.Clamp
                )
            )
    )
}


@Composable
private fun OnboardingPageContentAnimated(page: OnboardingPage) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
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
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .graphicsLayer { alpha = alphaAnim },
        verticalArrangement = Arrangement.Center,
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
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(500)) +
                        slideInVertically(initialOffsetY = { -16 }, animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(280)) +
                        slideOutVertically(targetOffsetY = { -16 }, animationSpec = tween(280))
            ) {
                Text(
                    text = stringResource(page.subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Callout centered between title and features
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(550)) +
                    slideInVertically(initialOffsetY = { 28 }, animationSpec = tween(550)),
            exit = fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(targetOffsetY = { 28 }, animationSpec = tween(300))
        ) {
            EngagingCallout(gradient = page.gradient)
        }

        Spacer(modifier = Modifier.height(20.dp))

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

// Removed visual placeholder: if image fails, layout centers text/buttons and hides the image card

private data class FeatureRes(val icon: androidx.compose.ui.graphics.vector.ImageVector, val textRes: Int)

private data class OnboardingPage(
    val titleRes: Int,
    val subtitleRes: Int,
    val gradient: List<Color>,
    val features: List<FeatureRes> = emptyList()
)


@Composable
private fun EngagingCallout(gradient: List<Color>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCCFFFFFF))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emoji as lightweight visual mascot
                Text(text = "🐴", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF8B4513))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = com.horsegallop.core.R.string.onboarding_callout_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B2A1E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = com.horsegallop.core.R.string.onboarding_callout_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x993B2A1E)
                    )
                }
                // No action button
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

