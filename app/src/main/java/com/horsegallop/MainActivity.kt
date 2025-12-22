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
import com.google.firebase.auth.FirebaseAuth
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
		
        // Enable edge-to-edge
        enableEdgeToEdge()
		
        // Handle Firebase verifyEmail deep link
        intent?.data?.let { data ->
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")
            if (mode == "verifyEmail" && !oobCode.isNullOrBlank()) {
                FirebaseAuth.getInstance().applyActionCode(oobCode)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "E-posta doğrulandı", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, task.exception?.localizedMessage ?: "Doğrulama başarısız", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        setContent { AppTheme { AppContent() } }
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
fun AppContent(): Unit {
    var showSplash: Boolean by remember { mutableStateOf(true) }
    var splashFinished: Boolean by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val isLoggedIn = remember { FirebaseAuth.getInstance().currentUser != null }
    
    LaunchedEffect(splashFinished) {
        if (splashFinished) showSplash = false
    }
    
    // Sync system bars color to app theme (Saddle Brown)
    val activity = LocalContext.current as? ComponentActivity
    val primaryColor = MaterialTheme.colorScheme.primary
    SideEffect {
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            window.statusBarColor = primaryColor.toArgb()
            window.navigationBarColor = primaryColor.toArgb()
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
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
        val act = LocalContext.current as? ComponentActivity
        BackHandler {
            val canPop = navController.popBackStack()
            if (!canPop) {
                act?.finish()
            }
        }
        AppNavHost(
            navController = navController,
            role = if (isLoggedIn) UserRole.CUSTOMER else null
        )

        DisposableEffect(navController) {
            val auth = FirebaseAuth.getInstance()
            val authListener = FirebaseAuth.AuthStateListener { fa ->
                if (fa.currentUser == null) {
                    val currentRoute = navController.currentDestination?.route
                    // Allow Onboarding and Auth flows without redirect
                    if (currentRoute != Dest.Onboarding.route && 
                        currentRoute != Dest.Login.route && 
                        currentRoute != Dest.EmailLogin.route &&
                        currentRoute != Dest.Enroll.route &&
                        currentRoute != Dest.ForgotPassword.route) {
                        
                        AppLog.w("AuthState", "currentUser null navigate Login")
                        navController.navigate(Dest.Onboarding.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
            auth.addAuthStateListener(authListener)

            val lifecycle = act?.lifecycle
            fun reloadAndCheck() {
                val u = auth.currentUser ?: return
                u.reload().addOnCompleteListener { t ->
                    if (!t.isSuccessful) {
                        val ex = t.exception
                        if (ex is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            AppLog.e("AuthState", "invalid user signOut")
                            auth.signOut()
                            navController.navigate(Dest.Onboarding.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        return@addOnCompleteListener
                    }
                    val cur = auth.currentUser
                    val hasProvider = cur?.providerData?.isNotEmpty() == true
                    val hasEmail = cur?.email != null
                    if (!hasProvider || !hasEmail) {
                        AppLog.w("AuthState", "provider or email missing, signing out")
                        auth.signOut()
                        navController.navigate(Dest.Onboarding.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) reloadAndCheck()
            }
            lifecycle?.addObserver(lifecycleObserver)

            val destListener = NavController.OnDestinationChangedListener { _, destination, _ ->
                val route = destination.route
                if (route == Dest.Home.route || route == Dest.Profile.route) reloadAndCheck()
            }
            navController.addOnDestinationChangedListener(destListener)

            onDispose {
                lifecycle?.removeObserver(lifecycleObserver)
                auth.removeAuthStateListener(authListener)
                navController.removeOnDestinationChangedListener(destListener)
            }
        }
    }
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
            LottieCompositionSpec.RawRes(R.raw.horse)
        )
        // Use LottieAnimatable to control playback precisely
        val lottieAnimatable = rememberLottieAnimatable()
        
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
        var isSoundReady by remember { mutableStateOf(false) }
        
        // 1. Preload Sound in parallel
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val mp = MediaPlayer.create(ctx, R.raw.horse_gallop)
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
