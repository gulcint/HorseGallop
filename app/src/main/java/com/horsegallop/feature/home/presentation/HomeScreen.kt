@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
fun HomeScreen(onBarnSelected: (BarnUi) -> Unit) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf(
    TabItem("Home", Icons.Filled.Home),
    TabItem("Ride", Icons.Filled.DirectionsRun),
    TabItem("Barns", Icons.Filled.List)
  )

  Scaffold(
    bottomBar = {
      NavigationBar {
        tabs.forEachIndexed { index, item ->
          NavigationBarItem(
            icon = { Icon(item.icon, contentDescription = item.label) },
            label = { Text(item.label) },
            selected = selectedTab == index,
            onClick = { selectedTab = index }
          )
        }
      }
    }
  ) { _ ->
    when (selectedTab) {
      0 -> HomeDashboard(onStartRide = { selectedTab = 1 }, onViewBarns = { selectedTab = 2 })
      1 -> RideTrackingScreen(viewModel = com.horsegallop.feature.ride.presentation.RideTrackingViewModel())
      else -> BarnListScreen(onBarnClick = onBarnSelected)
    }
  }
}

@Composable
private fun HomeDashboard(onStartRide: () -> Unit, onViewBarns: () -> Unit) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      top = 16.dp,
      bottom = 16.dp
    ),
    verticalArrangement = Arrangement.spacedBy(20.dp)
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
      Spacer(modifier = Modifier.height(80.dp))
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
    shape = RoundedCornerShape(20.dp)
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
        .padding(24.dp)
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
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Binicilik serüveninize devam edin",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
        Icon(
          Icons.Filled.TrendingUp,
          contentDescription = null,
          modifier = Modifier.size(48.dp),
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
      modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        title = "Ahırları Görüntüle",
        subtitle = "Yakındaki ahırları keşfet",
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
      .height(120.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp),
    onClick = onClick
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
      ) {
        Icon(
          icon,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
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
      modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      StatCard(
        title = "Toplam Sürüş",
        value = "12",
        subtitle = "saat",
        icon = Icons.Filled.Timer,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Mesafe",
        value = "45.2",
        subtitle = "km",
        icon = Icons.Filled.Speed,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.weight(1f)
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
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = color
      )
      Spacer(modifier = Modifier.height(8.dp))
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
        modifier = Modifier.padding(top = 4.dp)
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
      modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        ActivityItem(
          title = "Sabah Sürüşü",
          subtitle = "Bugün, 08:30",
          duration = "45 dk",
          distance = "8.2 km"
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        ActivityItem(
          title = "Akşam Turu",
          subtitle = "Dün, 18:15",
          duration = "1 saat 20 dk",
          distance = "12.5 km"
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
  distance: String
) {
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
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.Filled.DirectionsRun,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
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
      modifier = Modifier.padding(bottom = 12.dp)
    )
    
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
      shape = RoundedCornerShape(16.dp)
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(
          Icons.Filled.Lightbulb,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(24.dp)
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
    HomeScreen(onBarnSelected = {})
  }
}


