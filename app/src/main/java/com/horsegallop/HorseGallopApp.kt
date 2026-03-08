package com.horsegallop

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.horsegallop.core.debug.AppLog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HorseGallopApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        // Follow device language by default; later we can persist a user choice.
        val appLocales: LocaleListCompat = LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(appLocales)

        // Initialize FCM token on app start (PushService.onNewToken handles updates)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            AppLog.i("HorseGallopApp", "FCM token ready: ${token.take(20)}...")
        }.addOnFailureListener {
            AppLog.e("HorseGallopApp", "FCM token fetch failed: $it")
        }
    }
}


