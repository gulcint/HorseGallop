package com.horsegallop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.core.debug.AppLog
import com.horsegallop.domain.model.UserRole
import com.horsegallop.domain.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null,
    val showSplash: Boolean = true,
    val isInitialized: Boolean = false,
    val splashTitle: String? = null,
    val splashSubtitle: String? = null,
    val hasSplashError: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(MainUiState())
    val ui: StateFlow<MainUiState> = _ui

    init {
        viewModelScope.launch {
            // MAD Best Practice: Move data checks to background/coroutine to ensure Main Thread is free
            val loggedIn = authRepository.isSignedIn()
            _ui.value = _ui.value.copy(
                isLoggedIn = loggedIn,
                userRole = if (loggedIn) UserRole.CUSTOMER else null,
                hasSplashError = false
            )
        }
        val locale = Locale.getDefault().language
        loadSplashTexts(locale)
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

    override fun onCleared() {
        super.onCleared()
    }
}
