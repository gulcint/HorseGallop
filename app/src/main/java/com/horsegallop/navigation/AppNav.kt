package com.horsegallop.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.horsegallop.feature.auth.domain.model.UserRole
import com.horsegallop.feature.auth.presentation.LoginScreen
import com.horsegallop.feature.auth.presentation.EmailLoginScreen
import com.horsegallop.feature.auth.presentation.EnrollmentScreen
import com.horsegallop.feature.auth.presentation.ProfileScreen
import com.horsegallop.feature.barn.presentation.BarnDetail
import com.horsegallop.feature.home.presentation.HomeScreen
import com.horsegallop.feature.onboarding.presentation.OnboardingScreen

sealed class Dest(val route: String) {
  data object Onboarding : Dest("onboarding")
  data object Login : Dest("login")
  data object Home : Dest("home")
  data object Ride : Dest("ride")
  data object Barns : Dest("barns")
  data object EmailLogin : Dest("emailLogin")
  data object Enroll : Dest("enroll")
  data object Profile : Dest("profile")
  data object BarnDetail : Dest("barnDetail/{id}") {
    fun routeWithId(id: String): String = "barnDetail/$id"
  }
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun AppNavHost(
  navController: NavHostController,
  role: UserRole?
) {
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route
  val showBottomBar = currentRoute in listOf(Dest.Home.route, Dest.Barns.route, Dest.Ride.route, Dest.Profile.route)

  Scaffold(
    bottomBar = {
      if (showBottomBar) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          tonalElevation = 6.dp,
          shadowElevation = 8.dp
        ) {
          NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
            NavigationBarItem(
              selected = currentRoute == Dest.Home.route,
              onClick = {
                navController.navigate(Dest.Home.route) {
                  popUpTo(navController.graph.findStartDestination().id)
                  launchSingleTop = true
                }
              },
              icon = { androidx.compose.material3.Icon(Icons.Filled.Home, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
              label = { androidx.compose.material3.Text(text = "Home", color = MaterialTheme.colorScheme.onPrimaryContainer) },
              alwaysShowLabel = true
            )
            NavigationBarItem(
              selected = currentRoute == Dest.Barns.route,
              onClick = {
                navController.navigate(Dest.Barns.route) {
                  popUpTo(navController.graph.findStartDestination().id)
                  launchSingleTop = true
                }
              },
              icon = { androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
              label = { androidx.compose.material3.Text(text = "Barns", color = MaterialTheme.colorScheme.onPrimaryContainer) },
              alwaysShowLabel = true
            )
            NavigationBarItem(
              selected = currentRoute == Dest.Ride.route,
              onClick = {
                navController.navigate(Dest.Ride.route) {
                  popUpTo(navController.graph.findStartDestination().id)
                  launchSingleTop = true
                }
              },
              icon = { androidx.compose.material3.Icon(Icons.Filled.Navigation, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
              label = { androidx.compose.material3.Text(text = "Ride", color = MaterialTheme.colorScheme.onPrimaryContainer) },
              alwaysShowLabel = true
            )
            NavigationBarItem(
              selected = currentRoute == Dest.Profile.route,
              onClick = {
                navController.navigate(Dest.Profile.route) {
                  popUpTo(navController.graph.findStartDestination().id)
                  launchSingleTop = true
                }
              },
              icon = { androidx.compose.material3.Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
              label = { androidx.compose.material3.Text(text = "Profile", color = MaterialTheme.colorScheme.onPrimaryContainer) },
              alwaysShowLabel = true
            )
          }
        }
      }
    }
  ) { innerPadding ->
  NavHost(navController = navController, startDestination = if (role == null) Dest.Onboarding.route else Dest.Home.route, modifier = Modifier.padding(innerPadding)) {
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
          navController.navigate(Dest.Home.route) {
            popUpTo(Dest.Login.route) { inclusive = true }
          }
        },
        onEmailClick = { navController.navigate(Dest.EmailLogin.route) },
        onSignupClick = { navController.navigate(Dest.Enroll.route) }
      )
    }
    composable(Dest.EmailLogin.route) {
      EmailLoginScreen(
        onBack = { navController.popBackStack() },
        onSignup = { navController.navigate(Dest.Enroll.route) },
        onSignedIn = {
          navController.navigate(Dest.Home.route) {
            popUpTo(Dest.Login.route) { inclusive = true }
          }
        }
      )
    }
    composable(Dest.Enroll.route) {
      EnrollmentScreen(
        onBack = { navController.popBackStack() },
        onSignedUp = {
          navController.navigate(Dest.Home.route) {
            popUpTo(Dest.Login.route) { inclusive = true }
          }
        }
      )
    }
    composable(Dest.Home.route) {
      val activity = LocalContext.current as? Activity
      BackHandler { activity?.finish() }
      HomeScreen(
        currentRoute = currentRoute,
        onStartRide = { navController.navigate(Dest.Ride.route) },
        onViewBarns = { navController.navigate(Dest.Barns.route) },
        onProfileClick = { navController.navigate(Dest.Profile.route) }
      )
    }
    composable(Dest.Profile.route) {
      ProfileScreen(
        onBack = { navController.popBackStack() },
        onLogout = {
          navController.navigate(Dest.Login.route) {
            popUpTo(Dest.Home.route) { inclusive = true }
          }
        }
      )
    }
    composable(Dest.Ride.route) {
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
        onBarnClick = { barn -> navController.navigate(Dest.BarnDetail.routeWithId(barn.id)) },
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onRideClick = { navController.navigate(Dest.Ride.route) }
      )
    }
    composable(
      route = Dest.BarnDetail.route,
      arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { backStackEntry ->
      BackHandler { navController.popBackStack() }
      BarnDetail(slides = emptyList())
    }
  }
  }
}
