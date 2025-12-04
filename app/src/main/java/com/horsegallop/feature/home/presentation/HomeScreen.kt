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

@Composable
fun HomeScreen(
  currentRoute: String? = null,
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onProfileClick: () -> Unit
) {
  HomeDashboard(onStartRide = onStartRide, onViewBarns = onViewBarns, onProfileClick = onProfileClick)
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
private fun HomeDashboard(onStartRide: () -> Unit, onViewBarns: () -> Unit, onProfileClick: () -> Unit) {
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
      StatsOverviewSection()
    }
    
    item {
      RecentActivitySection()
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
            text = "🐴 Hoş Geldiniz!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
          Text(
            text = "Binicilik serüveninize devam edin",
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
      text = "Hızlı İşlemler",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      QuickActionCard(
        title = "Sürüşe Başla",
        subtitle = "Yeni bir binicilik deneyimi",
        icon = Icons.Filled.PlayArrow,
        color = MaterialTheme.colorScheme.primary,
        onClick = onStartRide,
        modifier = Modifier.weight(1f)
      )
      QuickActionCard(
        title = "Çiftlikleri Görüntüle",
        subtitle = "Yakındaki çiftlikleri keşfet",
        icon = Icons.Filled.LocationOn,
        color = MaterialTheme.colorScheme.secondary,
        onClick = onViewBarns,
        modifier = Modifier.weight(1f)
      )
    }
  }
}


@Composable
private fun StatsOverviewSection() {
  Column {
    Text(
      text = "İstatistikleriniz",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      StatCard(
        title = "Toplam Sürüş",
        value = "12",
        subtitle = "saat",
        icon = Icons.Filled.Timer,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f),
        onClick = { /* Navigate to detailed stats */ }
      )
      StatCard(
        title = "Mesafe",
        value = "45.2",
        subtitle = "km",
        icon = Icons.Filled.Speed,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.weight(1f),
        onClick = { /* Navigate to distance stats */ }
      )
    }
  }
}


@Composable
private fun RecentActivitySection() {
  Column {
    Text(
      text = "Son Aktiviteler",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    )
    
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
    ) {
      Column(
        modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md))
      ) {
        ActivityItem(
          title = "Sabah Sürüşü",
          subtitle = "Bugün, 08:30",
          duration = "45 dk",
          distance = "8.2 km",
          icon = Icons.AutoMirrored.Filled.DirectionsRun
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
        ActivityItem(
          title = "Akşam Turu",
          subtitle = "Dün, 18:15",
          duration = "1 saat 20 dk",
          distance = "12.5 km",
          icon = Icons.AutoMirrored.Filled.DirectionsRun
        )
      }
    }
  }
}


@Composable
private fun TipsSection() {
  Column {
    Text(
      text = "Binicilik İpuçları",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
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
            text = "Güvenli Binicilik",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
          Text(
            text = "Her zaman kask takın ve güvenlik ekipmanlarınızı kontrol edin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
          )
        }
      }
    }
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
