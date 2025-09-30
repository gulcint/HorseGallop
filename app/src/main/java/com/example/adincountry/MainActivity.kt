package com.example.adincountry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adincountry.navigation.AppNavHost
import com.example.adincountry.navigation.BottomNavItem
import com.example.adincountry.navigation.BottomNavigationBar
import com.example.adincountry.navigation.Dest
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        // Show bottom navigation only on main screens
        val showBottomNav = currentRoute in listOf(
          BottomNavItem.Home.route,
          BottomNavItem.Schedule.route,
          BottomNavItem.Profile.route,
          BottomNavItem.Settings.route
        )
        
        Scaffold(
          bottomBar = {
            if (showBottomNav) {
              BottomNavigationBar(navController = navController)
            }
          }
        ) { paddingValues ->
          AppNavHost(
            navController = navController,
            role = null, // TODO: Get from auth state
            modifier = Modifier.padding(paddingValues)
          )
        }
      }
    }
  }
}
