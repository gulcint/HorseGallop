package com.horsegallop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.horsegallop.feature.settings.presentation.SettingsViewModel
import com.horsegallop.settings.toLocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.horsegallop.navigation.AppNavHost
import com.horsegallop.navigation.Dest
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import com.horsegallop.domain.model.UserRole
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import com.horsegallop.core.debug.AppLog
import com.horsegallop.core.feedback.HorseGallopSnackbarHost
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.core.feedback.SnackbarAppFeedbackController
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

import com.horsegallop.feature.common.presentation.NoInternetScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingDeepLinkFeedbackResId: Int? = null

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent {
            AppContent(initialFeedbackMessageResId = pendingDeepLinkFeedbackResId)
        }
    }

    private fun handleDeepLink(intent: android.content.Intent?) {
        intent?.data?.let { data ->
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")
            if (mode == "verifyEmail" && !oobCode.isNullOrBlank()) {
                pendingDeepLinkFeedbackResId = R.string.feedback_email_verification_link_detected
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
        val semantic = LocalSemanticColors.current
        // Side-effect free splash preview (no MediaPlayer/Lottie playback)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(semantic.screenBase),
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
fun AppContent(initialFeedbackMessageResId: Int? = null) {
    val mainVm: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val ui by mainVm.ui.collectAsState()
    val settingsVm: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val settings by settingsVm.uiState.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    val appSnackbarHostState = remember { SnackbarHostState() }
    val appFeedbackController = remember(appSnackbarHostState, context.resources) {
        SnackbarAppFeedbackController(
            hostState = appSnackbarHostState,
            resources = context.resources
        )
    }
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
    
    CompositionLocalProvider(LocalAppFeedbackController provides appFeedbackController) {
    AppTheme(themeMode = settings.themeMode) {
        val semantic = LocalSemanticColors.current
        val systemBarColor = semantic.screenTopBar
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

        LaunchedEffect(initialFeedbackMessageResId) {
            initialFeedbackMessageResId?.let { messageResId ->
                appFeedbackController.showInfo(messageResId)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (showNoInternet) {
                NoInternetScreen(
                    message = ui.offlineHelpText,
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

            HorseGallopSnackbarHost(
                hostState = appSnackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
fun SplashScreen(
    title: String? = null,
    subtitle: String? = null,
    isOnline: Boolean = true,
    onFinished: () -> Unit
): Unit {
    val semantic = LocalSemanticColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(semantic.screenBase),
        contentAlignment = Alignment.Center
    ) {
        val titleText: String = title ?: ""
        val subtitleText: String = subtitle ?: ""
        var finished by remember { mutableStateOf(false) }
        val splashTimeoutMs = 1200L

        fun finishOnce(reason: String) {
            if (finished) return
            AppLog.i("SplashScreen", "splash_finished reason=$reason")
            finished = true
            onFinished()
        }
        
        DisposableEffect(Unit) {
            AppLog.i("SplashScreen", "splash_started")
            onDispose {}
        }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(splashTimeoutMs)
            if (!finished) {
                AppLog.w("SplashScreen", "forced_timeout_triggered")
                finishOnce("timeout")
            }
        }

        SplashBadge()

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

@Composable
private fun SplashBadge() {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = Modifier
            .size(156.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
        shape = CircleShape,
        color = semantic.cardElevated
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}
