package com.horsegallop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.horsegallop.core.debug.AppLog
import com.horsegallop.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoggedIn: Boolean = FirebaseAuth.getInstance().currentUser != null,
    val userRole: UserRole? = if (FirebaseAuth.getInstance().currentUser != null) UserRole.CUSTOMER else null,
    val showSplash: Boolean = true,
    val isInitialized: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _ui = MutableStateFlow(MainUiState())
    val ui: StateFlow<MainUiState> = _ui

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val currentUser = auth.currentUser
        _ui.value = _ui.value.copy(
            isLoggedIn = currentUser != null,
            userRole = if (currentUser != null) UserRole.CUSTOMER else null
        )
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    fun onSplashFinished() {
        _ui.value = _ui.value.copy(showSplash = false, isInitialized = true)
    }

    fun reloadUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        user.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                if (task.exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    AppLog.e("MainVM", "User invalid, signing out")
                    FirebaseAuth.getInstance().signOut()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }
}
