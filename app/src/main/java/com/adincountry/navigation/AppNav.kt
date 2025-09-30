package com.adincountry.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.domain.model.UserRole
import com.example.feature_auth.ProfessionalLoginScreen
import com.example.feature_home.HomeScreen
import com.example.feature_home.OnboardingScreen

sealed class Dest(val route: String) {
  data object Onboarding : Dest("onboarding")
  data object Login : Dest("login")
  data object Home : Dest("home")
  data object Admin : Dest("admin")
}

@Composable
fun AppNavHost(
  navController: NavHostController,
  role: UserRole?
) {
  NavHost(navController = navController, startDestination = if (role == null) Dest.Onboarding.route else Dest.Home.route) {
    composable(Dest.Onboarding.route) {
      OnboardingScreen(
        onStart = {
          navController.navigate(Dest.Login.route) {
            popUpTo(Dest.Onboarding.route) { inclusive = true }
          }
        },
        onSkip = {
          navController.navigate(Dest.Login.route) {
            popUpTo(Dest.Onboarding.route) { inclusive = true }
          }
        }
      )
    }
    composable(Dest.Login.route) {
      ProfessionalLoginScreen(
        onGoogleClick = { 
          // Mock OAuth - Navigate to Home
          navController.navigate(Dest.Home.route) {
            popUpTo(Dest.Login.route) { inclusive = true }
          }
        },
        onAppleClick = { 
          // Mock OAuth - Navigate to Home
          navController.navigate(Dest.Home.route) {
            popUpTo(Dest.Login.route) { inclusive = true }
          }
        },
        onEmailClick = { 
          // Mock Email Login - Navigate to Home
          navController.navigate(Dest.Home.route) {
            popUpTo(Dest.Login.route) { inclusive = true }
          }
        }
      )
    }
    composable(Dest.Home.route) {
      // Home ekranında geri tuşu uygulamayı kapatır
      val activity = LocalContext.current as? Activity
      BackHandler {
        activity?.finish()
      }
      
      HomeScreen(slides = emptyList())
    }
    composable(Dest.Admin.route) {
      // Admin panel root
    }
  }
}
