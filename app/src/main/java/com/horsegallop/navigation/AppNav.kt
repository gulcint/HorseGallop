package com.horsegallop.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.horsegallop.feature.auth.domain.model.UserRole
import com.horsegallop.feature.auth.presentation.LoginScreen
import com.horsegallop.feature.home.presentation.BarnDetail
import com.horsegallop.feature.home.presentation.BarnListScreen
import com.horsegallop.feature.home.presentation.OnboardingScreen

sealed class Dest(val route: String) {
  data object Onboarding : Dest("onboarding")
  data object Login : Dest("login")
  data object Home : Dest("home")
  data object BarnDetail : Dest("barnDetail")
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
      LoginScreen(
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
      // New home uses bottom tabs; default show Barn list
      BarnListScreen(onBarnClick = { navController.navigate(Dest.BarnDetail.route) })
    }
    composable(Dest.BarnDetail.route) {
      BarnDetail(slides = emptyList())
    }
  }
}
