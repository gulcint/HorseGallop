package com.horsegallop.feature.auth.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.horsegallop.feature.auth.domain.ResendVerificationEmailUseCase
import com.horsegallop.feature.auth.domain.ResetPasswordUseCase
import com.horsegallop.feature.auth.domain.SignInWithEmailUseCase
import com.horsegallop.feature.auth.domain.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val isFormValid: Boolean = false,
    val showResendVerification: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleClient: GoogleSignInClient,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInWithEmail: SignInWithEmailUseCase,
    private val resetPassword: ResetPasswordUseCase,
    private val resendVerificationEmail: ResendVerificationEmailUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

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

        _uiState.value = s.copy(loading = true, errorMessage = null, showResendVerification = false)
        viewModelScope.launch {
            signInWithEmail.execute(s.email, s.password)
                .collect { result ->
                    result.onSuccess { user ->
                        if (user.isEmailVerified) {
                            _uiState.value = s.copy(loading = false, success = true)
                        } else {
                            // Email doğrulanmamış, kullanıcıya bildir ama login yapmaya izin verme
                            _uiState.value = s.copy(loading = false, errorMessage = "login_verify_email_sent", showResendVerification = true)
                            // Başarı durumunu false tut ki navigation yapılmasın
                            return@collect
                        }
                    }.onFailure { e ->
                        _uiState.value = s.copy(loading = false, errorMessage = e.localizedMessage)
                    }
                }
        }
    }

    fun resendVerification() {
        val s = _uiState.value
        if (s.email.isBlank() || s.password.isBlank()) return
        
        _uiState.value = s.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // Use FirebaseAuth instance directly to bypass Repository's auto-signout on unverified email
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                // 1. Sign in
                val result = auth.signInWithEmailAndPassword(s.email, s.password).await()
                val user = result.user
                
                if (user != null) {
                    // 2. Send verification email
                    user.sendEmailVerification().await()
                    
                    // 3. Sign out (to keep security)
                    auth.signOut()
                    
                    _uiState.value = s.copy(loading = false, errorMessage = "verification_email_sent", showResendVerification = false)
                } else {
                    _uiState.value = s.copy(loading = false, errorMessage = "User not found")
                }
            } catch (e: Exception) {
                _uiState.value = s.copy(loading = false, errorMessage = "Error: ${e.message}")
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
        _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_cancelled")
    }

    fun onGoogleResult(data: Intent?) {
        if (data == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, errorMessage = null, success = false)
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val token = account.idToken
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_token_missing")
                    return@launch
                }
                try {
                    signInWithGoogle.execute(token)
                    _uiState.value = _uiState.value.copy(loading = false, success = true)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_firebase: ${e.message}")
                }
            } catch (e: ApiException) {
                _uiState.value = _uiState.value.copy(loading = false, errorMessage = "google_error_code:${e.statusCode}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_firebase: ${e.message}")
            }
        }
    }

    fun trySilentSignIn(onSignInRequired: (Intent) -> Unit) {
        viewModelScope.launch {
            val account = GoogleSignIn.getLastSignedInAccount(googleClient.applicationContext)
            if (account == null) {
                // No cached account, trigger sign-in flow
                onSignInRequired(googleClient.signInIntent)
            } else {
                // Account exists, try to get ID token
                val token = account.idToken
                if (token.isNullOrEmpty()) {
                    // Token is missing or expired, trigger sign-in flow
                    onSignInRequired(googleClient.signInIntent)
                } else {
                    // Token exists, attempt sign-in with it
                    _uiState.value = _uiState.value.copy(loading = true, errorMessage = null, success = false)
                    try {
                        signInWithGoogle.execute(token)
                        _uiState.value = _uiState.value.copy(loading = false, success = true)
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_firebase: ${e.message}")
                    }
                }
            }
        }
    }
}
