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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.airbnb.lottie.compose.*
import com.horsegallop.navigation.AppNavHost
import com.horsegallop.navigation.Dest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import android.widget.Toast
import com.horsegallop.domain.model.UserRole
import androidx.compose.ui.graphics.toArgb
import com.horsegallop.core.debug.AppLog

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent { AppTheme { AppContent() } }
    }

    private fun handleDeepLink(intent: android.content.Intent?) {
        intent?.data?.let { data ->
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")
            if (mode == "verifyEmail" && !oobCode.isNullOrBlank()) {
                Toast.makeText(this, "E-posta doğrulama bağlantısı algılandı", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Preview(showBackground = true, name = "AppContent")
@Composable
private fun PreviewAppContent() {
    AppTheme { AppContent() }
}

@Preview(showBackground = true, name = "SplashScreen")
@Composable
private fun PreviewSplashScreen() {
    AppTheme {
        // Side-effect free splash preview (no MediaPlayer/Lottie playback)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val mainVm: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val ui by mainVm.ui.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Sync system bars color
    val primaryColor = MaterialTheme.colorScheme.primary
    SideEffect {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            window.statusBarColor = primaryColor.toArgb()
            window.navigationBarColor = primaryColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    if (ui.showSplash) {
        val activity = context as? ComponentActivity
        BackHandler { activity?.finish() }
        SplashScreen(onFinished = { mainVm.onSplashFinished() })
    } else {
        val activity = context as? ComponentActivity
        BackHandler {
            if (!navController.popBackStack()) {
                activity?.finish()
            }
        }
        
        AppNavHost(
            navController = navController,
            role = ui.userRole
        )

        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    mainVm.reloadUser()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        
        LaunchedEffect(ui.isLoggedIn) {
            if (!ui.isLoggedIn) {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != null && isAuthRequired(currentRoute)) {
                    navController.navigate(Dest.Onboarding.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

private fun isAuthRequired(route: String): Boolean {
    return route !in listOf(
        Dest.Onboarding.route,
        Dest.Login.route,
        Dest.EmailLogin.route,
        Dest.Enroll.route,
        Dest.ForgotPassword.route
    )
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val scheme = LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(
        com.horsegallop.core.theme.LocalTextColors provides com.horsegallop.core.theme.textColorsFrom(scheme)
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = com.horsegallop.core.theme.AppTypography,
            content = content
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
            LottieCompositionSpec.RawRes(com.horsegallop.core.R.raw.horse)
        )
        // Use LottieAnimatable to control playback precisely
        val lottieAnimatable = rememberLottieAnimatable()
        
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isSoundReady by remember { mutableStateOf(false) }
        
        // 1. Preload Sound in parallel
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val mp = MediaPlayer.create(ctx, com.horsegallop.core.R.raw.horse_gallop)
                    if (mp != null) {
                        mp.setVolume(1.0f, 1.0f)
                        mediaPlayer = mp
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isSoundReady = true
            }
        }
        
        // 2. Sync Logic: Wait for BOTH composition and sound -> Start -> Animate -> Finish
        LaunchedEffect(composition, isSoundReady) {
            if (composition == null || !isSoundReady) return@LaunchedEffect
            
            // Start Sound
            mediaPlayer?.start()
            
            // Play Animation (suspend until finished)
            lottieAnimatable.animate(
                composition = composition!!,
                iterations = 1
            )
            
            // Finish
            onFinished()
        }

        // Cleanup sound on exit
        DisposableEffect(Unit) {
            onDispose {
                try {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.stop()
                    }
                    mediaPlayer?.release()
                    mediaPlayer = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        LottieAnimation(
            composition = composition,
            progress = { lottieAnimatable.progress },
            modifier = Modifier.size(220.dp)
        )
		
		// Localized welcome texts over splash (auto-resolved by app locales/device locale)
		Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp)) {
			Text(text = titleText, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
			Spacer(modifier = Modifier.size(6.dp))
			Text(text = subtitleText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
		}
	}
}
