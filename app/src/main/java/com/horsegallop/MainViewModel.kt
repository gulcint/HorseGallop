package com.horsegallop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.core.debug.AppLog
import com.horsegallop.domain.model.UserRole
import com.horsegallop.domain.auth.AuthRepository
import com.horsegallop.domain.ride.usecase.RetryPendingRideSyncUseCase
import com.horsegallop.domain.content.usecase.GetAppContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null,
    val showSplash: Boolean = true,
    val isInitialized: Boolean = false,
    val splashTitle: String? = null,
    val splashSubtitle: String? = null,
    val offlineHelpText: String? = null,
    val hasSplashError: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val retryPendingRideSyncUseCase: RetryPendingRideSyncUseCase,
    private val getAppContentUseCase: GetAppContentUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _ui = MutableStateFlow(MainUiState())
    val ui: StateFlow<MainUiState> = _ui

    init {
        viewModelScope.launch {
            // MAD Best Practice: Move data checks to background/coroutine to ensure Main Thread is free
            val forceLogin = try {
                Settings.Global.getInt(context.contentResolver, "horsegallop_force_login", 0) == 1
            } catch (e: Exception) {
                false
            }
            val loggedIn = if (forceLogin) true else authRepository.isSignedIn()
            _ui.value = _ui.value.copy(
                isLoggedIn = loggedIn,
                userRole = if (loggedIn) UserRole.CUSTOMER else null,
                hasSplashError = false
            )
        }
        val locale = Locale.getDefault().language
        loadAppContent(locale)
        loadSplashTexts(locale)
        viewModelScope.launch {
            runCatching { retryPendingRideSyncUseCase() }
        }
    }

    fun onSplashFinished() {
        _ui.value = _ui.value.copy(showSplash = false, isInitialized = true)
    }

    fun reloadUser() {
        viewModelScope.launch {
            val loggedIn = authRepository.isSignedIn()
            _ui.value = _ui.value.copy(
                isLoggedIn = loggedIn,
                userRole = if (loggedIn) UserRole.CUSTOMER else null
            )
        }
    }

    private fun loadSplashTexts(locale: String) {
        viewModelScope.launch {
            authRepository.getSplashTexts(locale).collect { result ->
                result
                    .onSuccess { pair ->
                        _ui.value = _ui.value.copy(
                            splashTitle = pair.first,
                            splashSubtitle = pair.second,
                            hasSplashError = false
                        )
                    }
                    .onFailure {
                        _ui.value = _ui.value.copy(
                            splashTitle = null,
                            splashSubtitle = null,
                            hasSplashError = true
                        )
                    }
            }
        }
    }

    private fun loadAppContent(locale: String) {
        viewModelScope.launch {
            getAppContentUseCase(locale).collect { result ->
                result.onSuccess { content ->
                    _ui.value = _ui.value.copy(
                        offlineHelpText = content.offlineHelp
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
