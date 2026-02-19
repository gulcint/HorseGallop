package com.example.feature_auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.feature_auth.LoginScreen

@Composable
fun AuthNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onGoogleClick = {},
                onAppleClick = {},
                onEmailClick = {}
            )
        }
    }
}
