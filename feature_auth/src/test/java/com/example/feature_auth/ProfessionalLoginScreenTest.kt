package com.horsegallop.feature_auth

import androidx.compose.material3.MaterialTheme
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class ProfessionalLoginScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.DayNight.NoActionBar",
        showSystemUi = false
    )

    @Test
    fun professionalLoginScreen_defaultState() {
        paparazzi.snapshot {
            MaterialTheme {
                ProfessionalLoginScreen()
            }
        }
    }

    @Test
    fun professionalLoginScreen_withCallbacks() {
        paparazzi.snapshot {
            MaterialTheme {
                ProfessionalLoginScreen(
                    onGoogleClick = {},
                    onAppleClick = {},
                    onEmailClick = {}
                )
            }
        }
    }
}
