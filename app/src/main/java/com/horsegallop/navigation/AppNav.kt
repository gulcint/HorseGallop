package com.horsegallop.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.horsegallop.domain.model.UserRole
import androidx.compose.runtime.remember
import com.horsegallop.feature.auth.presentation.LoginScreen
import com.horsegallop.feature.auth.presentation.EmailLoginScreen
import com.horsegallop.feature.auth.presentation.EditProfileScreen
import com.horsegallop.feature.auth.presentation.EnrollmentScreen
import com.horsegallop.feature.auth.presentation.ForgotPasswordScreen
import com.horsegallop.feature.auth.presentation.ProfileScreen
import com.horsegallop.feature.auth.presentation.ProfileViewModel
import com.horsegallop.feature.barn.presentation.BarnDetailScreen
import com.horsegallop.feature.home.presentation.HomeScreen
import com.horsegallop.feature.onboarding.presentation.OnboardingScreen
import com.horsegallop.feature.horse.presentation.AddHorseScreen
import com.horsegallop.feature.horse.presentation.HorseHealthScreen
import com.horsegallop.feature.horse.presentation.HorseListScreen
import com.horsegallop.feature.review.presentation.WriteReviewScreen
import com.horsegallop.feature.notifications.presentation.NotificationsScreen
import com.horsegallop.feature.schedule.presentation.MyReservationsScreen
import com.horsegallop.feature.schedule.presentation.ScheduleRoute
import com.horsegallop.domain.review.model.ReviewTargetType

import androidx.navigation.navDeepLink
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.feature.ride.presentation.RideTrackingRoute
import com.horsegallop.feature.ride.presentation.RideTrackingViewModel

import com.horsegallop.feature.ride.presentation.RideDetailScreen
import com.horsegallop.feature.safety.presentation.SafetyScreen
import com.horsegallop.feature.subscription.presentation.SubscriptionScreen
import com.horsegallop.feature.training.presentation.TrainingPlansScreen
import com.horsegallop.ui.theme.LocalSemanticColors

