package com.horsegallop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.horsegallop.navigation.AppNavHost
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
		
		setContent {
			MaterialTheme {
				var showLottie by remember { mutableStateOf(true) }
				
				// 2 saniye sonra ana ekrana geç
				LaunchedEffect(Unit) {
					delay(2000)
					showLottie = false
				}
				
				if (showLottie) {
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
							progress = { progress },
							modifier = Modifier.fillMaxSize(0.6f)
						)
					}
				} else {
					// Ana uygulama - Navigation
					val navController = rememberNavController()
					AppNavHost(
						navController = navController,
						role = null
					)
				}
			}
		}
	}
}
