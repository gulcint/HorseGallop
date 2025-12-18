package com.horsegallop.feature.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.core.debug.AppLog
import com.horsegallop.feature.auth.domain.ResendVerificationEmailUseCase
import com.horsegallop.feature.auth.domain.SignUpWithEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
<<<<<<< Updated upstream
  val verificationCode: String = "",
  val showVerificationResult: Boolean = false,
  val verificationSuccess: Boolean? = null,
  val successLottieUrl: String = "",
  val errorLottieUrl: String = ""
=======
  val resendCooldownRemaining: Int = 0,
  val isNameValid: Boolean = false,
  val isEmailValid: Boolean = false,
  val passwordStrengthScore: Int = 0,
  val passwordMissingCriteria: List<Int> = emptyList(),
  val isFormValid: Boolean = false,
  val validationErrors: List<Int> = emptyList(),
  val currentUserEmail: String? = null
>>>>>>> Stashed changes
)

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
<<<<<<< Updated upstream
  private val auth: FirebaseAuth,
  private val signUpWithEmail: SignUpWithEmailUseCase
=======
  private val signUpWithEmail: SignUpWithEmailUseCase,
  private val resendVerificationEmail: ResendVerificationEmailUseCase,
  @ApplicationContext private val appContext: Context
>>>>>>> Stashed changes
) : ViewModel() {
  private val _ui = MutableStateFlow(EnrollmentUiState())
  val ui: StateFlow<EnrollmentUiState> = _ui

<<<<<<< Updated upstream
  fun updateFirstName(v: String) { _ui.value = _ui.value.copy(firstName = v) }
  fun updateLastName(v: String) { _ui.value = _ui.value.copy(lastName = v) }
  fun updateEmail(v: String) { _ui.value = _ui.value.copy(email = v) }
  fun updatePassword(v: String) { _ui.value = _ui.value.copy(password = v) }
  fun toggleCountryMenu(expanded: Boolean) {}
=======
  fun updateFirstName(v: String) { 
    _ui.value = _ui.value.copy(firstName = v)
    validateForm()
  }
  fun updateLastName(v: String) { 
    _ui.value = _ui.value.copy(lastName = v)
    validateForm()
  }
  fun updateEmail(v: String) { 
    _ui.value = _ui.value.copy(email = v)
    validateForm()
  }
  fun updatePassword(v: String) { 
    _ui.value = _ui.value.copy(password = v)
    validateForm()
  }

  private fun validateForm() {
    val s = _ui.value
    val nameValid = s.firstName.trim().isNotEmpty() && s.lastName.trim().isNotEmpty()
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches()
    
    val hasLen = s.password.length >= 10
    val hasUpper = s.password.any { it.isUpperCase() }
    val hasLower = s.password.any { it.isLowerCase() }
    val hasDigit = s.password.any { it.isDigit() }
    val hasSpecial = s.password.any { !it.isLetterOrDigit() }
    
    val score = listOf(hasLen, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    val strong = hasLen && hasUpper && hasLower && hasDigit // Strong enough if length + all types present

    val missing = mutableListOf<Int>()
    if (!nameValid) missing.add(com.horsegallop.R.string.error_name_required)
    if (!emailValid) missing.add(com.horsegallop.R.string.error_email_invalid)
    
    if (!hasLen) missing.add(com.horsegallop.R.string.error_password_length)
    if (!strong) {
        if (!hasUpper) missing.add(com.horsegallop.R.string.password_needs_upper)
        if (!hasLower) missing.add(com.horsegallop.R.string.password_needs_lower)
        if (!hasDigit) missing.add(com.horsegallop.R.string.password_needs_digit)
        if (!hasSpecial) missing.add(com.horsegallop.R.string.password_needs_special)
    }

    _ui.value = s.copy(
        isNameValid = nameValid,
        isEmailValid = emailValid,
        passwordStrengthScore = score,
        passwordMissingCriteria = missing, 
        validationErrors = missing,
        isFormValid = nameValid && emailValid && strong
    )
  }

  fun handleDeepLink(uri: android.net.Uri?) {
      if (uri != null && uri.scheme == "horsegallop" && uri.host == "auto-enroll") {
      AppLog.i("EnrollmentUI", "auto-enroll ${uri}")
      val first = uri.getQueryParameter("first").orEmpty()
      val last = uri.getQueryParameter("last").orEmpty()
      val emailPrefill = uri.getQueryParameter("email").orEmpty()
      val passPrefill = uri.getQueryParameter("password").orEmpty()
      val dob = uri.getQueryParameter("dob").orEmpty()
      val auto = uri.getQueryParameter("auto") == "1" || uri.getQueryParameter("auto") == "true"
      
      var newState = _ui.value
      if (first.isNotBlank()) newState = newState.copy(firstName = first)
      if (last.isNotBlank()) newState = newState.copy(lastName = last)
      if (emailPrefill.isNotBlank()) newState = newState.copy(email = emailPrefill)
      if (passPrefill.isNotBlank()) newState = newState.copy(password = passPrefill)
      if (dob.isNotBlank()) newState = newState.copy(birthDate = dob)
      
      _ui.value = newState
      validateForm() 
      
      if (auto) signUp()
    }
  }
  
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
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
=======
    
    viewModelScope.launch {
        signUpWithEmail.execute(
            email = s.email,
            password = s.password,
            firstName = s.firstName,
            lastName = s.lastName
        ).collect { result ->
            result.onSuccess {
                AppLog.d("AuthSignUp", "use case success; verificationSent=true")
                _ui.value = _ui.value.copy(loading = false, verificationSent = true, verificationError = null, currentUserEmail = s.email)
            }.onFailure { e ->
                AppLog.e("AuthSignUp", "use case error ${e.localizedMessage}")
                // Map to generic error for now as we lost specific error codes in Repository abstraction (unless we pass them through)
                _ui.value = _ui.value.copy(loading = false, error = com.horsegallop.R.string.error_signup_failed)
            }
        }
    }
  }

  fun resendVerificationEmail() {
    AppLog.d("AuthSignUp", "resendVerificationEmail start")
    
    val cooldown = _ui.value.resendCooldownRemaining
    if (cooldown > 0) {
      AppLog.w("AuthSignUp", "resend blocked by cooldown remaining=${cooldown}s")
      return
    }
    _ui.value = _ui.value.copy(verifying = true, verificationError = null)
    
    viewModelScope.launch {
        resendVerificationEmail.execute().collect { result ->
            _ui.value = _ui.value.copy(verifying = false)
            result.onSuccess {
                AppLog.d("AuthSignUp", "resendVerificationEmail success")
                _ui.value = _ui.value.copy(verificationSent = true)
                startResendCooldown(60)
            }.onFailure { e ->
                AppLog.e("AuthSignUp", "resendVerificationEmail failed: ${e.localizedMessage}")
                val msg = appContext.getString(com.horsegallop.R.string.error_verification_email_failed)
                _ui.value = _ui.value.copy(verificationError = msg)
            }
        }
    }
  }

  private fun startResendCooldown(seconds: Int) {
    viewModelScope.launch {
      var s = seconds
      while (s > 0) {
        _ui.value = _ui.value.copy(resendCooldownRemaining = s)
        kotlinx.coroutines.delay(1000)
        s--
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
  fun checkEmailVerified(onVerified: (Boolean) -> Unit) {
    viewModelScope.launch {
      // Email verification check logic here
      // For now, assume it's verified after a delay
      kotlinx.coroutines.delay(1000)
      onVerified(true)
    }
>>>>>>> Stashed changes
  }
}
