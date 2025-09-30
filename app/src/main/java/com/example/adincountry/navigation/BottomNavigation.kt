package com.example.adincountry.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(
	val route: String,
	val icon: ImageVector,
	val label: String
) {
	data object Home : BottomNavItem("home", Icons.Default.Home, "Ana Sayfa")
	data object Schedule : BottomNavItem("schedule", Icons.Default.List, "Program")
	data object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
	data object Settings : BottomNavItem("settings", Icons.Default.Settings, "Ayarlar")
}

@Composable
fun BottomNavigationBar(
	navController: NavController,
	items: List<BottomNavItem> = listOf(
		BottomNavItem.Home,
		BottomNavItem.Schedule,
		BottomNavItem.Profile,
		BottomNavItem.Settings
	)
) {
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route

	NavigationBar {
		items.forEach { item ->
			NavigationBarItem(
				icon = { Icon(item.icon, contentDescription = item.label) },
				label = { Text(item.label) },
				selected = currentRoute == item.route,
				onClick = {
					navController.navigate(item.route) {
						// Pop up to the start destination and save state
						popUpTo(navController.graph.startDestinationId) {
							saveState = true
						}
						// Avoid multiple copies of the same destination
						launchSingleTop = true
						// Restore state when reselecting a previously selected item
						restoreState = true
					}
				}
			)
		}
	}
}
