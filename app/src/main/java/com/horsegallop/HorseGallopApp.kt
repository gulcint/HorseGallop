package com.horsegallop

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HorseGallopApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Follow device language by default; later we can persist a user choice.
        val appLocales: LocaleListCompat = LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}


