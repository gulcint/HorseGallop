package com.horsegallop

import android.os.Bundle
import android.content.Intent
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.horsegallop.theme.LightColorScheme
import com.horsegallop.theme.DarkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.horsegallop.navigation.Dest
import androidx.navigation.NavGraph.Companion.findStartDestination
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.airbnb.lottie.compose.*
import com.horsegallop.navigation.AppNavHost
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.widget.Toast
import com.horsegallop.feature.auth.domain.model.UserRole
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.horsegallop.core.debug.AppLog

private const val SPLASH_DURATION_MS: Long = 2000L
private const val MEDIA_VOLUME_MAX: Float = 1f
private const val LOTTIE_FILL_SCALE: Float = 0.6f

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var navControllerRef: NavHostController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()
        
        // Default web verification akışı kullanılacak; intent deep link işlemesi kaldırıldı
        val skipSplash = runCatching {
            val d = intent?.data
            d != null && d.scheme == "horsegallop" && d.host == "verify-complete"
        }.getOrDefault(false)
        setContent { AppTheme { AppContent(skipSplash) } }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setIntent(intent)
            AppLog.i("MainActivity", "onNewIntent deliver deepLink=${intent.data}")
            navControllerRef?.handleDeepLink(intent)
        }
    }
}

@Preview(showBackground = true, name = "AppContent")
@Composable
private fun PreviewAppContent() {
    AppTheme { AppContent(skipSplash = false) }
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
fun AppContent(skipSplash: Boolean): Unit {
    var showSplash: Boolean by remember { mutableStateOf(!skipSplash) }
    var splashFinished: Boolean by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val isLoggedIn = remember { FirebaseAuth.getInstance().currentUser != null }
    val owner = LocalContext.current as? MainActivity
    SideEffect { owner?.navControllerRef = navController }
    
    LaunchedEffect(splashFinished) {
        if (splashFinished) showSplash = false
    }

    
    
    // Sync system bars to current theme background for notch areas
    val activity = LocalContext.current as? ComponentActivity
    val bg = MaterialTheme.colorScheme.background
    SideEffect {
        val window = activity?.window
        window?.statusBarColor = bg.toArgb()
        window?.navigationBarColor = bg.toArgb()
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            val lum = bg.luminance()
            val light = lum > 0.5f
            controller.isAppearanceLightStatusBars = light
            controller.isAppearanceLightNavigationBars = light
        }
    }

    if (showSplash) {
		// Splash ekranında geri tuşu uygulamayı kapatır
		val act = LocalContext.current as? ComponentActivity
		BackHandler {
			// Uygulamayı kapat
			act?.finish()
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
        AppLog.d("MainActivity", "AppNavHost compose start isLoggedIn=${isLoggedIn}")
        AppNavHost(
            navController = navController,
            role = if (isLoggedIn) UserRole.CUSTOMER else null
        )
        LaunchedEffect(Unit) {
            val auth = FirebaseAuth.getInstance()
            val listener = FirebaseAuth.AuthStateListener { fa ->
                if (fa.currentUser == null) {
                    AppLog.w("AuthState", "currentUser null navigate Login")
                    navController.navigate(Dest.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            auth.addAuthStateListener(listener)
            try {
                while (true) {
                    kotlinx.coroutines.delay(60000)
                    val u = auth.currentUser ?: continue
                    u.reload().addOnCompleteListener { t ->
                        if (t.isSuccessful) {
                            val cur = auth.currentUser
                            val hasPasswordProvider = cur?.providerData?.any { it.providerId == "password" } == true
                            val hasEmail = cur?.email != null
                            if (!hasPasswordProvider || !hasEmail) {
                                AppLog.w("AuthState", "provider/email missing signOut")
                                auth.signOut()
                                navController.navigate(Dest.Login.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            val ex = t.exception
                            if (ex is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                                AppLog.e("AuthState", "invalid user signOut")
                                auth.signOut()
                                navController.navigate(Dest.Login.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            } finally {
                auth.removeAuthStateListener(listener)
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
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
@Composable
fun SplashScreen(onFinished: () -> Unit): Unit {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
		contentAlignment = Alignment.Center
	) {
		val titleText: String = stringResource(com.horsegallop.core.R.string.welcome_title)
		val subtitleText: String = stringResource(com.horsegallop.core.R.string.welcome_subtitle)
		LaunchedEffect(Unit) {
			delay(SPLASH_DURATION_MS)
			onFinished()
		}
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Image(painter = painterResource(id = com.horsegallop.R.mipmap.ic_launcher), contentDescription = null)
			Spacer(modifier = Modifier.size(12.dp))
			Text(text = titleText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
			Spacer(modifier = Modifier.size(6.dp))
			Text(text = subtitleText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
			Spacer(modifier = Modifier.size(16.dp))
			androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
		}
	}
}
