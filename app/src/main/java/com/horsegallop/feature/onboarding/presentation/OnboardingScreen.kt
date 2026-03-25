package com.horsegallop.feature.onboarding.presentation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.LottieComposition
import androidx.compose.ui.layout.ContentScale
import com.horsegallop.R
import com.horsegallop.ui.theme.LocalSemanticColors
import com.horsegallop.ui.theme.SemanticColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onStart: () -> Unit = {},
    onSkip: () -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val warmUmber = MaterialTheme.colorScheme.primary
    val warmCopper = MaterialTheme.colorScheme.secondary
    val warmChestnut = MaterialTheme.colorScheme.tertiary
    val warmClay = MaterialTheme.colorScheme.primaryContainer
    val semantic = LocalSemanticColors.current
    val uiState by viewModel.uiState.collectAsState()
    // ── Pages ─────────────────────────────────────────────────────────────────
    val pages: List<OnboardingPage> = remember(
        warmUmber, warmCopper, warmChestnut, warmClay,
        uiState.heroTitle, uiState.heroSubtitle
    ) {
        listOf(
            OnboardingPage(
                titleRes = R.string.onboarding_title_ranch,
                subtitleRes = R.string.onboarding_subtitle_ranch,
                titleOverride = uiState.heroTitle,
                subtitleOverride = uiState.heroSubtitle,
                gradient = listOf(warmUmber, warmCopper),
                features = listOf(
                    FeatureRes(Icons.Filled.Home, R.string.onboarding_feature_barn_select),
                    FeatureRes(Icons.Filled.MedicalServices, R.string.onboarding_feature_safety),
                    FeatureRes(Icons.Filled.Star, R.string.onboarding_feature_signup)
                )
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_packages,
                subtitleRes = R.string.onboarding_subtitle_packages,
                gradient = listOf(warmUmber, warmChestnut),
                features = listOf(
                    FeatureRes(Icons.Filled.School, R.string.onboarding_feature_reserve),
                    FeatureRes(Icons.Filled.Timeline, R.string.onboarding_feature_progress),
                    FeatureRes(Icons.Filled.Navigation, R.string.onboarding_feature_support)
                )
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_boarding,
                subtitleRes = R.string.onboarding_subtitle_boarding,
                gradient = listOf(warmCopper, warmChestnut),
                features = listOf(
                    FeatureRes(Icons.Filled.Build, R.string.onboarding_feature_kvkk),
                    FeatureRes(Icons.Filled.LocalFireDepartment, R.string.onboarding_feature_safety),
                    FeatureRes(Icons.Filled.Groups, R.string.onboarding_feature_reviews)
                )
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_cafe,
                subtitleRes = R.string.onboarding_subtitle_cafe,
                gradient = listOf(warmUmber, warmClay),
                features = listOf(
                    FeatureRes(Icons.Filled.EmojiEvents, R.string.onboarding_feature_progress),
                    FeatureRes(Icons.Filled.Star, R.string.onboarding_feature_reviews),
                    FeatureRes(Icons.Filled.LocalCafe, R.string.onboarding_feature_support)
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    // Lottie composition bir kere yükle — tüm sayfalar paylaşır, geç yükleme olmaz
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.horse))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(semantic.screenBase)
    ) {
        StaticOnboardingBackground(gradient = pages[pagerState.currentPage].gradient)

        BackHandler(enabled = true) { activity?.finish() }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            OnboardingPageContentAnimated(
                page = pages[page],
                pageOffset = pageOffset,
                semantic = semantic,
                lottieComposition = lottieComposition
            )
        }

        // Progress chip
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Text(
                text = stringResource(
                    id = R.string.onboarding_progress,
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(pages.size) { index ->
                        val isActive = pagerState.currentPage == index
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

                uiState.helpText?.takeIf { it.isNotBlank() }?.let { helpText ->
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.onImageOverlay.copy(alpha = 0.86f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isLast = pagerState.currentPage == pages.lastIndex
                    TextButton(onClick = onSkip) {
                        Text(
                            stringResource(R.string.onboarding_skip),
                            color = semantic.onImageOverlay
                        )
                    }
                    Button(
                        onClick = {
                            if (isLast) onStart() else {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        (pagerState.currentPage + 1).coerceAtMost(pages.lastIndex)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            if (isLast) stringResource(R.string.onboarding_start)
                            else stringResource(R.string.onboarding_next)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticOnboardingBackground(gradient: List<Color>) {
    val fallbackPrimary = MaterialTheme.colorScheme.primaryContainer
    val fallbackSecondary = MaterialTheme.colorScheme.secondaryContainer
    val semantic = LocalSemanticColors.current
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradient.firstOrNull() ?: fallbackPrimary,
                            gradient.getOrNull(1) ?: fallbackSecondary,
                            fallbackPrimary.copy(alpha = 0.88f)
                        )
                    )
                )
        )
        // Metin okunabilirliği için dark scrim — açık gradient tonlarında beyaz metni korur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(semantic.imageOverlayStrong)
        )
    }
}

// ── Per-page content ──────────────────────────────────────────────────────────
@Composable
private fun OnboardingPageContentAnimated(
    page: OnboardingPage,
    pageOffset: Float,
    semantic: SemanticColors,
    lottieComposition: LottieComposition?
) {
    val clamped = pageOffset.coerceIn(-1f, 1f)
    val alpha = 1f - kotlin.math.abs(clamped) * 0.20f
    val parallax = 18f * clamped

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .graphicsLayer {
                this.alpha = alpha
                translationX = parallax
            }
            // Alt bottom controls (dots + butonlar + nav bar ~180dp) ile çakışmayı önle
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            HorseLottieAnimation(
                composition = lottieComposition,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .alpha(alpha)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = page.titleOverride ?: stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = semantic.onImageOverlay
        )

        Spacer(modifier = Modifier.height(14.dp))

        EngagingCallout(
            subtitleRes = page.subtitleRes,
            subtitleOverride = page.subtitleOverride,
            gradient = page.gradient
        )

        Spacer(modifier = Modifier.height(18.dp))

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

@Composable
private fun HorseLottieAnimation(
    composition: LottieComposition?,
    modifier: Modifier = Modifier
) {
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    if (composition == null) {
        // Lottie yüklenene kadar at ikonu göster
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            )
        }
    } else {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}

private data class FeatureRes(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val textRes: Int
)

private data class OnboardingPage(
    val titleRes: Int,
    val subtitleRes: Int,
    val titleOverride: String? = null,
    val subtitleOverride: String? = null,
    val gradient: List<Color>,
    val features: List<FeatureRes> = emptyList()
)

@Composable
private fun EngagingCallout(
    subtitleRes: Int,
    subtitleOverride: String? = null,
    gradient: List<Color>
) {
    val semantic = LocalSemanticColors.current
    val base  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f)
    val start = gradient.firstOrNull()?.copy(alpha = 0.24f)
        ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
    val end   = gradient.getOrNull(1)?.copy(alpha = 0.24f)
        ?: MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
    Card(
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, semantic.cardStroke),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = listOf(base, start, end)))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val subtitle = subtitleOverride ?: stringResource(id = subtitleRes)
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureBullet(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    val semantic = LocalSemanticColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                semantic.cardElevated.copy(alpha = 0.72f),
                RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun OnboardingScreenPreview() {
    MaterialTheme { OnboardingScreen(onStart = {}, onSkip = {}) }
}
