package com.example.feature_home

import androidx.compose.material3.MaterialTheme
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class ModernHomeScreenTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material3.DayNight.NoActionBar",
        showSystemUi = false
    )

    @Test
    fun modernHomeScreen_emptyState() {
        paparazzi.snapshot {
            MaterialTheme {
                ModernHomeScreen(slides = emptyList())
            }
        }
    }

    @Test
    fun modernHomeScreen_withSlides() {
        paparazzi.snapshot {
            MaterialTheme {
                ModernHomeScreen(
                    slides = listOf(
                        com.example.domain.model.SliderItem(
                            id = "1",
                            imageUrl = "https://via.placeholder.com/800x400",
                            title = "Test Slide 1",
                            link = null,
                            order = 1
                        ),
                        com.example.domain.model.SliderItem(
                            id = "2",
                            imageUrl = "https://via.placeholder.com/800x400",
                            title = "Test Slide 2",
                            link = null,
                            order = 2
                        )
                    )
                )
            }
        }
    }
}
