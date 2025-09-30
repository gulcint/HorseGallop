package com.example.adincountry.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.domain.model.UserRole
import com.example.feature_auth.LoginScreen
import com.example.feature_home.HomeScreen
import com.example.feature_schedule.ScheduleScreen
import com.example.feature.profile.ProfileScreen
import com.example.feature.profile.ProfileData
import com.example.feature.settings.SettingsScreen

sealed class Dest(val route: String) {
  data object Login : Dest("login")
  data object Home : Dest("home")
  data object Schedule : Dest("schedule")
  data object Profile : Dest("profile")
  data object Settings : Dest("settings")
  data object Admin : Dest("admin")
}

@Composable
fun AppNavHost(
  navController: NavHostController,
  role: UserRole?,
  modifier: Modifier = Modifier
) {
  NavHost(
    navController = navController,
    startDestination = if (role == null) Dest.Login.route else BottomNavItem.Home.route
  ) {
    composable(Dest.Login.route) {
      LoginScreen(
        onGoogleClick = { /* trigger Google Sign-In */ },
        onAppleClick = { /* trigger Apple Sign-In */ },
        onEmailClick = { /* trigger Email Sign-In */ }
      )
    }
    
    composable(BottomNavItem.Home.route) {
      HomeScreen(slides = emptyList())
    }
    
    composable(BottomNavItem.Schedule.route) {
      ScheduleScreen(
        lessons = emptyList(),
        onLessonClick = { lessonId ->
          // Navigate to lesson details
        }
      )
    }
    
    composable(BottomNavItem.Profile.route) {
      ProfileScreen(
        profileData = ProfileData(
          name = "Kullanıcı",
          email = "user@example.com",
          phone = "+90 555 123 4567",
          role = role?.name ?: "CUSTOMER"
        ),
        onEditClick = { /* Navigate to edit profile */ },
        onLogoutClick = { 
          // Handle logout
          navController.navigate(Dest.Login.route) {
            popUpTo(0) { inclusive = true }
          }
        }
      )
    }
    
    composable(BottomNavItem.Settings.route) {
      SettingsScreen(
        currentLanguage = "tr",
        onLanguageChange = { lang ->
          // Change app language
        },
        notificationsEnabled = true,
        onNotificationsChange = { enabled ->
          // Update notification settings
        },
        onAboutClick = { /* Show about dialog */ },
        onPrivacyClick = { /* Navigate to privacy policy */ },
        onTermsClick = { /* Navigate to terms */ }
      )
    }
    
    composable(Dest.Admin.route) {
      // Admin panel root
    }
  }
}
