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
    onError: (String) -> Unit
  ) {
    auth.fetchSignInMethodsForEmail(email)
      .addOnSuccessListener { methods ->
        val exists = methods.signInMethods?.isNotEmpty() == true
        if (exists) {
          onError("Bu e-posta zaten kayıtlı")
          return@addOnSuccessListener
        }
        auth.createUserWithEmailAndPassword(email, password)
          .addOnSuccessListener { result ->
            val user = result.user
            val uid = user?.uid
            if (uid == null) {
              onError("Kullanıcı oluşturulamadı")
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
                .addOnFailureListener { e -> onError(e.localizedMessage ?: "Veri kaydı başarısız") }
            }
          }
          .addOnFailureListener { e ->
            val msg = e.localizedMessage ?: "Kayıt başarısız"
            onError(msg)
          }
      }
      .addOnFailureListener { e -> onError(e.localizedMessage ?: "Kayıt kontrolü başarısız") }
  }
}
