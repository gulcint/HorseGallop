package com.example.adincountry

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.example.adincountry.navigation.AppNavHost
import com.example.adincountry.navigation.BottomNavItem
import com.example.adincountry.navigation.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
	override fun onCreate(savedInstanceState: Bundle?) {
		// Splash screen'i hızlı geç
		installSplashScreen().apply {
			setKeepOnScreenCondition { false }
		}
		super.onCreate(savedInstanceState)
		
		setContent {
			MaterialTheme {
				var showLottie by remember { mutableStateOf(true) }
				
				// 2 saniye sonra ana ekrana geç
				LaunchedEffect(Unit) {
					delay(2000)
					showLottie = false
				}
				
				if (showLottie) {
					// Lottie animasyon ekranı
					Box(
						modifier = Modifier.fillMaxSize(),
						contentAlignment = Alignment.Center
					) {
						val composition by rememberLottieComposition(
							LottieCompositionSpec.RawRes(R.raw.horse)
						)
						val progress by animateLottieCompositionAsState(
							composition = composition,
							iterations = LottieConstants.IterateForever
						)
						
						LottieAnimation(
							composition = composition,
							progress = { progress }
						)
					}
				} else {
					// Ana uygulama
					val navController = rememberNavController()
					val navBackStackEntry by navController.currentBackStackEntryAsState()
					val currentRoute = navBackStackEntry?.destination?.route
					
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
							role = null,
							modifier = Modifier.padding(paddingValues)
						)
					}
				}
			}
		}
	}
}
