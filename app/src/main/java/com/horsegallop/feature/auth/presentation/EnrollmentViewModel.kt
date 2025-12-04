package com.horsegallop.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.horsegallop.feature.auth.domain.SignUpWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

data class EnrollmentUiState(
  val firstName: String = "",
  val lastName: String = "",
  val birthDate: String = "",
  val email: String = "",
  val password: String = "",
  val loading: Boolean = false,
  val error: String? = null,
  val showDatePicker: Boolean = false
)

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
  private val auth: FirebaseAuth,
  private val signUpWithEmail: SignUpWithEmailUseCase
) : ViewModel() {
  private val _ui = MutableStateFlow(EnrollmentUiState())
  val ui: StateFlow<EnrollmentUiState> = _ui

  fun updateFirstName(v: String) { _ui.value = _ui.value.copy(firstName = v) }
  fun updateLastName(v: String) { _ui.value = _ui.value.copy(lastName = v) }
  fun updateEmail(v: String) { _ui.value = _ui.value.copy(email = v) }
  fun updatePassword(v: String) { _ui.value = _ui.value.copy(password = v) }
  fun toggleCountryMenu(expanded: Boolean) {}
  fun setBirthDate(date: String) { _ui.value = _ui.value.copy(birthDate = date) }
  fun setShowDatePicker(show: Boolean) { _ui.value = _ui.value.copy(showDatePicker = show) }

  fun signUp(onSuccess: () -> Unit) {
    val s = _ui.value
    val hasLen = s.password.length >= 10
    val hasUpper = s.password.any { it.isUpperCase() }
    val hasLower = s.password.any { it.isLowerCase() }
    val hasDigit = s.password.any { it.isDigit() }
    val hasSpecial = s.password.any { !it.isLetterOrDigit() }
    val strong = hasLen && hasUpper && hasLower && hasDigit && hasSpecial
    val emailValid = s.email.contains("@")
    if (s.firstName.isBlank() || s.lastName.isBlank() || !emailValid || !strong) {
      _ui.value = s.copy(error = "Geçerli bilgileri girin ve güçlü şifre kullanın")
      return
    }
    _ui.value = s.copy(loading = true, error = null)
    signUpWithEmail.execute(
      firstName = s.firstName,
      lastName = s.lastName,
      email = s.email,
      password = s.password,
      onSuccess = { _ui.value = _ui.value.copy(loading = false); onSuccess() },
      onError = { msg -> _ui.value = _ui.value.copy(loading = false, error = msg) }
    )
  }
}
