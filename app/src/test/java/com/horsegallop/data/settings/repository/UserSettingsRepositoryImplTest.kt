package com.horsegallop.data.settings.repository

import com.horsegallop.data.remote.dto.UserSettingsFunctionsDto
import com.horsegallop.data.remote.functions.AppFunctionsDataSource
import com.horsegallop.domain.settings.model.UserSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserSettingsRepositoryImplTest {

    private val dataSource: AppFunctionsDataSource = mock()
    private lateinit var repository: UserSettingsRepositoryImpl

    @Before
    fun setUp() {
        repository = UserSettingsRepositoryImpl(dataSource)
    }

    // ─── getUserSettings ─────────────────────────────────────────────────────

    @Test
    fun `getUserSettings returns mapped UserSettings on success`() = runTest {
        whenever(dataSource.getUserSettings()).thenReturn(
            UserSettingsFunctionsDto(
                themeMode = "DARK",
                language = "tr",
                notificationsEnabled = false,
                weightUnit = "lb",
                distanceUnit = "mi"
            )
        )

        val result = repository.getUserSettings()

        assertTrue(result.isSuccess)
        val settings = result.getOrNull()!!
        assertEquals("DARK", settings.themeMode)
        assertEquals("tr", settings.language)
        assertEquals(false, settings.notificationsEnabled)
        assertEquals("lb", settings.weightUnit)
        assertEquals("mi", settings.distanceUnit)
    }

    @Test
    fun `getUserSettings returns failure when dataSource throws`() = runTest {
        whenever(dataSource.getUserSettings()).thenThrow(RuntimeException("Backend error"))

        val result = repository.getUserSettings()

        assertTrue(result.isFailure)
    }

    // ─── updateUserSettings ──────────────────────────────────────────────────

    @Test
    fun `updateUserSettings passes all fields to dataSource`() = runTest {
        val settings = UserSettings(
            themeMode = "LIGHT",
            language = "en",
            notificationsEnabled = true,
            weightUnit = "kg",
            distanceUnit = "km"
        )

        repository.updateUserSettings(settings)

        verify(dataSource).updateUserSettings(
            themeMode = "LIGHT",
            language = "en",
            notificationsEnabled = true,
            weightUnit = "kg",
            distanceUnit = "km"
        )
    }

    @Test
    fun `updateUserSettings returns success when dataSource completes`() = runTest {
        val settings = UserSettings()

        val result = repository.updateUserSettings(settings)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateUserSettings returns failure when dataSource throws`() = runTest {
        whenever(
            dataSource.updateUserSettings(
                themeMode = "SYSTEM", language = "SYSTEM",
                notificationsEnabled = true, weightUnit = "kg", distanceUnit = "km"
            )
        ).thenThrow(RuntimeException("Update failed"))

        val result = repository.updateUserSettings(UserSettings())

        assertTrue(result.isFailure)
    }
}
