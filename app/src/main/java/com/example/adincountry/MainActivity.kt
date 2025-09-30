package com.example.adincountry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.example.adincountry.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		// Splash screen'i hızlı geç
		installSplashScreen().apply {
			setKeepOnScreenCondition { false }
		}
		super.onCreate(savedInstanceState)
		
		// Enable edge-to-edge mode for modern UI
		enableEdgeToEdge()
		
		// Set light status bar icons
		WindowCompat.getInsetsController(window, window.decorView).apply {
			isAppearanceLightStatusBars = true
			isAppearanceLightNavigationBars = true
		}
		
		setContent {
			MaterialTheme {
				AppContent()
			}
		}
	}
}

@Composable
fun AppContent() {
	var showSplash by remember { mutableStateOf(true) }
	
	// 2 saniye sonra ana ekrana geç
	LaunchedEffect(Unit) {
		delay(2000)
		showSplash = false
	}
	
	if (showSplash) {
		// Splash ekranında geri tuşu uygulamayı kapatır
		BackHandler {
			// Do nothing - splash ekranında geri tuşunu devre dışı bırak
		}
		
		SplashScreen()
	} else {
		// Ana uygulama - Navigation
		val navController = rememberNavController()
		AppNavHost(
			navController = navController,
			role = null
		)
	}
}

@Composable
fun SplashScreen() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White),
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
			progress = { progress },
			modifier = Modifier.fillMaxSize(0.6f)
		)
	}
}
