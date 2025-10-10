package com.horsegallop.feature_auth

import androidx.compose.material3.MaterialTheme
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.DayNight.NoActionBar",
        showSystemUi = false
    )

    @Test
    fun loginScreen_defaultState() {
        paparazzi.snapshot {
            MaterialTheme {
                com.horsegallop.feature_auth.LoginScreen(
                    onGoogleClick = {},
                    onAppleClick = {},
                    onEmailClick = {}
                )
            }
        }
    }

    @Test
    fun loginScreen_withCallbacks() {
        paparazzi.snapshot {
            MaterialTheme {
                com.horsegallop.feature_auth.LoginScreen(
                    onGoogleClick = {},
                    onAppleClick = {},
                    onEmailClick = {}
                )
            }
        }
    }
}
