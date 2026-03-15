package com.horsegallop.feature.challenge.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.domain.challenge.model.Badge
import com.horsegallop.domain.challenge.model.BadgeType
import com.horsegallop.domain.challenge.model.Challenge
import com.horsegallop.domain.challenge.model.ChallengeType
import com.horsegallop.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.ui.collectAsState()
    val semantic = LocalSemanticColors.current
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { viewModel.clearError() }
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.challenge_screen_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = semantic.screenBase
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = stringResource(R.string.challenge_tab_challenges),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = stringResource(R.string.challenge_tab_badges),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> ChallengesTab(
                    loading = uiState.loadingChallenges,
                    challenges = uiState.challenges
                )
                1 -> BadgesTab(
                    loading = uiState.loadingBadges,
                    earnedBadges = uiState.badges
                )
            }
        }
    }
}

@Composable
private fun ChallengesTab(
    loading: Boolean,
    challenges: List<Challenge>
) {
    val semantic = LocalSemanticColors.current

    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        challenges.isEmpty() -> {
            ChallengeEmptyState()
        }
        else -> {
            val active = challenges.filter { !it.isCompleted }
            val completed = challenges.filter { it.isCompleted }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (active.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.challenge_tab_challenges),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(active, key = { it.id }) { challenge ->
                        ChallengeCard(challenge = challenge)
                    }
                }

                if (completed.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.challenge_completed),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = semantic.success,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(completed, key = { "done_${it.id}" }) { challenge ->
                        ChallengeCard(challenge = challenge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge) {
    val semantic = LocalSemanticColors.current
    val accentColor: Color = when {
        challenge.isCompleted -> semantic.success
        challenge.daysLeft < 3 -> semantic.warning
        else -> MaterialTheme.colorScheme.primary
    }
    val animatedProgress by animateFloatAsState(
        targetValue = challenge.progress,
        animationSpec = tween(600),
        label = "progress_${challenge.id}"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (challenge.isCompleted) {
                    Surface(
                        accentColor = accentColor,
                        label = stringResource(R.string.challenge_completed)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.challenge_days_left, challenge.daysLeft),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (challenge.description.isNotBlank()) {
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f)
            )

            Text(
                text = "${challenge.currentValue.toFormattedValue()} / ${challenge.targetValue.toFormattedValue()} ${challenge.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Surface(accentColor: Color, label: String) {
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(8.dp),
        color = accentColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ChallengeEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Text(
                text = stringResource(R.string.challenge_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.challenge_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BadgesTab(
    loading: Boolean,
    earnedBadges: List<Badge>
) {
    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val earnedTypes = earnedBadges.map { it.type }.toSet()
            val allBadgeTypes = BadgeType.entries.toList()

            if (earnedBadges.isEmpty()) {
                BadgesEmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allBadgeTypes, key = { it.name }) { badgeType ->
                        val earned = earnedBadges.find { it.type == badgeType }
                        BadgeItem(
                            badgeType = badgeType,
                            badge = earned,
                            isLocked = earned == null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgesEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🏅", style = MaterialTheme.typography.displayMedium)
            Text(
                text = stringResource(R.string.badge_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.badge_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BadgeItem(
    badgeType: BadgeType,
    badge: Badge?,
    isLocked: Boolean
) {
    val semantic = LocalSemanticColors.current
    val alphaValue = if (isLocked) 0.35f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaValue),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) semantic.cardSubtle else semantic.cardElevated
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeType.emoji(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = badgeType.localizedTitle(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
            )
            if (!isLocked && badge != null) {
                Text(
                    text = badge.earnedDate.formatEpochDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = semantic.success,
                    textAlign = TextAlign.Center
                )
            } else if (isLocked) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun Double.toFormattedValue(): String {
    return if (this == kotlin.math.floor(this)) this.toInt().toString()
    else String.format("%.1f", this)
}

private fun Long.formatEpochDate(): String = runCatching {
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(this))
}.getOrDefault("")

@Composable
private fun BadgeType.localizedTitle(): String = when (this) {
    BadgeType.FIRST_RIDE -> stringResource(R.string.badge_type_first_ride)
    BadgeType.DISTANCE_10K -> stringResource(R.string.badge_type_distance_10k)
    BadgeType.DISTANCE_50K -> stringResource(R.string.badge_type_distance_50k)
    BadgeType.DISTANCE_100K -> stringResource(R.string.badge_type_distance_100k)
    BadgeType.STREAK_7 -> stringResource(R.string.badge_type_streak_7)
    BadgeType.STREAK_30 -> stringResource(R.string.badge_type_streak_30)
    BadgeType.SPEED_DEMON -> stringResource(R.string.badge_type_speed_demon)
    BadgeType.EARLY_BIRD -> stringResource(R.string.badge_type_early_bird)
    BadgeType.MONTHLY_CHAMPION -> stringResource(R.string.badge_type_monthly_champion)
    BadgeType.SOCIAL_RIDER -> stringResource(R.string.badge_type_social_rider)
}

private fun BadgeType.emoji(): String = when (this) {
    BadgeType.FIRST_RIDE -> "🐴"
    BadgeType.DISTANCE_10K -> "🏃"
    BadgeType.DISTANCE_50K -> "🌟"
    BadgeType.DISTANCE_100K -> "🏆"
    BadgeType.STREAK_7 -> "🔥"
    BadgeType.STREAK_30 -> "💎"
    BadgeType.SPEED_DEMON -> "⚡"
    BadgeType.EARLY_BIRD -> "🌅"
    BadgeType.MONTHLY_CHAMPION -> "🥇"
    BadgeType.SOCIAL_RIDER -> "🗺️"
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ChallengesTabPreview() {
    val now = System.currentTimeMillis()
    val fakeChallenges = listOf(
        Challenge(
            id = "1",
            type = ChallengeType.MONTHLY_DISTANCE,
            title = "Mayıs Mesafe Meydan Okuma",
            description = "Bu ay toplam 50 km sürüş yap",
            targetValue = 50.0,
            currentValue = 23.5,
            unit = "km",
            startDate = now - 10 * 86400_000L,
            endDate = now + 5 * 86400_000L,
            isCompleted = false,
            reward = BadgeType.MONTHLY_CHAMPION
        ),
        Challenge(
            id = "2",
            type = ChallengeType.WEEKLY_RIDES,
            title = "Haftalık Sürüş",
            description = "Bu hafta 3 sürüş tamamla",
            targetValue = 3.0,
            currentValue = 3.0,
            unit = "sürüş",
            startDate = now - 6 * 86400_000L,
            endDate = now + 1 * 86400_000L,
            isCompleted = true,
            reward = BadgeType.STREAK_7
        )
    )
    MaterialTheme {
        ChallengesTab(loading = false, challenges = fakeChallenges)
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgesTabPreview() {
    val now = System.currentTimeMillis()
    val fakeBadges = listOf(
        Badge(
            id = "b1",
            type = BadgeType.FIRST_RIDE,
            earnedDate = now - 30 * 86400_000L,
            title = "İlk Sürüş",
            description = "İlk sürüşünü tamamladın!"
        ),
        Badge(
            id = "b2",
            type = BadgeType.DISTANCE_10K,
            earnedDate = now - 15 * 86400_000L,
            title = "10km",
            description = "Toplam 10km sürüş"
        ),
        Badge(
            id = "b3",
            type = BadgeType.STREAK_7,
            earnedDate = now - 5 * 86400_000L,
            title = "7 Gün Seri",
            description = "7 gün üst üste sürüş"
        )
    )
    MaterialTheme {
        BadgesTab(loading = false, earnedBadges = fakeBadges)
    }
}
