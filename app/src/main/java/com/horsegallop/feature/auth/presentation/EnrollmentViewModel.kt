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
  val error: Int? = null,
  val showDatePicker: Boolean = false,
  val verificationSent: Boolean = false,
  val verifying: Boolean = false,
  val verificationError: String? = null,
  val verificationCode: String = "",
  val showVerificationResult: Boolean = false,
  val verificationSuccess: Boolean? = null,
  val successLottieUrl: String = "",
  val errorLottieUrl: String = ""
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
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
    if (s.firstName.isBlank() || s.lastName.isBlank() || !emailValid || !hasLen) {
      _ui.value = s.copy(error = com.horsegallop.R.string.error_invalid_form)
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
      onErrorRes = { resId -> _ui.value = _ui.value.copy(loading = false, error = resId) }
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

  fun updateVerificationCode(code: String) {
    _ui.value = _ui.value.copy(verificationCode = code)
  }

  fun applyVerificationCode(onResult: (Boolean) -> Unit) {
    val code = _ui.value.verificationCode
    if (code.isBlank()) {
      _ui.value = _ui.value.copy(verificationError = "Kod boş olamaz")
      return
    }
    _ui.value = _ui.value.copy(verifying = true, verificationError = null)
    auth.applyActionCode(code)
      .addOnCompleteListener { task ->
        val ok = task.isSuccessful
        _ui.value = _ui.value.copy(verifying = false, showVerificationResult = true, verificationSuccess = ok)
        onResult(ok)
      }
  }

  fun dismissVerificationResult() {
    _ui.value = _ui.value.copy(showVerificationResult = false, verificationSuccess = null, verificationCode = "")
  }

  fun loadLottieConfig() {
    try {
      val fs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
      fs.collection("appConfig").document("lottie").get()
        .addOnSuccessListener { doc ->
          val success = doc.getString("verificationSuccessUrl") ?: "https://assets9.lottiefiles.com/packages/lf20_jbrw3hcz.json"
          val error = doc.getString("verificationErrorUrl") ?: "https://assets9.lottiefiles.com/packages/lf20_yYdx1X.json"
          _ui.value = _ui.value.copy(successLottieUrl = success, errorLottieUrl = error)
        }
        .addOnFailureListener {
          _ui.value = _ui.value.copy(
            successLottieUrl = "https://assets9.lottiefiles.com/packages/lf20_jbrw3hcz.json",
            errorLottieUrl = "https://assets9.lottiefiles.com/packages/lf20_yYdx1X.json"
          )
        }
    } catch (_: Throwable) {
      _ui.value = _ui.value.copy(
        successLottieUrl = "https://assets9.lottiefiles.com/packages/lf20_jbrw3hcz.json",
        errorLottieUrl = "https://assets9.lottiefiles.com/packages/lf20_yYdx1X.json"
      )
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
