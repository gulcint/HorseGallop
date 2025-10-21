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
import com.horsegallop.feature.barn.presentation.BarnDetail
import com.horsegallop.feature.home.presentation.HomeScreen
import com.horsegallop.feature.onboarding.presentation.OnboardingScreen

sealed class Dest(val route: String) {
  data object Onboarding : Dest("onboarding")
  data object Login : Dest("login")
  data object Home : Dest("home")
  data object Ride : Dest("ride")
  data object Barns : Dest("barns")
  data object BarnDetail : Dest("barnDetail")
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
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
      BackHandler { activity?.finish() }
      HomeScreen(
        onStartRide = { navController.navigate(Dest.Ride.route) },
        onViewBarns = { navController.navigate(Dest.Barns.route) },
        onBarnSelected = { navController.navigate(Dest.BarnDetail.route) }
      )
    }
    composable(Dest.Ride.route) {
      // Ride ekranında geri tuşu Home'a döner
      BackHandler { navController.popBackStack() }
      com.horsegallop.feature.ride.presentation.RideTrackingScreen(
        viewModel = com.horsegallop.feature.ride.presentation.RideTrackingViewModel(),
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onBarnsClick = { navController.navigate(Dest.Barns.route) }
      )
    }
    composable(Dest.Barns.route) {
      // Barns ekranında geri tuşu Home'a döner
      BackHandler { navController.popBackStack() }
      com.horsegallop.feature.barn.presentation.BarnListScreen(
        onBarnClick = { navController.navigate(Dest.BarnDetail.route) },
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onRideClick = { navController.navigate(Dest.Ride.route) }
      )
    }
    composable(Dest.BarnDetail.route) {
      // BarnDetail ekranında geri tuşu önceki ekrana döner
      BackHandler { navController.popBackStack() }
      BarnDetail(slides = emptyList())
    }
  }
}
