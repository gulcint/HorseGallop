package com.horsegallop.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.horsegallop.feature.auth.domain.SignUpWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
import com.horsegallop.core.debug.AppLog
import android.content.Context

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
  val resendCooldownRemaining: Int = 0,
  
)

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
  private val auth: FirebaseAuth,
  private val signUpWithEmail: SignUpWithEmailUseCase,
  @ApplicationContext private val appContext: Context
) : ViewModel() {
  private val _ui = MutableStateFlow(EnrollmentUiState())
  val ui: StateFlow<EnrollmentUiState> = _ui

  fun updateFirstName(v: String) { _ui.value = _ui.value.copy(firstName = v) }
  fun updateLastName(v: String) { _ui.value = _ui.value.copy(lastName = v) }
  fun updateEmail(v: String) { _ui.value = _ui.value.copy(email = v) }
  fun updatePassword(v: String) { _ui.value = _ui.value.copy(password = v) }
  
  fun setBirthDate(date: String) { _ui.value = _ui.value.copy(birthDate = date) }
  fun setShowDatePicker(show: Boolean) { _ui.value = _ui.value.copy(showDatePicker = show) }

  fun signUp() {
    val s = _ui.value
    val hasLen = s.password.length >= 10
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
    AppLog.d("AuthSignUp", "signUp start first=${s.firstName.isNotBlank()} last=${s.lastName.isNotBlank()} emailValid=$emailValid hasLen=$hasLen")
    if (s.firstName.isBlank() || s.lastName.isBlank() || !emailValid || !hasLen) {
      AppLog.w("AuthSignUp", "invalid form")
      _ui.value = s.copy(error = com.horsegallop.R.string.error_invalid_form)
      return
    }
    AppLog.d("AuthSignUp", "calling use case")
    _ui.value = s.copy(loading = true, error = null)
    signUpWithEmail.execute(
      firstName = s.firstName,
      lastName = s.lastName,
      email = s.email,
      password = s.password,
      onSuccess = {
        AppLog.d("AuthSignUp", "use case success; verificationSent=true")
        _ui.value = _ui.value.copy(loading = false, verificationSent = true, verificationError = null)
      },
      onErrorRes = { resId ->
        AppLog.e("AuthSignUp", "use case error resId=$resId")
        _ui.value = _ui.value.copy(loading = false, error = resId)
      }
    )
  }

  fun resendVerificationEmail() {
    AppLog.d("AuthSignUp", "resendVerificationEmail start")
    val user = auth.currentUser
    if (user == null) {
      AppLog.e("AuthSignUp", "resendVerificationEmail no current user")
      _ui.value = _ui.value.copy(verificationError = "Kullanıcı oturumu bulunamadı")
      return
    }
    val cooldown = _ui.value.resendCooldownRemaining
    if (cooldown > 0) {
      AppLog.w("AuthSignUp", "resend blocked by cooldown remaining=${cooldown}s")
      return
    }
    _ui.value = _ui.value.copy(verifying = true, verificationError = null)
    user.sendEmailVerification()
      .addOnCompleteListener { task ->
        _ui.value = _ui.value.copy(verifying = false)
        if (!task.isSuccessful) {
          val ex = task.exception
          val msgRes = when (ex) {
            is com.google.firebase.auth.FirebaseAuthException -> {
              when (ex.errorCode) {
                "ERROR_TOO_MANY_REQUESTS" -> com.horsegallop.R.string.error_too_many_requests
                "ERROR_QUOTA_EXCEEDED" -> com.horsegallop.R.string.error_quota_exceeded
                else -> null
              }
            }
            else -> null
          }
          val finalMsg = if (msgRes != null) appContext.getString(msgRes) else appContext.getString(com.horsegallop.R.string.error_verification_email_failed)
          AppLog.e("AuthSignUp", "resendVerificationEmail failed: ${ex?.localizedMessage}")
          _ui.value = _ui.value.copy(verificationError = finalMsg)
        } else {
          AppLog.d("AuthSignUp", "resendVerificationEmail success")
          _ui.value = _ui.value.copy(verificationSent = true)
        }
        startResendCooldown(60)
      }
  }

  private fun startResendCooldown(seconds: Int) {
    viewModelScope.launch {
      var s = seconds
      while (s > 0) {
        _ui.value = _ui.value.copy(resendCooldownRemaining = s)
        kotlinx.coroutines.delay(1000)
        s -= 1
      }
      _ui.value = _ui.value.copy(resendCooldownRemaining = 0)
    }
  }

  

fun checkEmailVerified(onVerified: () -> Unit) {
    AppLog.d("AuthSignUp", "checkEmailVerified start")
    val user = auth.currentUser
    if (user == null) {
      AppLog.e("AuthSignUp", "checkEmailVerified no current user")
      _ui.value = _ui.value.copy(verificationError = "Kullanıcı oturumu bulunamadı")
      return
    }
    _ui.value = _ui.value.copy(verifying = true, verificationError = null)
    user.reload()
      .addOnCompleteListener { task ->
        if (!task.isSuccessful) {
          val msg = task.exception?.localizedMessage ?: "Doğrulama kontrolü başarısız"
          AppLog.e("AuthSignUp", "checkEmailVerified reload failed: $msg")
          _ui.value = _ui.value.copy(verifying = false, verificationError = msg)
        } else {
          val verified = auth.currentUser?.isEmailVerified == true
          AppLog.d("AuthSignUp", "checkEmailVerified verified=$verified")
          if (verified) {
            _ui.value = _ui.value.copy(verifying = false)
            onVerified()
          } else {
            _ui.value = _ui.value.copy(verifying = false)
          }
        }
      }
}
}
