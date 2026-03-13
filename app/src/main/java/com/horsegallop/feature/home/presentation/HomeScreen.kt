@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.horsegallop.core.components.ViewAllButton
import com.horsegallop.navigation.Dest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.horsegallop.core.components.QuickActionCard
import com.horsegallop.core.components.StatCard
import com.horsegallop.core.components.ActivityItem
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.HomeDashboardSkeleton
import com.horsegallop.domain.horse.model.HorseTip
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun HomeScreen(
  currentRoute: String? = null,
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onOpenTrainingPlans: () -> Unit = {},
  onOpenEquestrianAgenda: () -> Unit = {},
  onProfileClick: () -> Unit,
  onOpenRideDetail: (String) -> Unit = {},
  onViewAllActivities: (() -> Unit)? = null,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val uiState by viewModel.ui.collectAsState()
  HomeDashboard(
    onStartRide = onStartRide,
    onViewBarns = onViewBarns,
    onOpenTrainingPlans = onOpenTrainingPlans,
    onOpenEquestrianAgenda = onOpenEquestrianAgenda,
    onProfileClick = onProfileClick,
    onOpenRideDetail = onOpenRideDetail,
    onViewAllActivities = onViewAllActivities,
    uiState = uiState,
    onRetry = { viewModel.refresh() }
  )
}

@Preview(showBackground = true, name = "HomeScreen")
@Composable
private fun PreviewHomeScreen() {
  MaterialTheme {
    HomeScreen(
      onStartRide = {},
      onViewBarns = {},
      onOpenTrainingPlans = {},
      onProfileClick = {}
    )
  }
}

@Composable
private fun HomeDashboard(
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onOpenTrainingPlans: () -> Unit,
  onOpenEquestrianAgenda: () -> Unit = {},
  onProfileClick: () -> Unit,
  onOpenRideDetail: (String) -> Unit = {},
  onViewAllActivities: (() -> Unit)? = null,
  uiState: HomeUiState = HomeUiState(loading = false),
  onRetry: () -> Unit = {}
) {
  val semantic = LocalSemanticColors.current

  if (uiState.loading) {
    HomeDashboardSkeleton()
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
              MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f),
              semantic.screenBase
            )
          )
        ),
      contentPadding = PaddingValues(
        start = dimensionResource(id = com.horsegallop.R.dimen.padding_screen_horizontal),
        end = dimensionResource(id = com.horsegallop.R.dimen.padding_screen_horizontal),
        top = dimensionResource(id = com.horsegallop.R.dimen.padding_screen_vertical),
        bottom = dimensionResource(id = com.horsegallop.R.dimen.padding_screen_vertical)
      ),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.R.dimen.section_spacing_md))
    ) {
      item {
        WelcomeHeader(
          onProfileClick = onProfileClick,
          cardColor = semantic.cardElevated,
          title = uiState.heroTitle,
          subtitle = uiState.heroSubtitle
        )
      }

      item {
        QuickActionsSection(
          onStartRide = onStartRide,
          onViewBarns = onViewBarns,
          onOpenTrainingPlans = onOpenTrainingPlans,
          onOpenEquestrianAgenda = onOpenEquestrianAgenda
        )
      }
      
      item {
        StatsOverviewSection(totalRides = uiState.totalRides, totalDistance = uiState.totalDistance)
      }

      if (uiState.error != null) {
        item {
          ErrorStateCard(
            message = uiState.error,
            onRetry = onRetry
          )
        }
      }
      
      item {
        RecentActivitySection(
          activities = uiState.activities,
          onOpenRideDetail = onOpenRideDetail,
          onViewAllActivities = onViewAllActivities
        )
      }
      
      item {
        TipsSection(tip = uiState.currentTip)
      }
    }
  }
}

@Composable
private fun ErrorStateCard(
  message: String,
  onRetry: () -> Unit
) {
  val semantic = LocalSemanticColors.current
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = semantic.calloutErrorContainer),
    border = BorderStroke(1.dp, semantic.calloutBorderError),
    shape = RoundedCornerShape(18.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = semantic.calloutOnContainer
      )
      OutlinedButton(onClick = onRetry) {
        Text(text = stringResource(id = R.string.retry))
      }
    }
  }
}

