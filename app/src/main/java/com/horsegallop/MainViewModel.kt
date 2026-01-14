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
import javax.inject.Inject

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null,
    val showSplash: Boolean = true,
    val isInitialized: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(MainUiState())
    val ui: StateFlow<MainUiState> = _ui

    init {
        val loggedIn = authRepository.isSignedIn()
        _ui.value = _ui.value.copy(
            isLoggedIn = loggedIn,
            userRole = if (loggedIn) UserRole.CUSTOMER else null
        )
    }

    fun onSplashFinished() {
        _ui.value = _ui.value.copy(showSplash = false, isInitialized = true)
    }

    fun reloadUser() {
        val loggedIn = authRepository.isSignedIn()
        _ui.value = _ui.value.copy(
            isLoggedIn = loggedIn,
            userRole = if (loggedIn) UserRole.CUSTOMER else null
        )
    }

    override fun onCleared() {
        super.onCleared()
    }
}
