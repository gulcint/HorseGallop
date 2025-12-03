package com.horsegallop.feature.auth.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.horsegallop.feature.auth.domain.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class LoginUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleClient: GoogleSignInClient,
    private val signInWithGoogle: SignInWithGoogleUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

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
                    _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_firebase")
                }
            } catch (e: ApiException) {
                val key = when (e.statusCode) {
                    CommonStatusCodes.NETWORK_ERROR -> "error_network"
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "auth_error_cancelled"
                    GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "auth_error_google"
                    CommonStatusCodes.INTERNAL_ERROR -> "error_unknown"
                    else -> "auth_error_google"
                }
                _uiState.value = _uiState.value.copy(loading = false, errorMessage = key)
            }
        }
    }

    fun onSignInCancelled() {
        _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_cancelled")
    }

    fun signInIntent(): Intent = googleClient.signInIntent

    fun trySilentSignIn(onIntent: (Intent?) -> Unit) {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null, success = false)
        googleClient.silentSignIn()
            .addOnSuccessListener { account ->
                val token = account.idToken
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_token_missing")
                } else {
                    try {
                        viewModelScope.launch {
                            try {
                                signInWithGoogle.execute(token)
                                _uiState.value = _uiState.value.copy(loading = false, success = true)
                            } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_firebase")
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(loading = false, errorMessage = "auth_error_firebase")
                    }
                }
            }
            .addOnFailureListener { onIntent(googleClient.signInIntent) }
    }
}
