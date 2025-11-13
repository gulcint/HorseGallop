package com.horsegallop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.horsegallop.theme.LightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.airbnb.lottie.compose.*
import com.horsegallop.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview

private const val SPLASH_DURATION_MS: Long = 2000L
private const val MEDIA_VOLUME_MAX: Float = 1f
private const val LOTTIE_FILL_SCALE: Float = 0.6f

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

@Preview(showBackground = true, name = "AppContent")
@Composable
private fun PreviewAppContent() {
    MaterialTheme(colorScheme = LightColorScheme) { AppContent() }
}

@Preview(showBackground = true, name = "SplashScreen")
@Composable
private fun PreviewSplashScreen() {
    MaterialTheme(colorScheme = LightColorScheme) {
        // Side-effect free splash preview (no MediaPlayer/Lottie playback)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(com.horsegallop.core.R.string.welcome_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF222222)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(com.horsegallop.core.R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}
@Composable
fun AppContent(): Unit {
    var showSplash: Boolean by remember { mutableStateOf(true) }
    var splashFinished: Boolean by remember { mutableStateOf(false) }
    
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
fun SplashScreen(onFinished: () -> Unit): Unit {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White),
		contentAlignment = Alignment.Center
	) {
        val ctx = LocalContext.current
        val titleText: String = stringResource(com.horsegallop.core.R.string.welcome_title)
        val subtitleText: String = stringResource(com.horsegallop.core.R.string.welcome_subtitle)
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
				mp.setVolume(MEDIA_VOLUME_MAX, MEDIA_VOLUME_MAX)
				mp.start()
			}
			delay(SPLASH_DURATION_MS)
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
		
		var showLottie: Boolean by remember { mutableStateOf(true) }
		LaunchedEffect(Unit) {
			delay(SPLASH_DURATION_MS)
			showLottie = false
		}
		if (showLottie) {
			LottieAnimation(
				composition = composition,
				progress = { progress },
				modifier = Modifier.fillMaxSize(LOTTIE_FILL_SCALE)
			)
		}
		// Localized welcome texts over splash (auto-resolved by app locales/device locale)
		Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)) {
			Text(text = titleText, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF222222))
			Spacer(modifier = Modifier.size(6.dp))
			Text(text = subtitleText, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF666666))
		}
	}
}
