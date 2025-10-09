package com.horsegallop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import com.horsegallop.theme.LightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.*
import com.horsegallop.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	 override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Enable edge-to-edge mode for modern UI
		enableEdgeToEdge()
		
		// Set light status bar icons
		WindowCompat.getInsetsController(window, window.decorView).apply {
			isAppearanceLightStatusBars = true
			isAppearanceLightNavigationBars = true
		}
		
		setContent {
			MaterialTheme(colorScheme = LightColorScheme) {
				AppContent()
			}
		}
	}
}

@Composable
fun AppContent() {
    var showSplash by remember { mutableStateOf(true) }
    var splashFinished by remember { mutableStateOf(false) }
    
    LaunchedEffect(splashFinished) {
        if (splashFinished) showSplash = false
    }
    
    if (showSplash) {
		// Splash ekranında geri tuşu uygulamayı kapatır
		val activity = LocalContext.current as? ComponentActivity
		BackHandler {
			// Uygulamayı kapat
			activity?.finish()
		}
		
        SplashScreen(onFinished = { splashFinished = true })
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
fun SplashScreen(onFinished: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White),
		contentAlignment = Alignment.Center
	) {
        val ctx = LocalContext.current
        val composition by rememberLottieComposition(
			LottieCompositionSpec.RawRes(R.raw.horse)
		)
		val progress by animateLottieCompositionAsState(
			composition = composition,
			iterations = LottieConstants.IterateForever
		)

		val mediaPlayer = remember { MediaPlayer.create(ctx, R.raw.horse_gallop) }
		LaunchedEffect(mediaPlayer) {
			mediaPlayer?.let { mp ->
				mp.isLooping = true
				mp.setVolume(1f, 1f)
				mp.start()
			}
			delay(2000)
			onFinished()
		}
		DisposableEffect(Unit) {
			onDispose {
				try {
					mediaPlayer?.stop()
					mediaPlayer?.release()
				} catch (_: Throwable) {}
			}
		}
		
		var showLottie by remember { mutableStateOf(true) }
		LaunchedEffect(Unit) {
			delay(2000)
			showLottie = false
		}
		if (showLottie) {
			LottieAnimation(
				composition = composition,
				progress = { progress },
				modifier = Modifier.fillMaxSize(0.6f)
			)
		}
	}
}
