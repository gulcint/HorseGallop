package com.horsegallop.feature.auth.domain

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

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
    auth.fetchSignInMethodsForEmail(email)
      .addOnSuccessListener { methods ->
        val exists = methods.signInMethods?.isNotEmpty() == true
        if (exists) {
          onErrorRes(com.horsegallop.R.string.error_email_exists)
          return@addOnSuccessListener
        }
        auth.createUserWithEmailAndPassword(email, password)
          .addOnSuccessListener { result ->
            val user = result.user
            val uid = user?.uid
            if (uid == null) {
              onErrorRes(com.horsegallop.R.string.error_user_create_failed)
            } else {
              val userMap = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email
              )
              firestore.collection("users").document(uid).set(userMap)
                .addOnSuccessListener {
                  user.sendEmailVerification()
                  onSuccess()
                }
                .addOnFailureListener { _ -> onErrorRes(com.horsegallop.R.string.error_data_save_failed) }
            }
          }
          .addOnFailureListener { e ->
            onErrorRes(com.horsegallop.R.string.error_signup_failed)
          }
      }
      .addOnFailureListener { _ -> onErrorRes(com.horsegallop.R.string.error_signup_check_failed) }
  }
}
