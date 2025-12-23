package com.horsegallop.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horsegallop.core.debug.AppLog
import com.horsegallop.feature.auth.domain.ResendVerificationEmailUseCase
import com.horsegallop.feature.auth.domain.SignUpWithEmailUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.horsegallop.BuildConfig
import com.horsegallop.domain.auth.AuthValidator

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
  val isNameValid: Boolean = false,
  val isEmailValid: Boolean = false,
  val passwordStrengthScore: Int = 0,
  val passwordMissingCriteria: List<Int> = emptyList(),
  val isFormValid: Boolean = false,
  val validationErrors: List<Int> = emptyList(),
  val currentUserEmail: String? = null,
  val showVerificationResult: Boolean = false,
  val verificationSuccess: Boolean? = null,
  val verificationCode: String = "",
  val successLottieUrl: String = "",
  val errorLottieUrl: String = "",
  val errorMessage: String? = null

)

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
  private val signUpWithEmail: SignUpWithEmailUseCase,
  private val resendVerificationEmail: ResendVerificationEmailUseCase,
  private val authValidator: AuthValidator
) : ViewModel() {
  private val _ui = MutableStateFlow(EnrollmentUiState())
  val ui: StateFlow<EnrollmentUiState> = _ui

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
    val nameValid = authValidator.validateName(s.firstName, s.lastName)
    val emailValid = authValidator.validateEmail(s.email)
    val score = authValidator.calculatePasswordStrength(s.password)
    val strong = authValidator.isPasswordStrongEnough(s.password)

    val missing = mutableListOf<Int>()
    if (!nameValid) missing.add(com.horsegallop.R.string.error_name_required)
    if (!emailValid) missing.add(com.horsegallop.R.string.error_email_invalid)
    
    if (s.password.length < 10) missing.add(com.horsegallop.R.string.error_password_length)
    if (!strong && s.password.isNotEmpty()) {
        if (!s.password.any { it.isUpperCase() }) missing.add(com.horsegallop.R.string.password_needs_upper)
        if (!s.password.any { it.isLowerCase() }) missing.add(com.horsegallop.R.string.password_needs_lower)
        if (!s.password.any { it.isDigit() }) missing.add(com.horsegallop.R.string.password_needs_digit)
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
      // AppLog.i("EnrollmentUI", "auto-enroll ${uri}")
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
  

  fun setBirthDate(date: String) { _ui.value = _ui.value.copy(birthDate = date) }
  fun setShowDatePicker(show: Boolean) { _ui.value = _ui.value.copy(showDatePicker = show) }

  fun signUp() {
    val s = _ui.value
    val valid = authValidator.validateName(s.firstName, s.lastName) && 
                authValidator.validateEmail(s.email) && 
                authValidator.isPasswordStrongEnough(s.password)
                
    if (!valid) {
      _ui.value = s.copy(error = com.horsegallop.R.string.error_invalid_form)
      return
    }
    _ui.value = s.copy(loading = true, error = null)

    
    viewModelScope.launch {
        signUpWithEmail.execute(
            email = s.email,
            password = s.password,
            firstName = s.firstName,
            lastName = s.lastName
        )
        .onCompletion { cause: Throwable? ->
            if (cause != null) {
                // If flow failed with exception/cancellation not caught downstream
                _ui.value = _ui.value.copy(loading = false, error = com.horsegallop.R.string.error_signup_failed)
            } else {
                // Flow completed normally, ensure loading is off if not already
                // (though onSuccess/onFailure should have handled it)
                if (_ui.value.loading) {
                    _ui.value = _ui.value.copy(loading = false)
                }
            }
        }
        .collect { result ->
            result.onSuccess {
                AppLog.d("AuthSignUp", "use case success; verificationSent=true")
                _ui.value = _ui.value.copy(loading = false, verificationSent = true, verificationError = null, currentUserEmail = s.email)
            }.onFailure { e ->
                AppLog.e("AuthSignUp", "use case error ${e.localizedMessage}")
                if (BuildConfig.DEBUG) {
                    _ui.value = _ui.value.copy(loading = false, errorMessage = "Debug Error: ${e.message}", error = null)
                } else {
                    _ui.value = _ui.value.copy(loading = false, error = com.horsegallop.R.string.error_signup_failed, errorMessage = null)
                }
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
                // AppLog.d("AuthSignUp", "resendVerificationEmail success")
                _ui.value = _ui.value.copy(verificationSent = true)
                startResendCooldown(60)
            }.onFailure { e ->
                // AppLog.e("AuthSignUp", "resendVerificationEmail failed: ${e.localizedMessage}")
                val msg = "Could not send verification email. Please try again."
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

      }
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


  fun checkEmailVerified(onVerified: (Boolean) -> Unit) {
    viewModelScope.launch {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                 user.reload().addOnCompleteListener { task: com.google.android.gms.tasks.Task<Void> ->
                     if (task.isSuccessful) {
                         val isVerified = user.isEmailVerified
                         if (isVerified) {
                             try {
                                 val s = _ui.value
                                 val displayName = listOf(s.firstName, s.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { user.displayName ?: "" }
                                 val data = mapOf(
                                     "id" to user.uid,
                                     "role" to com.horsegallop.domain.model.UserRole.CUSTOMER.name,
                                     "firstName" to s.firstName,
                                     "lastName" to s.lastName,
                                     "name" to displayName,
                                     "email" to (user.email ?: ""),
                                     "createdAt" to com.google.firebase.Timestamp.now()
                                 )
                                 com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                     .collection("users").document(user.uid)
                                     .set(data, com.google.firebase.firestore.SetOptions.merge())
                                     .addOnCompleteListener { onVerified(true) }
                             } catch (_: Exception) {
                                 onVerified(true)
                             }
                         } else {
                             // Still not verified
                             // AppLog.d("AuthSignUp", "Email not verified yet")
                         }
                     }
                 }
             } catch (e: Exception) {
                // AppLog.e("AuthSignUp", "Error reloading user: ${e.localizedMessage}")
            }
        }
    }
  }
}
