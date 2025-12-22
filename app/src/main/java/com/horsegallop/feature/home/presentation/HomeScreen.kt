@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.horsegallop.feature.barn.presentation.BarnListScreen
import com.horsegallop.feature.barn.domain.model.BarnUi
import com.horsegallop.feature.ride.presentation.RideTrackingScreen
import com.valentinilk.shimmer.shimmer
import com.horsegallop.compose.QuickActionCard
import com.horsegallop.compose.StatCard
import com.horsegallop.compose.ActivityItem
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
  currentRoute: String? = null,
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onProfileClick: () -> Unit,
  onViewAllActivities: (() -> Unit)? = null,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val uiState by viewModel.ui.collectAsState()
  HomeDashboard(
    onStartRide = onStartRide,
    onViewBarns = onViewBarns,
    onProfileClick = onProfileClick,
    onViewAllActivities = onViewAllActivities,
    uiState = uiState
  )
}

@Preview(showBackground = true, name = "HomeScreen")
@Composable
private fun PreviewHomeScreen() {
  MaterialTheme {
    HomeScreen(
      onStartRide = {},
      onViewBarns = {},
      onProfileClick = {}
    )
  }
}

@Composable
private fun HomeDashboard(
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onProfileClick: () -> Unit,
  onViewAllActivities: (() -> Unit)? = null,
  uiState: HomeUiState = HomeUiState(loading = false)
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize(),
    contentPadding = PaddingValues(
      start = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal),
      end = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal),
      top = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_vertical),
      bottom = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_vertical)
    ),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md))
  ) {
    item {
      WelcomeHeader(onProfileClick = onProfileClick)
    }
    
    item {
      QuickActionsSection(onStartRide = onStartRide, onViewBarns = onViewBarns)
    }
    
    item {
      StatsOverviewSection(totalRides = uiState.totalRides, totalDistance = uiState.totalDistance)
    }
    
    item {
      if (uiState.loading) {
        RecentActivitySkeleton()
      } else {
        val activities = if (uiState.activities.isEmpty()) listOf(
          ActivityUi(
            title = stringResource(id = com.horsegallop.core.R.string.activity_morning_ride_title),
            dateLabel = stringResource(id = com.horsegallop.core.R.string.activity_morning_ride_subtitle).substringBefore(", "),
            timeLabel = stringResource(id = com.horsegallop.core.R.string.activity_morning_ride_subtitle).substringAfter(", "),
            durationMin = 45,
            distanceKm = 8.2
          ),
          ActivityUi(
            title = stringResource(id = com.horsegallop.core.R.string.activity_evening_ride_title),
            dateLabel = stringResource(id = com.horsegallop.core.R.string.activity_evening_ride_subtitle).substringBefore(", "),
            timeLabel = stringResource(id = com.horsegallop.core.R.string.activity_evening_ride_subtitle).substringAfter(", "),
            durationMin = 80,
            distanceKm = 12.5
          )
        ) else uiState.activities
        RecentActivitySection(activities = activities, onViewAllActivities = onViewAllActivities)
      }
    }
    
    item {
      TipsSection()
    }
    
    // Removed manual bottom spacer to avoid double padding with Scaffold's innerPadding
  }
}

