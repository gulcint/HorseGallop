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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onStart: () -> Unit = {}, onSkip: () -> Unit = {}) {
    val pages: List<OnboardingPage> = remember {
        listOf(
            OnboardingPage(
                titleRes = R.string.onboarding_title_ranch,
                subtitleRes = R.string.onboarding_subtitle_ranch,
                gradient = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_packages,
                subtitleRes = R.string.onboarding_subtitle_packages,
                gradient = listOf(Color(0xFF0BA360), Color(0xFF3CBA92))
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_boarding,
                subtitleRes = R.string.onboarding_subtitle_boarding,
                gradient = listOf(Color(0xFFFF512F), Color(0xFFF09819))
            ),
            OnboardingPage(
                titleRes = R.string.onboarding_title_cafe,
                subtitleRes = R.string.onboarding_subtitle_cafe,
                gradient = listOf(Color(0xFF614385), Color(0xFF516395))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        TextButton(onClick = onSkip) {
                            Text(stringResource(R.string.onboarding_skip))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Progress text
            Text(
                text = stringResource(
                    id = R.string.onboarding_progress,
                    pagerState.currentPage + 1,
                    pages.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicators
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { index ->
                    val isActive: Boolean = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            .size(if (isActive) 10.dp else 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSkip, enabled = pagerState.currentPage < pages.lastIndex) {
                    Text(stringResource(R.string.onboarding_skip))
                }

                val isLast: Boolean = pagerState.currentPage == pages.lastIndex
                Button(
                    onClick = {
                        if (isLast) onStart() else {
                            scope.launch {
                                val next = (pagerState.currentPage + 1).coerceAtMost(pages.lastIndex)
                                pagerState.animateScrollToPage(next)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isLast) stringResource(R.string.onboarding_start)
                        else stringResource(R.string.onboarding_next)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero area placeholder with gradient (could host Lottie/illustration later)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(page.gradient))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(page.subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private data class OnboardingPage(
    val titleRes: Int,
    val subtitleRes: Int,
    val gradient: List<Color>
)


