package com.horsegallop.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.core.feedback.FeedbackErrorMapper
import com.horsegallop.domain.content.usecase.GetAppContentUseCase
import com.horsegallop.domain.settings.model.UserSettings
import com.horsegallop.domain.settings.usecase.GetUserSettingsUseCase
import com.horsegallop.domain.settings.usecase.UpdateUserSettingsUseCase
import com.horsegallop.settings.AppLanguage
import com.horsegallop.settings.SettingsRepository
import com.horsegallop.settings.SettingsState
import com.horsegallop.settings.ThemeMode
import com.horsegallop.domain.privacy.usecase.RequestDataExportUseCase
import com.horsegallop.domain.privacy.usecase.DeleteUserDataUseCase
import com.horsegallop.domain.auth.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class PrivacyUiState(
    val isProcessing: Boolean = false,
    val exportJson: String? = null,
    val errorMessageResId: Int? = null
)

data class SettingsContentUiState(
    val themeSubtitle: String? = null,
    val languageSubtitle: String? = null,
    val notificationsSubtitle: String? = null,
    val privacySubtitle: String? = null
)

data class SettingsSyncUiState(
    val isInitialSyncRunning: Boolean = false,
    val isSaving: Boolean = false,
    val remoteErrorMessageResId: Int? = null,
    val saveErrorMessageResId: Int? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val requestDataExportUseCase: RequestDataExportUseCase,
    private val deleteUserDataUseCase: DeleteUserDataUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getAppContentUseCase: GetAppContentUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = settingsRepository.state
    private val _privacyState = MutableStateFlow(PrivacyUiState())
    val privacyState: StateFlow<PrivacyUiState> = _privacyState.asStateFlow()
    private val _contentState = MutableStateFlow(SettingsContentUiState())
    val contentState: StateFlow<SettingsContentUiState> = _contentState.asStateFlow()
    private val _syncState = MutableStateFlow(SettingsSyncUiState())
    val syncState: StateFlow<SettingsSyncUiState> = _syncState.asStateFlow()
    private var saveJob: Job? = null

    init {
        loadContent(uiState.value.language)
        syncSettingsFromBackend()
    }

    fun onThemeSelected(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
        syncSettingToBackend()
    }

    fun onLanguageSelected(language: AppLanguage) {
        settingsRepository.setLanguage(language)
        loadContent(language)
        syncSettingToBackend()
    }

    fun onNotificationsChanged(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
        syncSettingToBackend()
    }

    private fun syncSettingsFromBackend() {
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(
                isInitialSyncRunning = true,
                remoteErrorMessageResId = null
            )

            getUserSettingsUseCase()
                .onSuccess { remote ->
                    val remoteThemeMode = ThemeMode.fromId(remote.themeMode.lowercase())
                    val remoteLanguage = AppLanguage.fromId(remote.language.lowercase())
                    settingsRepository.replaceState(
                        themeMode = remoteThemeMode,
                        language = remoteLanguage,
                        notificationsEnabled = remote.notificationsEnabled
                    )
                    loadContent(remoteLanguage)
                    _syncState.value = _syncState.value.copy(isInitialSyncRunning = false)
                }
                .onFailure { error ->
                    FeedbackErrorMapper.logTechnicalError("SettingsViewModel.syncSettingsFromBackend", error)
                    _syncState.value = _syncState.value.copy(
                        isInitialSyncRunning = false,
                        remoteErrorMessageResId = FeedbackErrorMapper.toMessageRes(error)
                    )
                }
        }
    }

    private fun syncSettingToBackend() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            val state = settingsRepository.state.value
            _syncState.value = _syncState.value.copy(
                isSaving = true,
                saveErrorMessageResId = null
            )

            updateUserSettingsUseCase(
                    UserSettings(
                        themeMode = state.themeMode.id.uppercase(),
                        language = state.language.id.uppercase(),
                        notificationsEnabled = state.notificationsEnabled
                    )
                )
                .onSuccess {
                    _syncState.value = _syncState.value.copy(isSaving = false)
                }
                .onFailure { error ->
                    FeedbackErrorMapper.logTechnicalError("SettingsViewModel.syncSettingToBackend", error)
                    _syncState.value = _syncState.value.copy(
                        isSaving = false,
                        saveErrorMessageResId = FeedbackErrorMapper.toMessageRes(error)
                    )
                }
        }
    }

    fun requestDataExport() {
        if (_privacyState.value.isProcessing) return
        _privacyState.value = _privacyState.value.copy(isProcessing = true, errorMessageResId = null)
        viewModelScope.launch {
            requestDataExportUseCase.execute().collect { result ->
                result.onSuccess { json ->
                    _privacyState.value = _privacyState.value.copy(isProcessing = false, exportJson = json)
                }.onFailure { e ->
                    FeedbackErrorMapper.logTechnicalError("SettingsViewModel.requestDataExport", e)
                    _privacyState.value = _privacyState.value.copy(
                        isProcessing = false,
                        errorMessageResId = FeedbackErrorMapper.toMessageRes(e)
                    )
                }
            }
        }
    }

    fun requestAccountDeletion(onDeleted: () -> Unit) {
        if (_privacyState.value.isProcessing) return
        _privacyState.value = _privacyState.value.copy(isProcessing = true, errorMessageResId = null)
        viewModelScope.launch {
            deleteUserDataUseCase.execute().collect { result ->
                result.onSuccess {
                    signOutUseCase.execute().collect { }
                    _privacyState.value = _privacyState.value.copy(isProcessing = false)
                    onDeleted()
                }.onFailure { e ->
                    FeedbackErrorMapper.logTechnicalError("SettingsViewModel.requestAccountDeletion", e)
                    _privacyState.value = _privacyState.value.copy(
                        isProcessing = false,
                        errorMessageResId = FeedbackErrorMapper.toMessageRes(e)
                    )
                }
            }
        }
    }

    fun consumeExport() {
        _privacyState.value = _privacyState.value.copy(exportJson = null)
    }

    fun clearPrivacyError() {
        _privacyState.value = _privacyState.value.copy(errorMessageResId = null)
    }

    fun consumeRemoteError() {
        _syncState.value = _syncState.value.copy(remoteErrorMessageResId = null)
    }

    fun consumeSaveError() {
        _syncState.value = _syncState.value.copy(saveErrorMessageResId = null)
    }

    private fun loadContent(language: AppLanguage) {
        val locale = language.localeTag ?: Locale.getDefault().language
        viewModelScope.launch {
            getAppContentUseCase(locale).collect { result ->
                result.onSuccess { content ->
                    _contentState.value = SettingsContentUiState(
                        themeSubtitle = content.settingsThemeSubtitle,
                        languageSubtitle = content.settingsLanguageSubtitle,
                        notificationsSubtitle = content.settingsNotificationsSubtitle,
                        privacySubtitle = content.settingsPrivacySubtitle
                    )
                }
            }
        }
    }
}
