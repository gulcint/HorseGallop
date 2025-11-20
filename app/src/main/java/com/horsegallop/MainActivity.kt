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
 
import com.horsegallop.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.firebase.auth.FirebaseAuth
import com.horsegallop.feature.auth.domain.model.UserRole

private const val SPLASH_DURATION_MS: Long = 2000L

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
            val scheme = LightColorScheme
            androidx.compose.runtime.CompositionLocalProvider(
                com.horsegallop.core.theme.LocalTextColors provides com.horsegallop.core.theme.textColorsFrom(scheme)
            ) {
                MaterialTheme(colorScheme = scheme) {
                    AppContent()
                }
            }
        }
	}
}

@Preview(showBackground = true, name = "AppContent")
@Composable
private fun PreviewAppContent() {
    val scheme = LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(
        com.horsegallop.core.theme.LocalTextColors provides com.horsegallop.core.theme.textColorsFrom(scheme)
    ) {
        MaterialTheme(colorScheme = scheme) { AppContent() }
    }
}

@Preview(showBackground = true, name = "SplashScreen")
@Composable
private fun PreviewSplashScreen() {
    val scheme = LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(
        com.horsegallop.core.theme.LocalTextColors provides com.horsegallop.core.theme.textColorsFrom(scheme)
    ) {
    MaterialTheme(colorScheme = scheme) {
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
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(com.horsegallop.core.R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(): Unit {
    var showSplash: Boolean by remember { mutableStateOf(true) }
    var splashFinished: Boolean by remember { mutableStateOf(false) }
    
    LaunchedEffect(splashFinished) {
        if (splashFinished) showSplash = false
    }
    
    if (showSplash) {
        SplashScreen(onFinished = { splashFinished = true })
    } else {
        // Ana uygulama - Navigation
        val navController = rememberNavController()
        val isLoggedIn = remember { FirebaseAuth.getInstance().currentUser != null }
        AppNavHost(
            navController = navController,
            role = if (isLoggedIn) UserRole.CUSTOMER else null
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
        val titleText: String = stringResource(com.horsegallop.core.R.string.welcome_title)
        val subtitleText: String = stringResource(com.horsegallop.core.R.string.welcome_subtitle)
        LaunchedEffect(Unit) {
            delay(SPLASH_DURATION_MS)
            onFinished()
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = titleText, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.size(6.dp))
            Text(text = subtitleText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
