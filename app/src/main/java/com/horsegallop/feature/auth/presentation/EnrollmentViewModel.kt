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
  val showDatePicker: Boolean = false,
  val verificationSent: Boolean = false,
  val verifying: Boolean = false,
  val verificationError: String? = null
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

  fun signUp() {
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
      onSuccess = {
        // E-posta doğrulama maili use case içinde gönderiliyor
        _ui.value = _ui.value.copy(loading = false, verificationSent = true, verificationError = null)
      },
      onError = { msg -> _ui.value = _ui.value.copy(loading = false, error = msg) }
    )
  }

  fun resendVerificationEmail() {
    val user = auth.currentUser
    if (user == null) {
      _ui.value = _ui.value.copy(verificationError = "Kullanıcı oturumu bulunamadı")
      return
    }
    _ui.value = _ui.value.copy(verifying = true, verificationError = null)
    user.sendEmailVerification()
      .addOnCompleteListener { task ->
        _ui.value = _ui.value.copy(verifying = false)
        if (!task.isSuccessful) {
          _ui.value = _ui.value.copy(verificationError = task.exception?.localizedMessage ?: "Doğrulama e-postası gönderilemedi")
        } else {
          _ui.value = _ui.value.copy(verificationSent = true)
        }
      }
  }

  fun checkEmailVerified(onVerified: () -> Unit) {
    val user = auth.currentUser
    if (user == null) {
      _ui.value = _ui.value.copy(verificationError = "Kullanıcı oturumu bulunamadı")
      return
    }
    _ui.value = _ui.value.copy(verifying = true, verificationError = null)
    user.reload()
      .addOnCompleteListener { task ->
        if (!task.isSuccessful) {
          _ui.value = _ui.value.copy(verifying = false, verificationError = task.exception?.localizedMessage ?: "Doğrulama kontrolü başarısız")
        } else {
          val verified = auth.currentUser?.isEmailVerified == true
          if (verified) {
            _ui.value = _ui.value.copy(verifying = false)
            onVerified()
          } else {
            _ui.value = _ui.value.copy(verifying = false, verificationError = "E-posta henüz doğrulanmadı. Lütfen maildeki linke tıklayın.")
          }
        }
      }
  }
}
