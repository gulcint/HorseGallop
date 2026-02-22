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
import com.horsegallop.core.theme.DarkColorScheme
import com.horsegallop.core.theme.LightColorScheme
import com.horsegallop.feature.settings.presentation.SettingsViewModel
import com.horsegallop.settings.ThemeMode
import com.horsegallop.settings.toLocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import android.widget.Toast
import com.horsegallop.domain.model.UserRole
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import com.horsegallop.core.debug.AppLog

import com.horsegallop.feature.common.presentation.NoInternetScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent { AppContent() }
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
    AppContent()
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
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Your horse riding experience starts here",
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
    val settingsVm: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val settings by settingsVm.uiState.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    val isOnlineState = remember { mutableStateOf(true) }
    var showNoInternet by remember { mutableStateOf(false) }

    LaunchedEffect(settings.language) {
        AppCompatDelegate.setApplicationLocales(settings.language.toLocaleList())
    }
    
    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        
        fun updateConnectionState() {
            scope.launch {
                try {
                    val network = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    val isOnline = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isOnlineState.value = isOnline
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        updateConnectionState()
        
        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                updateConnectionState()
            }
            override fun onLost(network: android.net.Network) {
                updateConnectionState()
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
            scope.cancel()
        }
    }
    
    AppTheme(themeMode = settings.themeMode) {
        val systemBarColor = MaterialTheme.colorScheme.background
        SideEffect {
            val window = (context as? android.app.Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                window.statusBarColor = systemBarColor.toArgb()
                window.navigationBarColor = systemBarColor.toArgb()
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                val isLightBars = systemBarColor.luminance() > 0.5f
                controller.isAppearanceLightStatusBars = isLightBars
                controller.isAppearanceLightNavigationBars = isLightBars
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (showNoInternet) {
                NoInternetScreen(
                    onRetry = {
                        if (isOnlineState.value) {
                            showNoInternet = false
                            if (ui.showSplash) mainVm.onSplashFinished()
                        }
                    }
                )
            } else if (ui.showSplash) {
                val activity = context as? ComponentActivity
                BackHandler { activity?.finish() }
                val isOnline = isOnlineState.value
                val hasError = ui.hasSplashError
                val splashTitle = when {
                    hasError && !isOnline -> stringResource(com.horsegallop.R.string.splash_error_title)
                    !ui.splashTitle.isNullOrBlank() -> ui.splashTitle
                    else -> "Welcome"
                }
                val splashSubtitle = when {
                    hasError && !isOnline -> stringResource(com.horsegallop.R.string.splash_error_subtitle)
                    !ui.splashSubtitle.isNullOrBlank() -> ui.splashSubtitle
                    else -> "Your horse riding experience starts here"
                }
                SplashScreen(
                    title = splashTitle,
                    subtitle = splashSubtitle,
                    isOnline = isOnline,
                    onFinished = {
                        if (isOnlineState.value) {
                            mainVm.onSplashFinished()
                        } else {
                            showNoInternet = true
                        }
                    }
                )
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
private fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme
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
fun SplashScreen(
    title: String? = null,
    subtitle: String? = null,
    isOnline: Boolean = true,
    onFinished: () -> Unit
): Unit {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val ctx = LocalContext.current
        val titleText: String = title ?: ""
        val subtitleText: String = subtitle ?: ""
        
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(com.horsegallop.R.raw.horse)
        )
        // Use LottieAnimatable to control playback precisely
        val lottieAnimatable = rememberLottieAnimatable()
        
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isSoundReady by remember { mutableStateOf(false) }
        var soundDurationMs by remember { mutableStateOf(0) }
        var finished by remember { mutableStateOf(false) }

        fun finishOnce() {
            if (finished) return
            finished = true
            onFinished()
        }
        
        DisposableEffect(Unit) {
            val mp = MediaPlayer()
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            
            val job = scope.launch {
                try {
                    val afd = ctx.resources.openRawResourceFd(com.horsegallop.R.raw.horse_gallop)
                    if (afd != null) {
                        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            mp.setOnPreparedListener { player ->
                                player.setVolume(1.0f, 1.0f)
                                soundDurationMs = player.duration
                                mediaPlayer = player
                                isSoundReady = true
                            }
                            mp.prepareAsync()
                        }
                    } else {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            isSoundReady = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isSoundReady = true
                    }
                }
            }

            onDispose {
                scope.cancel()
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.release()
                    mediaPlayer = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Prevent indefinite splash if media or animation cannot initialize.
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(5500)
            if (!finished) {
                AppLog.w("SplashScreen", "Forced finish after startup timeout")
                finishOnce()
            }
        }
        
        LaunchedEffect(composition, isSoundReady) {
            if (finished || !isSoundReady) return@LaunchedEffect
            
            val player = mediaPlayer
            if (player == null) {
                if (composition != null) {
                    lottieAnimatable.animate(
                        composition = composition,
                        iterations = 1
                    )
                }
                finishOnce()
                return@LaunchedEffect
            }
            
            val durationToWait = soundDurationMs
                .takeIf { it > 0 }
                ?: 1200
            
            val clampedDuration = durationToWait.coerceIn(500, 5000).toLong()
            
            try {
                player.seekTo(0)
                player.start()

                kotlinx.coroutines.delay(clampedDuration)

                if (composition != null) {
                    lottieAnimatable.animate(
                        composition = composition,
                        iterations = 1
                    )
                }
            } catch (e: Exception) {
                AppLog.e("SplashScreen", "Splash playback error: ${e.message}")
            } finally {
                finishOnce()
            }
        }
        
        LottieAnimation(
            composition = composition,
            progress = { lottieAnimatable.progress },
            modifier = Modifier.size(220.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