@Composable
private fun WelcomeHeader(onProfileClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
              MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
          )
        )
        .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_xl))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = stringResource(id = com.horsegallop.core.R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
          Text(
            text = stringResource(id = com.horsegallop.core.R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xxl)),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun QuickActionsSection(onStartRide: () -> Unit, onViewBarns: () -> Unit) {
  Column {
    Text(
      text = stringResource(id = com.horsegallop.core.R.string.quick_actions_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      QuickActionCard(
        title = stringResource(id = com.horsegallop.core.R.string.qa_start_ride_title),
        subtitle = stringResource(id = com.horsegallop.core.R.string.qa_start_ride_subtitle),
        icon = Icons.Filled.PlayArrow,
        color = MaterialTheme.colorScheme.primary,
        onClick = onStartRide,
        modifier = Modifier.weight(1f)
      )
      QuickActionCard(
        title = stringResource(id = com.horsegallop.core.R.string.qa_view_barns_title),
        subtitle = stringResource(id = com.horsegallop.core.R.string.qa_view_barns_subtitle),
        icon = Icons.Filled.LocationOn,
        color = MaterialTheme.colorScheme.secondary,
        onClick = onViewBarns,
        modifier = Modifier.weight(1f)
      )
    }
  }
}


@Composable
private fun StatsOverviewSection(totalRides: String, totalDistance: String) {
  Column {
    Text(
      text = stringResource(id = com.horsegallop.core.R.string.stats_yours_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      StatCard(
        title = stringResource(id = com.horsegallop.core.R.string.stats_total_rides),
        value = totalRides,
        subtitle = stringResource(id = com.horsegallop.core.R.string.stats_hours_suffix),
        icon = Icons.Filled.Timer,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f),
        onClick = { /* Navigate to detailed stats */ }
      )
      StatCard(
        title = stringResource(id = com.horsegallop.core.R.string.stats_distance),
        value = totalDistance,
        subtitle = stringResource(id = com.horsegallop.core.R.string.stats_km_suffix),
        icon = Icons.Filled.Speed,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.weight(1f),
        onClick = { /* Navigate to distance stats */ }
      )
    }
  }
}


@Composable
private fun RecentActivitySection(
  activities: List<ActivityUi> = listOf(
  ActivityUi(
    title = stringResource(id = com.horsegallop.core.R.string.activity_morning_ride_title),
    dateLabel = stringResource(id = com.horsegallop.core.R.string.activity_morning_ride_subtitle).substringBefore(", "),
    timeLabel = stringResource(id = com.horsegallop.core.R.string.activity_morning_ride_subtitle).substringAfter(", "),
    durationMin = 45,
    distanceKm = 8.2
  ),
  ActivityUi(
    title = stringResource(id = com.horsegallop.core.R.string.activity_evening_ride_title),
    dateLabel = stringResource(id = com.horsegallop.core.R.string.activity_evening_ride_subtitle).substringBefore(", "),
    timeLabel = stringResource(id = com.horsegallop.core.R.string.activity_evening_ride_subtitle).substringAfter(", "),
    durationMin = 80,
    distanceKm = 12.5
  )
),
  onViewAllActivities: (() -> Unit)? = null
) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(id = com.horsegallop.core.R.string.recent_activity_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        onClick = { onViewAllActivities?.invoke() }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "View All",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(20.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        val a1 = activities.getOrNull(0)
        if (a1 != null) {
          ActivityItem(
            title = a1.title,
            subtitle = stringResource(id = com.horsegallop.core.R.string.activity_subtitle_format, a1.dateLabel, a1.timeLabel),
            duration = stringResource(id = com.horsegallop.core.R.string.activity_duration_minutes, a1.durationMin),
            distance = stringResource(id = com.horsegallop.core.R.string.activity_distance_km, a1.distanceKm),
            icon = Icons.AutoMirrored.Filled.DirectionsRun
          )
        }
        HorizontalDivider(
          modifier = Modifier.padding(vertical = 4.dp), // Reduced padding, divider makes it clean
          thickness = 1.dp,
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
        val a2 = activities.getOrNull(1)
        if (a2 != null) {
          ActivityItem(
            title = a2.title,
            subtitle = stringResource(id = com.horsegallop.core.R.string.activity_subtitle_format, a2.dateLabel, a2.timeLabel),
            duration = stringResource(id = com.horsegallop.core.R.string.activity_duration_minutes, a2.durationMin),
            distance = stringResource(id = com.horsegallop.core.R.string.activity_distance_km, a2.distanceKm),
            icon = Icons.AutoMirrored.Filled.DirectionsRun
          )
        }
      }
    }
  }
}


@Composable
private fun TipsSection() {
  Column {
    Text(
      text = stringResource(id = com.horsegallop.core.R.string.riding_tips_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(top = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm), bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    )
    
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { /* Navigate to tips section */ },
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
    ) {
      Row(
        modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
      ) {
        Icon(
          Icons.Filled.Lightbulb,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_md))
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(id = com.horsegallop.core.R.string.tip_safe_riding_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
          Text(
            text = stringResource(id = com.horsegallop.core.R.string.tip_safe_riding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md)))
  }
}

private data class TabItem(val label: String, val icon: ImageVector)

// Shimmer Skeleton Components
@Composable
private fun HomeDashboardSkeleton() {
  com.horsegallop.compose.HomeDashboardSkeleton()
}

@Composable
private fun WelcomeHeaderSkeleton() {
  com.horsegallop.compose.WelcomeHeaderSkeleton()
}

@Composable
private fun QuickActionsSkeleton() {
  com.horsegallop.compose.QuickActionsSkeleton()
}

@Composable
private fun StatsOverviewSkeleton() {
  com.horsegallop.compose.StatsOverviewSkeleton()
}

@Composable
private fun RecentActivitySkeleton() {
  com.horsegallop.compose.RecentActivitySkeleton()
}

@Composable
private fun TipsSkeleton() {
  com.horsegallop.compose.TipsSkeleton()
}

// Preview Components
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeDashboardPreview() {
  MaterialTheme {
    HomeDashboard(
      onStartRide = {},
      onViewBarns = {},
      onProfileClick = {}
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeDashboardSkeletonPreview() {
  MaterialTheme {
    com.horsegallop.compose.HomeDashboardSkeleton()
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeScreenPreview() {
  MaterialTheme {
    HomeScreen(
      currentRoute = com.horsegallop.navigation.Dest.Home.route,
      onStartRide = {},
      onViewBarns = {},
      onProfileClick = {}
    )
  }
}
