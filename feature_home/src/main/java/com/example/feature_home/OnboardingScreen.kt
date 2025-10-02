package com.example.feature_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onStart: () -> Unit = {}, onSkip: () -> Unit = {}) {
    val pages: List<OnboardingPage> = remember {
        listOf(
            OnboardingPage(
                titleRes = R.string.onboarding_title_ranch,
                subtitleRes = R.string.onboarding_subtitle_ranch,
                gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                imageUrl = "https://images.unsplash.com/photo-1517971052751-77380514b35e?q=80&w=1600&auto=format&fit=crop"
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_packages,
                subtitleRes = R.string.onboarding_subtitle_packages,
                gradient = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
                imageUrl = "https://images.unsplash.com/photo-1563481088221-49923125159e?q=80&w=1600&auto=format&fit=crop"
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_boarding,
                subtitleRes = R.string.onboarding_subtitle_boarding,
                gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                imageUrl = "https://images.unsplash.com/photo-1586776360433-d1e69679d171?q=80&w=1600&auto=format&fit=crop"
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_cafe,
                subtitleRes = R.string.onboarding_subtitle_cafe,
                gradient = listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
                imageUrl = "https://images.unsplash.com/photo-1555937020-39b8121700c1?q=80&w=1600&auto=format&fit=crop"
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
        // Back button exits app on onboarding
        val activity = LocalContext.current as? Activity
        BackHandler(enabled = true) { activity?.finish() }
        // Pager - Full screen
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // Progress indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        ) {
            Text(
                text = stringResource(
                    id = R.string.onboarding_progress,
                    pagerState.currentPage + 1,
                    pages.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
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
                    TextButton(onClick = onSkip) { Text(stringResource(R.string.onboarding_skip)) }
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
                        modifier = Modifier.height(48.dp)
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
private fun OnboardingPageContent(page: OnboardingPage) {
    val hasUrl: Boolean = !page.imageUrl.isNullOrBlank()
    var showImage by remember(page.imageUrl) { mutableStateOf(hasUrl) }
    val arrangement: Arrangement.Vertical = if (showImage) Arrangement.SpaceBetween else Arrangement.Center
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = arrangement,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showImage) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val ctx = LocalContext.current
                val request = remember(page.imageUrl) {
                    ImageRequest.Builder(ctx)
                        .data(page.imageUrl)
                        .crossfade(true)
                        .diskCacheKey(page.imageUrl ?: "")
                        .memoryCacheKey(page.imageUrl ?: "")
                        .build()
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    SubcomposeAsyncImage(
                        model = request,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                            else -> { showImage = false }
                        }
                    }
                    // Subtle bottom gradient overlay to enhance perceived quality
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0x14000000)),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                }
            }
        }

        // Title and subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(page.subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )
        }

        if (showImage) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Removed visual placeholder: if image fails, layout centers text/buttons and hides the image card

private data class OnboardingPage(
    val titleRes: Int,
    val subtitleRes: Int,
    val gradient: List<Color>,
    val imageUrl: String? = null
)


