package com.horsegallop.data.settings.repository

import com.horsegallop.data.remote.supabase.SupabaseDataSource
import com.horsegallop.data.remote.supabase.SupabaseUserSettingsDto
import com.horsegallop.domain.settings.model.UserSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserSettingsRepositoryImplTest {

    private val dataSource: SupabaseDataSource = mock()
    private lateinit var repository: UserSettingsRepositoryImpl

    @Before
    fun setUp() {
        repository = UserSettingsRepositoryImpl(dataSource)
    }

    // ─── getUserSettings ─────────────────────────────────────────────────────

    @Test
    fun `getUserSettings returns mapped UserSettings on success`() = runTest {
        whenever(dataSource.getUserSettingsById()).thenReturn(
            SupabaseUserSettingsDto(
                userId = "uid1",
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
    fun `getUserSettings returns defaults when no settings found`() = runTest {
        whenever(dataSource.getUserSettingsById()).thenReturn(null)

        val result = repository.getUserSettings()

        assertTrue(result.isSuccess)
        val settings = result.getOrNull()!!
        assertEquals("SYSTEM", settings.themeMode)
        assertEquals("SYSTEM", settings.language)
        assertEquals(true, settings.notificationsEnabled)
    }

    @Test
    fun `getUserSettings returns failure when dataSource throws`() = runTest {
        whenever(dataSource.getUserSettingsById()).thenThrow(RuntimeException("Backend error"))

        val result = repository.getUserSettings()

        assertTrue(result.isFailure)
    }

    // ─── updateUserSettings ──────────────────────────────────────────────────

    @Test
    fun `updateUserSettings returns success when dataSource completes`() = runTest {
        whenever(dataSource.currentUserId()).thenReturn("uid1")
        whenever(dataSource.upsertUserSettings(any())).thenReturn(Unit)
        val settings = UserSettings()

        val result = repository.updateUserSettings(settings)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateUserSettings returns failure when not authenticated`() = runTest {
        whenever(dataSource.currentUserId()).thenReturn(null)

        val result = repository.updateUserSettings(UserSettings())

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateUserSettings returns failure when dataSource throws`() = runTest {
        whenever(dataSource.currentUserId()).thenReturn("uid1")
        whenever(dataSource.upsertUserSettings(any())).thenThrow(RuntimeException("Update failed"))

        val result = repository.updateUserSettings(UserSettings())

        assertTrue(result.isFailure)
    }
}
