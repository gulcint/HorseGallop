package com.horsegallop.feature.auth.domain

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.horsegallop.core.debug.AppLog
 
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpWithEmailUseCase @Inject constructor(
  private val auth: FirebaseAuth,
  private val firestore: FirebaseFirestore
) {
  fun execute(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onErrorRes: (Int) -> Unit
  ) {
    AppLog.d("SignUpUseCase", "execute start email=$email")
    auth.fetchSignInMethodsForEmail(email)
      .addOnSuccessListener { methods ->
        val exists = methods.signInMethods?.isNotEmpty() == true
        AppLog.d("SignUpUseCase", "fetchSignInMethods exists=$exists methods=${methods.signInMethods}")
        if (exists) {
          AppLog.w("SignUpUseCase", "email already exists")
          onErrorRes(com.horsegallop.R.string.error_email_exists)
          return@addOnSuccessListener
        }
        AppLog.d("SignUpUseCase", "creating user")
        auth.createUserWithEmailAndPassword(email, password)
          .addOnSuccessListener { result ->
            val user = result.user
            val uid = user?.uid
            AppLog.d("SignUpUseCase", "createUser success uid=$uid")
            if (uid == null) {
              AppLog.e("SignUpUseCase", "uid null after createUser")
              onErrorRes(com.horsegallop.R.string.error_user_create_failed)
            } else {
              val profile = UserProfileChangeRequest.Builder()
                .setDisplayName("$firstName $lastName")
                .build()
              user.updateProfile(profile)
                .addOnFailureListener { e -> AppLog.w("SignUpUseCase", "updateProfile failed: ${e.localizedMessage}") }

              val userMap = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
              )

              AppLog.d("SignUpUseCase", "sending verify email (default)")
              user.sendEmailVerification()
                .addOnCompleteListener { t ->
                  AppLog.d("SignUpUseCase", "sendEmailVerification completed ok=${t.isSuccessful}")
                }
                .addOnFailureListener { e ->
                  AppLog.e("SignUpUseCase", "sendEmailVerification failed: ${e.localizedMessage}")
                }

              onSuccess()

              AppLog.d("SignUpUseCase", "writing users/$uid doc")
              firestore.collection("users").document(uid).set(userMap)
                .addOnSuccessListener {
                  AppLog.d("SignUpUseCase", "user doc written")
                }
                .addOnFailureListener { e ->
                  AppLog.e("SignUpUseCase", "firestore write failed: ${e.localizedMessage}")
                }
            }
          }
          .addOnFailureListener { e ->
            AppLog.e("SignUpUseCase", "createUser failed: ${e.localizedMessage}")
            val res = when (e) {
              is FirebaseAuthUserCollisionException -> com.horsegallop.R.string.error_email_exists
              is FirebaseAuthWeakPasswordException -> com.horsegallop.R.string.error_weak_password
              is FirebaseAuthInvalidCredentialsException -> com.horsegallop.R.string.error_invalid_email
              is FirebaseAuthException ->
                when (e.errorCode) {
                  "ERROR_OPERATION_NOT_ALLOWED" -> com.horsegallop.R.string.error_operation_not_allowed
                  "ERROR_NETWORK_REQUEST_FAILED" -> com.horsegallop.R.string.error_network
                  else -> com.horsegallop.R.string.error_signup_failed
                }
              else -> com.horsegallop.R.string.error_signup_failed
            }
            onErrorRes(res)
          }
      }
      .addOnFailureListener { e ->
        AppLog.e("SignUpUseCase", "fetchSignInMethods failed: ${e.localizedMessage}")
        onErrorRes(com.horsegallop.R.string.error_signup_check_failed)
      }
  }
}
