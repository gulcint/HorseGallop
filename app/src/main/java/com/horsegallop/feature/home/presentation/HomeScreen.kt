@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.home.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.horsegallop.feature.barn.presentation.BarnListScreen
import com.horsegallop.feature.barn.domain.model.BarnUi
import com.horsegallop.feature.ride.presentation.RideTrackingScreen

@Composable
fun HomeScreen(onBarnSelected: (BarnUi) -> Unit) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf(
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
  ) { padding ->
    when (selectedTab) {
      0 -> RideTrackingScreen(viewModel = com.horsegallop.feature.ride.presentation.RideTrackingViewModel())
      else -> BarnListScreen(onBarnClick = onBarnSelected)
    }
  }
}

private data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)


