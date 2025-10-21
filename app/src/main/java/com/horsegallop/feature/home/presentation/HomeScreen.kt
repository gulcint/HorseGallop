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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun HomeScreen(
  onStartRide: () -> Unit,
  onViewBarns: () -> Unit,
  onBarnSelected: (BarnUi) -> Unit
) {
  Scaffold(
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
          label = { Text("Home") },
          selected = true,
          onClick = { /* Already on home */ }
        )
        NavigationBarItem(
          icon = { Icon(Icons.Filled.DirectionsRun, contentDescription = "Ride") },
          label = { Text("Ride") },
          selected = false,
          onClick = onStartRide
        )
        NavigationBarItem(
          icon = { Icon(Icons.Filled.List, contentDescription = "Barns") },
          label = { Text("Barns") },
          selected = false,
          onClick = onViewBarns
        )
      }
    }
  ) { _ ->
    HomeDashboard(onStartRide = onStartRide, onViewBarns = onViewBarns)
  }
}

@Composable
private fun HomeDashboard(onStartRide: () -> Unit, onViewBarns: () -> Unit) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    contentPadding = PaddingValues(
      start = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal),
      end = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal),
      top = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_vertical) + dimensionResource(id = com.horsegallop.core.R.dimen.spacing_lg),
      bottom = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_vertical)
    ),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md))
  ) {
    item {
      WelcomeHeader()
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
    
    // Bottom padding for better scrolling
    item {
      Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.bottom_navigation_height)))
    }
  }
}

@Composable
private fun WelcomeHeader() {
  Card(
    modifier = Modifier.fillMaxWidth(),
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
        Icon(
          Icons.Filled.TrendingUp,
          contentDescription = null,
          modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xxl)),
          tint = MaterialTheme.colorScheme.primary
        )
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
private fun QuickActionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_card_md))
      .clickable { onClick() },
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md))
    ) {
      Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
      ) {
        Icon(
          icon,
          contentDescription = null,
          modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_lg)),
          tint = color
        )
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
        }
      }
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
private fun StatCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier.then(
      if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
  ) {
    Column(
      modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_md)),
        tint = color
      )
      Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs))
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
          onClick = { /* Navigate to activity details */ }
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
        ActivityItem(
          title = "Akşam Turu",
          subtitle = "Dün, 18:15",
          duration = "1 saat 20 dk",
          distance = "12.5 km",
          onClick = { /* Navigate to activity details */ }
        )
      }
    }
  }
}

@Composable
private fun ActivityItem(
  title: String,
  subtitle: String,
  duration: String,
  distance: String,
  onClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(
        if (onClick != null) Modifier.clickable { onClick() } else Modifier
      ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
    ) {
      Box(
        modifier = Modifier
          .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xl))
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Filled.DirectionsRun,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_sm))
        )
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
      }
    }
    Column(
      horizontalAlignment = Alignment.End
    ) {
      Text(
        text = duration,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = distance,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
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
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    item {
      WelcomeHeaderSkeleton()
    }
    
    item {
      QuickActionsSkeleton()
    }
    
    item {
      StatsOverviewSkeleton()
    }
    
    item {
      RecentActivitySkeleton()
    }
    
    item {
      TipsSkeleton()
    }
    
    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun WelcomeHeaderSkeleton() {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shimmer(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(20.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .background(Color.Gray.copy(alpha = 0.3f))
    )
  }
}

@Composable
private fun QuickActionsSkeleton() {
  Column {
    Box(
      modifier = Modifier
        .width(120.dp)
        .height(24.dp)
        .shimmer()
        .background(Color.Gray.copy(alpha = 0.3f))
        .clip(RoundedCornerShape(4.dp))
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(2) {
        Card(
          modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .shimmer(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Gray.copy(alpha = 0.3f))
          )
        }
      }
    }
  }
}

@Composable
private fun StatsOverviewSkeleton() {
  Column {
    Box(
      modifier = Modifier
        .width(140.dp)
        .height(24.dp)
        .shimmer()
        .background(Color.Gray.copy(alpha = 0.3f))
        .clip(RoundedCornerShape(4.dp))
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      repeat(2) {
        Card(
          modifier = Modifier
            .weight(1f)
            .shimmer(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(16.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp)
              .background(Color.Gray.copy(alpha = 0.3f))
          )
        }
      }
    }
  }
}

@Composable
private fun RecentActivitySkeleton() {
  Column {
    Box(
      modifier = Modifier
        .width(120.dp)
        .height(24.dp)
        .shimmer()
        .background(Color.Gray.copy(alpha = 0.3f))
        .clip(RoundedCornerShape(4.dp))
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shimmer(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        repeat(2) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .shimmer()
                  .background(Color.Gray.copy(alpha = 0.3f))
                  .clip(CircleShape)
              )
              Column {
                Box(
                  modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
                    .shimmer()
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                  modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
                    .shimmer()
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(4.dp))
                )
              }
            }
            Column(
              horizontalAlignment = Alignment.End
            ) {
              Box(
                modifier = Modifier
                  .width(40.dp)
                  .height(14.dp)
                  .shimmer()
                  .background(Color.Gray.copy(alpha = 0.3f))
                  .clip(RoundedCornerShape(4.dp))
              )
              Spacer(modifier = Modifier.height(4.dp))
              Box(
                modifier = Modifier
                  .width(50.dp)
                  .height(12.dp)
                  .shimmer()
                  .background(Color.Gray.copy(alpha = 0.3f))
                  .clip(RoundedCornerShape(4.dp))
              )
            }
          }
          if (it == 0) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun TipsSkeleton() {
  Column {
    Box(
      modifier = Modifier
        .width(140.dp)
        .height(24.dp)
        .shimmer()
        .background(Color.Gray.copy(alpha = 0.3f))
        .clip(RoundedCornerShape(4.dp))
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shimmer(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp)
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .shimmer()
            .background(Color.Gray.copy(alpha = 0.3f))
            .clip(CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .width(100.dp)
              .height(16.dp)
              .shimmer()
              .background(Color.Gray.copy(alpha = 0.3f))
              .clip(RoundedCornerShape(4.dp))
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(14.dp)
              .shimmer()
              .background(Color.Gray.copy(alpha = 0.3f))
              .clip(RoundedCornerShape(4.dp))
          )
        }
      }
    }
  }
}

// Preview Components
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun HomeDashboardPreview() {
  MaterialTheme {
    HomeDashboard(
      onStartRide = {},
      onViewBarns = {}
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
      onStartRide = {},
      onViewBarns = {},
      onBarnSelected = {}
    )
  }
}


