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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.draw.drawWithCache
import com.horsegallop.ui.theme.LocalSemanticColors
import com.horsegallop.ui.theme.SemanticColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onStart: () -> Unit = {}, onSkip: () -> Unit = {}) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val toastedAlmond = MaterialTheme.colorScheme.secondary
    val softSand = MaterialTheme.colorScheme.tertiaryContainer
    val lightCoffee = MaterialTheme.colorScheme.primaryContainer
    val semantic = LocalSemanticColors.current

    // User requested less whiteness. We use Saddle Brown -> Toasted Almond/Soft Sand.
    // This reduces the white intensity while keeping a gradient.
    val pages: List<OnboardingPage> = remember(primary, secondary, toastedAlmond, softSand, lightCoffee) {
        listOf(
            OnboardingPage(
                titleRes = com.horsegallop.R.string.onboarding_title_ranch,
                subtitleRes = com.horsegallop.R.string.onboarding_subtitle_ranch,
                // Saddle Brown -> Toasted Almond (Warm, reduced whiteness)
                gradient = listOf(primary, toastedAlmond),
                features = listOf(
                    FeatureRes(Icons.Filled.Home, com.horsegallop.R.string.onboarding_feature_barn_select),
                    FeatureRes(Icons.Filled.MedicalServices, com.horsegallop.R.string.onboarding_feature_safety),
                    FeatureRes(Icons.Filled.Star, com.horsegallop.R.string.onboarding_feature_signup)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.R.string.onboarding_title_packages,
                subtitleRes = com.horsegallop.R.string.onboarding_subtitle_packages,
                // Saddle Brown -> Soft Sand (Light beige, but not white)
                gradient = listOf(primary, softSand),
                features = listOf(
                    FeatureRes(Icons.Filled.School, com.horsegallop.R.string.onboarding_feature_reserve),
                    FeatureRes(Icons.Filled.Timeline, com.horsegallop.R.string.onboarding_feature_progress),
                    FeatureRes(Icons.Filled.Navigation, com.horsegallop.R.string.onboarding_feature_support)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.R.string.onboarding_title_boarding,
                subtitleRes = com.horsegallop.R.string.onboarding_subtitle_boarding,
                // Saddle Brown -> Toasted Almond
                gradient = listOf(primary, toastedAlmond),
                features = listOf(
                    FeatureRes(Icons.Filled.Build, com.horsegallop.R.string.onboarding_feature_kvkk),
                    FeatureRes(Icons.Filled.LocalFireDepartment, com.horsegallop.R.string.onboarding_feature_safety),
                    FeatureRes(Icons.Filled.Groups, com.horsegallop.R.string.onboarding_feature_reviews)
                )
            ),
            OnboardingPage(
                titleRes = com.horsegallop.R.string.onboarding_title_cafe,
                subtitleRes = com.horsegallop.R.string.onboarding_subtitle_cafe,
                // Saddle Brown -> Soft Sand
                gradient = listOf(primary, softSand),
                features = listOf(
                    FeatureRes(Icons.Filled.EmojiEvents, com.horsegallop.R.string.onboarding_feature_progress),
                    FeatureRes(Icons.Filled.Star, com.horsegallop.R.string.onboarding_feature_reviews),
                    FeatureRes(Icons.Filled.LocalCafe, com.horsegallop.R.string.onboarding_feature_support)
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
        // Animated gradient background (kept, but optimized)
        ThemedAnimatedBackground(gradient = pages[pagerState.currentPage].gradient)
        AnimatedCoffeeOverlay(semantic = semantic)
        // Back button exits app on onboarding
        val activity = LocalContext.current as? Activity
        BackHandler(enabled = true) { activity?.finish() }
        // Pager - Full screen
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            OnboardingPageContentAnimated(
                page = pages[page],
                pageOffset = pageOffset,
                semantic = semantic
            )
        }

        // Progress indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(
                    id = com.horsegallop.R.string.onboarding_progress,
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
                .navigationBarsPadding()
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
                    TextButton(onClick = onSkip) { 
                        Text(
                            stringResource(com.horsegallop.R.string.onboarding_skip), 
                            color = semantic.onImageOverlay
                        ) 
                    }
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
                            if (isLast) stringResource(com.horsegallop.R.string.onboarding_start)
                            else stringResource(com.horsegallop.R.string.onboarding_next)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ThemedAnimatedBackground(gradient: List<Color>) {
    val transition = rememberInfiniteTransition(label = "bg")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )
    val drift by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val fallbackPrimary = MaterialTheme.colorScheme.primaryContainer
    val fallbackSecondary = MaterialTheme.colorScheme.secondaryContainer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val start = Offset(x = size.width * (0.1f + 0.2f * shift), y = size.height * (0.1f + 0.2f * drift))
                val end = Offset(x = size.width * (0.9f - 0.2f * shift), y = size.height * (0.9f - 0.2f * drift))
                val brush = Brush.linearGradient(
                    colors = listOf(
                        gradient.firstOrNull() ?: fallbackPrimary,
                        gradient.getOrNull(1) ?: fallbackSecondary
                    ),
                    start = start,
                    end = end,
                    tileMode = TileMode.Clamp
                )
                onDrawBehind { drawRect(brush) }
            }
    )
}

@Composable
private fun AnimatedCoffeeOverlay(semantic: SemanticColors) {
    val transition = rememberInfiniteTransition(label = "coffee")
    val pulse by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val slide by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slide"
    )
    val softCoffee1 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f + pulse * 0.4f)
    val softCoffee2 = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f + pulse * 0.4f)
    val softCoffee3 = semantic.onImageOverlay.copy(alpha = 0.12f + pulse * 0.4f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val start = Offset(x = size.width * (0.15f + 0.2f * slide), y = size.height * 0.1f)
                val end = Offset(x = size.width * (0.85f - 0.2f * slide), y = size.height * 0.9f)
                val brush = Brush.linearGradient(
                    colors = listOf(softCoffee1, softCoffee2, softCoffee3),
                    start = start,
                    end = end
                )
                onDrawBehind { drawRect(brush) }
            }
            .alpha(0.55f)
    )
}


@Composable
private fun OnboardingPageContentAnimated(
    page: OnboardingPage,
    pageOffset: Float,
    semantic: SemanticColors
) {
    val clamped = pageOffset.coerceIn(-1f, 1f)
    val alpha = 1f - kotlin.math.abs(clamped) * 0.25f
    val parallax = 24f * clamped
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .graphicsLayer {
                this.alpha = alpha
                translationX = parallax
            }
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = semantic.onImageOverlay
        )
        Spacer(modifier = Modifier.height(16.dp))

        EngagingCallout(
            titleRes = page.titleRes,
            subtitleRes = page.subtitleRes,
            gradient = page.gradient
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            page.features.forEach { feature ->
                FeatureBullet(icon = feature.icon, text = stringResource(id = feature.textRes))
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
    val start = gradient.firstOrNull()?.copy(alpha = 0.35f)
        ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    val end = gradient.getOrNull(1)?.copy(alpha = 0.35f)
        ?: MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
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
                            MaterialTheme.colorScheme.surface,
                            start,
                            end
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
                    color = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val subtitle = stringResource(id = subtitleRes)
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
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
            titleRes = com.horsegallop.R.string.onboarding_title_ride_tracking,
            subtitleRes = com.horsegallop.R.string.onboarding_subtitle_ride_tracking,
            gradient = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
        )
    }
}
