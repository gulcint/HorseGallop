package com.horsegallop.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.horsegallop.domain.model.UserRole
import com.horsegallop.feature.auth.presentation.*
import com.horsegallop.feature.barn.presentation.*
import com.horsegallop.feature.home.presentation.*
import com.horsegallop.feature.onboarding.presentation.OnboardingScreen
import com.horsegallop.feature.schedule.presentation.ScheduleRoute
import com.horsegallop.feature.ride.presentation.*
import com.horsegallop.feature.horse.presentation.HorseDashboardScreen
import androidx.navigation.navDeepLink

// Import BottomNavItem and HorseGallopBottomNavigation from components module
import com.horsegallop.core.components.BottomNavigationItem
import com.horsegallop.core.components.HorseGallopBottomNavigation

// Bottom Navigation Items - Horse/Equestrian Theme
val bottomNavItems = listOf(
    BottomNavigationItem(Icons.Filled.Home, "Home", Dest.Home.route),
    BottomNavigationItem(Icons.Filled.Storefront, "Barns", Dest.Barns.route),
    BottomNavigationItem(Icons.Filled.PlayCircle, "Ride", Dest.Ride.route),
    BottomNavigationItem(Icons.Filled.Pets, "My Horse", Dest.HorseDashboard.route),
    BottomNavigationItem(Icons.Filled.Person, "Profile", Dest.Profile.route)
)

sealed class Dest(val route: String) {
  object Onboarding : Dest("onboarding")
  object Login : Dest("login")
  object Home : Dest("home")
  object Ride : Dest("ride")
  object HorseDashboard : Dest("horseDashboard")
  object Barns : Dest("barns")
  object EmailLogin : Dest("emailLogin")
  object ForgotPassword : Dest("forgotPassword")
  object Enroll : Dest("enroll")
  object Profile : Dest("profile")
  object RideDetail : Dest("rideDetail/{id}") {
      fun createRoute(id: String) = "rideDetail/$id"
  }
  object BarnDetail : Dest("barnDetail/{id}") {
    fun routeWithId(id: String): String = "barnDetail/$id"
  }
  object RecentActivityDetail : Dest("recentActivityDetail")
  object BarnsMapView : Dest("barnsMapView")
}

@Composable
fun AppNavHost(
  navController: NavHostController,
  role: UserRole?
) {
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route
  val showBottomBar = currentRoute in listOf(Dest.Home.route, Dest.Barns.route, Dest.Ride.route, Dest.HorseDashboard.route, Dest.Profile.route)
  val ctx = LocalContext.current
  val prefs = remember { ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
  val onboardingDone = remember { prefs.getBoolean("onboarding_done", false) }
  val startDest = when {
    !onboardingDone -> Dest.Onboarding.route
    role == null -> Dest.Login.route
    else -> Dest.Home.route
  }

  Scaffold(
    bottomBar = {
      if (showBottomBar) {
        HorseGallopBottomNavigation(
            items = bottomNavItems,
            selectedIndex = bottomNavItems.indexOfFirst { it.route == currentRoute },
            onSelectItem = { index ->
                navController.navigate(bottomNavItems[index].route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            },
            modifier = Modifier
        )
      }
    }
  ) { innerPadding ->
  NavHost(navController = navController, startDestination = startDest, modifier = Modifier.fillMaxSize().padding(innerPadding)) {
    composable(Dest.Onboarding.route) {
      OnboardingScreen(onStart = { prefs.edit().putBoolean("onboarding_done", true).apply(); navController.navigate(Dest.Login.route) { popUpTo(Dest.Onboarding.route) { inclusive = true } } }, onSkip = { prefs.edit().putBoolean("onboarding_done", true).apply(); navController.navigate(Dest.Login.route) { popUpTo(Dest.Onboarding.route) { inclusive = true } } })
    }
    composable(Dest.Login.route) {
      LoginScreen(onGoogleClick = { navController.navigate(Dest.Home.route) { popUpTo(Dest.Login.route) { inclusive = true } } }, onEmailClick = { navController.navigate(Dest.EmailLogin.route) }, onSignupClick = { navController.navigate(Dest.Enroll.route) }, onForgotPasswordClick = { navController.navigate(Dest.ForgotPassword.route) })
    }
    composable(Dest.Home.route) {
      val activity = LocalContext.current as? Activity
      BackHandler { activity?.finish() }
      HomeScreen(currentRoute = currentRoute, onStartRide = { navController.navigate(Dest.Ride.route) }, onViewBarns = { navController.navigate(Dest.Barns.route) }, onProfileClick = { navController.navigate(Dest.Profile.route) }, onViewAllActivities = { navController.navigate(Dest.RecentActivityDetail.route) })
    }
    composable(Dest.HorseDashboard.route) {
      HorseDashboardScreen(onBack = { navController.popBackStack() }, horse = null)
    }
    composable(Dest.Profile.route) {
      ProfileScreen(onBack = { navController.popBackStack() }, onLogout = { navController.navigate(Dest.Login.route) { popUpTo(0) { inclusive = true }; launchSingleTop = true } })
    }
    composable(Dest.Ride.route) {
      RideTrackingRoute(onHomeClick = { navController.navigate(Dest.Home.route) }, onBarnsClick = { navController.navigate(Dest.Barns.route) })
    }
    composable(Dest.Barns.route) {
      BarnListScreen(onBarnClick = { barn -> navController.navigate(Dest.BarnDetail.routeWithId(barn.id)) }, onHomeClick = { navController.navigate(Dest.Home.route) }, onRideClick = { navController.navigate(Dest.Ride.route) }, navController = navController)
    }
    composable(route = Dest.BarnDetail.route, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
      BarnDetailScreen(onBack = { navController.popBackStack() })
    }
    composable(route = Dest.RideDetail.route, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
      RideDetailScreen(onBack = { navController.popBackStack() })
    }
    composable(Dest.RecentActivityDetail.route) {
      RecentActivityDetailScreen(navController = navController)
    }
    composable(Dest.BarnsMapView.route) {
      BarnsMapViewScreen(navController = navController)
    }
    composable(Dest.EmailLogin.route) {
      EmailLoginScreen(onBack = { navController.popBackStack() }, onSignup = { navController.navigate(Dest.Enroll.route) }, onSignedIn = { navController.navigate(Dest.Home.route) { popUpTo(Dest.Login.route) { inclusive = true } } })
    }
    composable(Dest.Enroll.route) {
      EnrollmentScreen(onBack = { navController.popBackStack() }, onSignedUp = { navController.navigate(Dest.Home.route) { popUpTo(Dest.Login.route) { inclusive = true } } })
    }
  }
  }
}
