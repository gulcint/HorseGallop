package com.example.adincountry.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.domain.model.UserRole
import com.example.feature_auth.LoginScreen
import com.example.feature_home.ModernHomeScreen

sealed class Dest(val route: String) {
  data object Login : Dest("login")
  data object Home : Dest("home")
  data object Admin : Dest("admin")
}

@Composable
fun AppNavHost(
  navController: NavHostController,
  role: UserRole?
) {
  NavHost(navController = navController, startDestination = if (role == null) Dest.Login.route else Dest.Home.route) {
    composable(Dest.Login.route) {
      LoginScreen(
        onGoogleClick = { /* trigger */ },
        onAppleClick = { /* trigger */ },
        onEmailClick = { /* TODO */ }
      )
    }
    composable(Dest.Home.route) {
      ModernHomeScreen(slides = emptyList())
    }
    composable(Dest.Admin.route) {
      // Admin panel root
    }
  }
}
