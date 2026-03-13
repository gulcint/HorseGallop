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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        loadContent(Locale.getDefault().language)
        syncSettingsFromBackend()
    }

    fun onThemeSelected(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
        syncSettingToBackend()
    }

    fun onLanguageSelected(language: AppLanguage) {
        settingsRepository.setLanguage(language)
        syncSettingToBackend()
    }

    fun onNotificationsChanged(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
        syncSettingToBackend()
    }

    private fun syncSettingsFromBackend() {
        viewModelScope.launch {
            getUserSettingsUseCase().onSuccess { remote ->
                // Apply remote theme and language only if they differ from local default
                val localState = settingsRepository.state.value
                if (localState.themeMode == ThemeMode.SYSTEM && remote.themeMode != "SYSTEM") {
                    runCatching { settingsRepository.setThemeMode(ThemeMode.fromId(remote.themeMode)) }
                }
                if (localState.language == AppLanguage.SYSTEM && remote.language != "SYSTEM") {
                    runCatching { settingsRepository.setLanguage(AppLanguage.fromId(remote.language)) }
                }
            }
        }
    }

    private fun syncSettingToBackend() {
        viewModelScope.launch {
            val state = settingsRepository.state.value
            runCatching {
                updateUserSettingsUseCase(
                    UserSettings(
                        themeMode = state.themeMode.id.uppercase(),
                        language = state.language.id.uppercase(),
                        notificationsEnabled = state.notificationsEnabled
                    )
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

    private fun loadContent(locale: String) {
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