@Composable
private fun WelcomeHeader(
  onProfileClick: () -> Unit,
  cardColor: androidx.compose.ui.graphics.Color,
  title: String?,
  subtitle: String?
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = com.horsegallop.R.dimen.spacing_md)),
    colors = CardDefaults.cardColors(
      containerColor = cardColor
    ),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.R.dimen.radius_xl))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
              MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            )
          )
        )
        .padding(dimensionResource(id = com.horsegallop.R.dimen.padding_card_xl))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = title ?: stringResource(id = R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.R.dimen.spacing_sm)))
          Text(
            text = subtitle ?: stringResource(id = R.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(id = com.horsegallop.R.dimen.icon_xxl)),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun QuickActionsSection(
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onOpenTrainingPlans: () -> Unit,
  onOpenEquestrianAgenda: () -> Unit = {}
) {
  Column {
    Text(
      text = stringResource(id = com.horsegallop.R.string.quick_actions_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
    ) {
      QuickActionCard(
        title = stringResource(id = R.string.qa_start_ride_title),
        subtitle = stringResource(id = R.string.qa_start_ride_subtitle),
        icon = Icons.Filled.PlayArrow,
        color = MaterialTheme.colorScheme.primary,
        onClick = onStartRide,
        modifier = Modifier.weight(1f)
      )
      QuickActionCard(
        title = stringResource(id = R.string.qa_view_barns_title),
        subtitle = stringResource(id = R.string.qa_view_barns_subtitle),
        icon = Icons.Filled.LocationOn,
        color = MaterialTheme.colorScheme.secondary,
        onClick = onViewBarns,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.R.dimen.spacing_md)))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
    ) {
      QuickActionCard(
        title = stringResource(id = R.string.qa_training_title),
        subtitle = stringResource(id = R.string.qa_training_subtitle),
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        color = MaterialTheme.colorScheme.tertiary,
        onClick = onOpenTrainingPlans,
        modifier = Modifier.weight(1f)
      )
      QuickActionCard(
        title = stringResource(id = R.string.qa_equestrian_agenda_title),
        subtitle = stringResource(id = R.string.qa_equestrian_agenda_subtitle),
        icon = Icons.Filled.EmojiEvents,
        color = MaterialTheme.colorScheme.error,
        onClick = onOpenEquestrianAgenda,
        modifier = Modifier.weight(1f)
      )
    }
  }
}


@Composable
private fun StatsOverviewSection(totalRides: String, totalDistance: String) {
  Column {
    Text(
      text = stringResource(id = com.horsegallop.R.string.stats_yours_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
    ) {
      StatCard(
        title = stringResource(id = R.string.stats_total_rides),
        value = totalRides,
        subtitle = stringResource(id = R.string.stats_hours_suffix),
        icon = Icons.Filled.Timer,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f),
        onClick = { /* Navigate to detailed stats */ }
      )
      StatCard(
        title = stringResource(id = R.string.stats_distance),
        value = totalDistance,
        subtitle = stringResource(id = R.string.stats_km_suffix),
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
  activities: List<ActivityUi> = emptyList(),
  onOpenRideDetail: (String) -> Unit = {},
  onViewAllActivities: (() -> Unit)? = null
) {
  val semantic = LocalSemanticColors.current
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(id = com.horsegallop.R.string.recent_activity_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      ViewAllButton(
        onClick = { onViewAllActivities?.invoke() }
      )
    }
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
      shape = RoundedCornerShape(20.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        val visibleActivities = activities.take(3)
        if (visibleActivities.isEmpty()) {
          Text(
            text = stringResource(id = R.string.no_activity_data),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
          )
        } else {
          visibleActivities.forEachIndexed { index, activity ->
            ActivityItem(
              title = activity.title ?: stringResource(id = R.string.ride_default_title),
              subtitle = stringResource(id = R.string.activity_subtitle_format, activity.dateLabel, activity.timeLabel),
              duration = stringResource(id = R.string.activity_duration_minutes, activity.durationMin),
              distance = stringResource(id = R.string.activity_distance_km, activity.distanceKm),
              icon = Icons.AutoMirrored.Filled.DirectionsRun,
              onClick = {
                if (activity.id.isNotBlank()) {
                  onOpenRideDetail(activity.id)
                }
              }
            )
            if (index < visibleActivities.lastIndex) {
              HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
              )
            }
          }
        }
      }
    }
  }
}


@Composable
private fun TipsSection(tip: HorseTip? = null) {
  val semantic = LocalSemanticColors.current
  Column {
    Text(
      text = stringResource(id = com.horsegallop.R.string.riding_tips_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(top = dimensionResource(id = com.horsegallop.R.dimen.spacing_sm), bottom = dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
    )

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.R.dimen.radius_lg)),
      border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
      Row(
        modifier = Modifier.padding(dimensionResource(id = com.horsegallop.R.dimen.padding_card_md)),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.R.dimen.spacing_md))
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Filled.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (tip != null) tip.title else stringResource(id = com.horsegallop.R.string.tip_safe_riding_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (tip != null) tip.body else stringResource(id = com.horsegallop.R.string.tip_safe_riding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = tip?.category?.let { horseTipCategoryLabel(it) } ?: stringResource(id = com.horsegallop.R.string.horse_tip_category_default),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.R.dimen.section_spacing_md)))
  }
}

@Composable
private fun horseTipCategoryLabel(category: String): String {
    val resId = when (category.lowercase().trim()) {
        "breed"      -> R.string.horse_tip_category_breed
        "physiology" -> R.string.horse_tip_category_physiology
        "anatomy"    -> R.string.horse_tip_category_anatomy
        "care"       -> R.string.horse_tip_category_care
        "speed"      -> R.string.horse_tip_category_speed
        "vision"     -> R.string.horse_tip_category_vision
        "behavior"   -> R.string.horse_tip_category_behavior
        else         -> R.string.horse_tip_category_default
    }
    return stringResource(resId)
}

// Preview Components
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeDashboardPreview() {
  MaterialTheme {
    HomeDashboard(
      onStartRide = {},
      onViewBarns = {},
      onOpenTrainingPlans = {},
      onProfileClick = {}
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeDashboardSkeletonPreview() {
  MaterialTheme {
    HomeDashboardSkeleton()
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeScreenPreview() {
  MaterialTheme {
    HomeScreen(
      currentRoute = Dest.Home.route,
      onStartRide = {},
      onViewBarns = {},
      onProfileClick = {}
    )
  }
}
