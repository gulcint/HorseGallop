package com.horsegallop.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.feature.auth.domain.ConfirmPasswordResetUseCase
import com.horsegallop.feature.auth.domain.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val resetCode: String? = null, // From deep link
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isResetMode: Boolean = false, // True if we are in the "enter new password" phase
    val resetSuccess: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password)
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password)
    }

    fun sendResetLink() {
        val email = _uiState.value.email
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "email_error_invalid")
            return
        }

        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            sendPasswordResetEmailUseCase.execute(email).collect { result ->
                result.onSuccess {
                    _uiState.value = _uiState.value.copy(loading = false, success = true)
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(loading = false, errorMessage = e.localizedMessage)
                }
            }
        }
    }

    fun handleDeepLink(code: String) {
        _uiState.value = _uiState.value.copy(resetCode = code, isResetMode = true)
    }

    fun confirmReset() {
        val s = _uiState.value
        if (s.resetCode == null) return
        if (s.newPassword.length < 6) {
             _uiState.value = s.copy(errorMessage = "password_error_min_length")
             return
        }
        if (s.newPassword != s.confirmPassword) {
            _uiState.value = s.copy(errorMessage = "passwords_do_not_match")
            return
        }

        _uiState.value = s.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            confirmPasswordResetUseCase.execute(s.resetCode, s.newPassword).collect { result ->
                 result.onSuccess {
                     _uiState.value = _uiState.value.copy(loading = false, resetSuccess = true)
                 }.onFailure { e ->
                     _uiState.value = _uiState.value.copy(loading = false, errorMessage = e.localizedMessage)
                 }
            }
        }
    }
}
