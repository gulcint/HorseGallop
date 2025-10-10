package com.horsegallop.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

enum class HomeTab { Ride, Achievements, Settings, Barns }

@Composable
fun HomeScreen(navController: NavHostController) {
    var selectedTab: HomeTab by remember { mutableStateOf(HomeTab.Ride) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Ride,
                    onClick = { selectedTab = HomeTab.Ride },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    label = { Text("Ride") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Achievements,
                    onClick = { selectedTab = HomeTab.Achievements },
                    icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                    label = { Text("Achievements") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Settings,
                    onClick = { selectedTab = HomeTab.Settings },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Barns,
                    onClick = { selectedTab = HomeTab.Barns },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Barns") }
                )
            }
        }
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                HomeTab.Ride -> Text(
                    "Coming soon: Achievements",
                    style = MaterialTheme.typography.titleMedium
                )

                HomeTab.Achievements -> {
                    Text("Coming soon: Achievements", style = MaterialTheme.typography.titleMedium)
                }

                HomeTab.Settings -> {
                    Text("Coming soon: Settings", style = MaterialTheme.typography.titleMedium)
                }

                HomeTab.Barns -> {
                    BarnListScreen(onBarnClick = { navController.navigate("barnDetail") })
                }
            }
        }
    }
}
