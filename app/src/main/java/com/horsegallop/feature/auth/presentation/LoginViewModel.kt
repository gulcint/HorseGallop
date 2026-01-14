package com.horsegallop.feature.auth.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.domain.auth.usecase.ResendVerificationEmailUseCase
import com.horsegallop.domain.auth.usecase.ResetPasswordUseCase
import com.horsegallop.domain.auth.usecase.SignInWithEmailUseCase
import com.horsegallop.domain.auth.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInWithEmail: SignInWithEmailUseCase,
    private val resetPassword: ResetPasswordUseCase,
    private val resendVerificationEmail: ResendVerificationEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _effect = Channel<LoginEffect>()
    val effect = _effect.receiveAsFlow()


    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
        validateForm()
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
        validateForm()
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    private fun validateForm() {
        val s = _uiState.value
        val isValid = s.email.isNotBlank() && s.password.isNotBlank()
        _uiState.value = s.copy(isFormValid = isValid)
    }

    fun login() {
        val s = _uiState.value
        if (!s.isFormValid) return

        _uiState.value = s.copy(isLoading = true, errorMessage = null, showResendVerification = false)
        viewModelScope.launch {
            signInWithEmail.execute(s.email, s.password)
                .collect { result ->
                    result.onSuccess { user ->
                        if (user.isEmailVerified) {
                            _uiState.value = s.copy(isLoading = false)
                            _effect.send(LoginEffect.NavigateToHome)
                        } else {
                            // Email doğrulanmamış, kullanıcıya bildir ama login yapmaya izin verme
                            _uiState.value = s.copy(isLoading = false, showResendVerification = true)
                            _effect.send(LoginEffect.ShowSnackbarError("login_verify_email_sent"))
                        }
                    }.onFailure { e ->
                        _uiState.value = s.copy(isLoading = false, errorMessage = e.localizedMessage)
                    }
                }
        }
    }

    fun resendVerification() {
        val s = _uiState.value
        if (s.email.isBlank() || s.password.isBlank()) return
        
        _uiState.value = s.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            resendVerificationEmail.execute(s.email, s.password)
                .collect { result ->
                    result.onSuccess {
                        _uiState.value = s.copy(isLoading = false)
                        _effect.send(LoginEffect.ShowVerificationEmailSent)
                        _effect.send(LoginEffect.ShowSnackbarError("verification_email_sent"))
                    }.onFailure { e ->
                        _uiState.value = s.copy(isLoading = false, errorMessage = e.localizedMessage)
                    }
                }
        }
    }

    fun resetPassword(email: String) {
        // This functionality might be called from UI if needed, 
        // or used in ForgotPasswordScreen's ViewModel.
        // Keeping it here if the user wants to trigger it from Login (e.g. "Forgot Password?" dialog)
        viewModelScope.launch {
             resetPassword.execute(email).collect { result ->
                 result.onSuccess {
                     // Handle success (maybe UI event)
                 }.onFailure {
                     // Handle failure
                 }
             }
        }
    }

    fun onSignInCancelled() {
        _uiState.value = _uiState.value.copy(isLoading = false)
        viewModelScope.launch {
            _effect.send(LoginEffect.ShowSnackbarError("auth_error_cancelled"))
        }
    }

    fun loginWithGoogle(token: String) {
        if (token.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            viewModelScope.launch {
                _effect.send(LoginEffect.ShowSnackbarError("auth_error_token_missing"))
            }
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                signInWithGoogle.execute(token)
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.send(LoginEffect.NavigateToHome)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _effect.send(LoginEffect.ShowSnackbarError("auth_error_firebase: ${e.message}"))
            }
        }
    }

    fun onGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false)
        viewModelScope.launch {
            _effect.send(LoginEffect.ShowSnackbarError(message))
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFormValid: Boolean = false,
    val showResendVerification: Boolean = false
)

sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
    data class ShowSnackbarError(val message: String) : LoginEffect()
    object ShowVerificationEmailSent : LoginEffect()
}