sealed class Dest(val route: String) {
  object Onboarding : Dest("onboarding")
  object Login : Dest("login")
  object Home : Dest("home")
  object Ride : Dest("ride")
  object Schedule : Dest("schedule")
  object Barns : Dest("barns")
  object EmailLogin : Dest("emailLogin")
  object ForgotPassword : Dest("forgotPassword")
  object Enroll : Dest("enroll")
  object Profile : Dest("profile")
  object Training : Dest("training")
  object ProfileEdit : Dest("profile/edit")
  object Settings : Dest("settings")
  object MyReservations : Dest("myReservations")
  object MyHorses : Dest("myHorses")
  object AddHorse : Dest("addHorse")
  object WriteReview : Dest("writeReview/{targetId}/{targetType}/{targetName}") {
    fun route(targetId: String, targetType: String, targetName: String) =
      "writeReview/$targetId/$targetType/${android.net.Uri.encode(targetName)}"
  }
  object BarnDetail : Dest("barnDetail/{id}") {
    fun routeWithId(id: String): String = "barnDetail/$id"
  }
  object RecentActivityDetail : Dest("recentActivityDetail")
  object BarnsMapView : Dest("barnsMapView")
  object RideDetail : Dest("rideDetail/{id}") {
    fun routeWithId(id: String): String = "rideDetail/$id"
  }
  object Notifications : Dest("notifications")
  object Subscription : Dest("subscription")
  object HorseHealth : Dest("horseHealth/{horseId}/{horseName}") {
    fun route(horseId: String, horseName: String): String =
      "horseHealth/$horseId/${android.net.Uri.encode(horseName)}"
  }
  object Safety : Dest("safety")
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun AppNavHost(
  navController: NavHostController,
  role: UserRole?
) {
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route
  val showBottomBar = currentRoute in listOf(
    Dest.Home.route,
    Dest.Barns.route,
    Dest.Ride.route,
    Dest.Schedule.route,
    Dest.Profile.route
  )
  val ctx = androidx.compose.ui.platform.LocalContext.current
  val prefs = remember { ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
  val onboardingDone = remember { prefs.getBoolean("onboarding_done", false) }
  val startDest = when {
    !onboardingDone -> Dest.Onboarding.route
    role == null -> Dest.Login.route
    else -> Dest.Home.route
  }
  
  com.horsegallop.core.debug.AppLog.i("AppNavHost", "role=$role onboardingDone=$onboardingDone startDest=$startDest")

  Scaffold(
    bottomBar = {
      if (showBottomBar) {
        val semantic = LocalSemanticColors.current
        Surface(
          color = semantic.panelOverlay,
          tonalElevation = 2.dp,
          shadowElevation = 4.dp
        ) {
          val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
          NavigationBar(containerColor = semantic.panelOverlay) {
            NavigationBarItem(
              selected = currentRoute == Dest.Home.route,
              onClick = {
                navController.navigate(Dest.Home.route) {
                  popUpTo(navController.graph.findStartDestination().id)
                  launchSingleTop = true
                }
              },
              icon = { androidx.compose.material3.Icon(Icons.Filled.Home, null) },
              label = { androidx.compose.material3.Text(text = stringResource(com.horsegallop.R.string.nav_home)) },
              colors = itemColors,
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
              icon = { androidx.compose.material3.Icon(Icons.Filled.House, null) },
              label = { androidx.compose.material3.Text(text = stringResource(com.horsegallop.R.string.nav_barns)) },
              colors = itemColors,
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
              icon = { androidx.compose.material3.Icon(Icons.Filled.Pets, null) },
              label = { androidx.compose.material3.Text(text = stringResource(com.horsegallop.R.string.nav_ride)) },
              colors = itemColors,
              alwaysShowLabel = true
            )
            NavigationBarItem(
              selected = currentRoute == Dest.Schedule.route,
              onClick = {
                navController.navigate(Dest.Schedule.route) {
                  popUpTo(navController.graph.findStartDestination().id)
                  launchSingleTop = true
                }
              },
              icon = { androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.List, null) },
              label = { androidx.compose.material3.Text(text = stringResource(com.horsegallop.R.string.nav_schedule)) },
              colors = itemColors,
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
              icon = { androidx.compose.material3.Icon(Icons.Filled.Person, null) },
              label = { androidx.compose.material3.Text(text = stringResource(com.horsegallop.R.string.nav_profile)) },
              colors = itemColors,
              alwaysShowLabel = true
            )
          }
        }
      }
    }
  ) { innerPadding ->
  NavHost(navController = navController, startDestination = startDest, modifier = Modifier.fillMaxSize().padding(innerPadding)) {
    composable(Dest.Onboarding.route) {
      OnboardingScreen(
        onStart = {
          prefs.edit().putBoolean("onboarding_done", true).apply()
          navController.navigate(Dest.Login.route) {
            popUpTo(Dest.Onboarding.route) { inclusive = true }
          }
        },
        onSkip = {
          prefs.edit().putBoolean("onboarding_done", true).apply()
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
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
          }
        },
        onEmailClick = { navController.navigate(Dest.EmailLogin.route) },
        onSignupClick = { navController.navigate(Dest.Enroll.route) },
        onForgotPasswordClick = { navController.navigate(Dest.ForgotPassword.route) }
      )
    }
    composable(
      Dest.ForgotPassword.route,
      deepLinks = listOf(
          navDeepLink { uriPattern = "horsegallop://auth-action?mode={mode}&oobCode={oobCode}&apiKey={apiKey}" },
          navDeepLink { uriPattern = "https://horsegallop.page.link/reset-password?oobCode={oobCode}&mode={mode}&apiKey={apiKey}" },
          navDeepLink { uriPattern = "https://horsegallop.page.link/reset-password?link={link}" } // Sometimes Firebase wraps it in a 'link' param
      )
    ) { backStackEntry ->
        val uri = backStackEntry.arguments?.getString("link") ?: ""
        var mode = backStackEntry.arguments?.getString("mode")
        var oobCode = backStackEntry.arguments?.getString("oobCode")

        // If it came via "link" param (Firebase Dynamic Links often do this), parse it manually if needed,
        // or rely on Android intent filter to have already unwrapped it if it was a direct match.
        // Actually, Firebase ActionCodeSettings with handleCodeInApp=true usually sends a link like:
        // https://package_name/reset-password?oobCode=...&mode=resetPassword
        
        // Let's assume the navDeepLink pattern matching works for the query params.
        
        val viewModel: com.horsegallop.feature.auth.presentation.ForgotPasswordViewModel = androidx.hilt.navigation.compose.hiltViewModel()
        
        if (mode == "resetPassword" && oobCode != null) {
            androidx.compose.runtime.LaunchedEffect(oobCode) {
                viewModel.handleDeepLink(oobCode)
            }
        }
        
      ForgotPasswordScreen(
        onBack = { navController.popBackStack() },
        viewModel = viewModel
      )
    }
    composable(Dest.EmailLogin.route) {
      EmailLoginScreen(
        onBack = { navController.popBackStack() },
        onSignup = { navController.navigate(Dest.Enroll.route) },
        onForgotPassword = { navController.navigate(Dest.ForgotPassword.route) },
        onSignedIn = {
          navController.navigate(Dest.Home.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
          }
        }
      )
    }
    composable(Dest.Enroll.route) {
      EnrollmentScreen(
        onBack = { navController.popBackStack() },
        onSignedUp = {
          navController.navigate(Dest.Home.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
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
        onOpenTrainingPlans = { navController.navigate(Dest.Training.route) },
        onProfileClick = { navController.navigate(Dest.Profile.route) },
        onOpenRideDetail = { rideId ->
          navController.navigate(Dest.RideDetail.routeWithId(rideId))
        },
        onViewAllActivities = { navController.navigate(Dest.RecentActivityDetail.route) }
      )
    }
    composable(Dest.Training.route) {
      TrainingPlansScreen(
        onBack = { navController.popBackStack() },
        onNavigateToSubscription = { navController.navigate(Dest.Subscription.route) }
      )
    }
    composable(Dest.Subscription.route) {
      SubscriptionScreen(
        onBack = { navController.popBackStack() }
      )
    }
    composable(Dest.Profile.route) {
      val profileViewModel: ProfileViewModel = hiltViewModel()
      ProfileScreen(
        onBack = { navController.popBackStack() },
        onSettings = { navController.navigate(Dest.Settings.route) },
        onEditProfile = { navController.navigate(Dest.ProfileEdit.route) },
        onMyHorses = { navController.navigate(Dest.MyHorses.route) },
        onNotifications = { navController.navigate(Dest.Notifications.route) },
        onLogout = {
          navController.navigate(Dest.Onboarding.route) {
            popUpTo(Dest.Home.route) { inclusive = true }
          }
        },
        viewModel = profileViewModel
      )
    }
    composable(Dest.ProfileEdit.route) { backStackEntry ->
      val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(Dest.Profile.route)
      }
      val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
      EditProfileScreen(
        onBack = { navController.popBackStack() },
        viewModel = profileViewModel
      )
    }
    composable(Dest.Settings.route) {
      com.horsegallop.feature.settings.presentation.SettingsScreen(
        onBack = { navController.popBackStack() },
        onAccountDeleted = {
          navController.navigate(Dest.Onboarding.route) {
            popUpTo(Dest.Home.route) { inclusive = true }
          }
        },
        onSafety = { navController.navigate(Dest.Safety.route) }
      )
    }
    composable(Dest.Safety.route) {
      BackHandler { navController.popBackStack() }
      SafetyScreen(onBack = { navController.popBackStack() })
    }
    composable(Dest.Ride.route) {
      com.horsegallop.feature.ride.presentation.RideTrackingRoute(
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onBarnsClick = { navController.navigate(Dest.Barns.route) }
      )
    }
    composable(Dest.Schedule.route) {
      ScheduleRoute(onMyReservations = { navController.navigate(Dest.MyReservations.route) })
    }
    composable(Dest.MyReservations.route) {
      BackHandler { navController.popBackStack() }
      MyReservationsScreen(
        onBack = { navController.popBackStack() },
        onWriteReview = { lessonId, lessonTitle ->
          navController.navigate(Dest.WriteReview.route(lessonId, "lesson", lessonTitle))
        }
      )
    }
    composable(Dest.MyHorses.route) {
      BackHandler { navController.popBackStack() }
      HorseListScreen(
        onAddHorse = { navController.navigate(Dest.AddHorse.route) },
        onBack = { navController.popBackStack() },
        onHorseHealthClick = { horseId, horseName ->
          navController.navigate(Dest.HorseHealth.route(horseId, horseName))
        }
      )
    }
    composable(Dest.AddHorse.route) {
      BackHandler { navController.popBackStack() }
      AddHorseScreen(onBack = { navController.popBackStack() })
    }
    composable(
      route = Dest.HorseHealth.route,
      arguments = listOf(
        navArgument("horseId") { type = NavType.StringType },
        navArgument("horseName") { type = NavType.StringType }
      )
    ) { backStackEntry ->
      BackHandler { navController.popBackStack() }
      val horseId = backStackEntry.arguments?.getString("horseId") ?: ""
      val horseName = android.net.Uri.decode(backStackEntry.arguments?.getString("horseName") ?: "")
      HorseHealthScreen(
        horseId = horseId,
        horseName = horseName,
        onBack = { navController.popBackStack() }
      )
    }
    composable(
      route = Dest.WriteReview.route,
      arguments = listOf(
        navArgument("targetId") { type = NavType.StringType },
        navArgument("targetType") { type = NavType.StringType },
        navArgument("targetName") { type = NavType.StringType }
      )
    ) { backStackEntry ->
      BackHandler { navController.popBackStack() }
      val targetId = backStackEntry.arguments?.getString("targetId") ?: ""
      val targetType = if (backStackEntry.arguments?.getString("targetType") == "instructor")
        ReviewTargetType.INSTRUCTOR else ReviewTargetType.LESSON
      val targetName = android.net.Uri.decode(backStackEntry.arguments?.getString("targetName") ?: "")
      WriteReviewScreen(
        targetId = targetId,
        targetType = targetType,
        targetName = targetName,
        onBack = { navController.popBackStack() }
      )
    }
    composable(Dest.Barns.route) {
      // Barns ekranında geri tuşu Home'a döner
      BackHandler { navController.popBackStack() }
      com.horsegallop.feature.barn.presentation.BarnListScreen(
        onBarnClick = { barn -> navController.navigate(Dest.BarnDetail.routeWithId(barn.id)) },
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onRideClick = { navController.navigate(Dest.Ride.route) },
        navController = navController
      )
    }
    composable(
      route = Dest.BarnDetail.route,
      arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { backStackEntry ->
      BackHandler { navController.popBackStack() }
      BarnDetailScreen(onBack = { navController.popBackStack() })
    }
    composable(Dest.RecentActivityDetail.route) {
      BackHandler { navController.popBackStack() }
      com.horsegallop.feature.home.presentation.RecentActivityDetailScreen(
        navController = navController,
        onOpenRideDetail = { rideId ->
          navController.navigate(Dest.RideDetail.routeWithId(rideId))
        }
      )
    }
    composable(
      route = Dest.RideDetail.route,
      arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) {
      BackHandler { navController.popBackStack() }
      RideDetailScreen(
        onBack = { navController.popBackStack() }
      )
    }
    composable(Dest.BarnsMapView.route) {
      BackHandler { navController.popBackStack() }
      com.horsegallop.feature.barn.presentation.BarnsMapViewScreen(
        navController = navController
      )
    }
    composable(Dest.Notifications.route) {
      BackHandler { navController.popBackStack() }
      NotificationsScreen(
        onBack = { navController.popBackStack() }
      )
    }
  }
  }
}
