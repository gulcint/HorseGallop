package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EmailLoginScreen(
  onBack: () -> Unit,
  onSignup: () -> Unit,
  onSignedIn: () -> Unit
) {
  val context = androidx.compose.ui.platform.LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var loading by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Column(
      modifier = Modifier.fillMaxWidth().align(Alignment.Center),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(text = "Email ile Giriş", style = MaterialTheme.typography.titleLarge)
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        )
      )
      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Şifre") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        )
      )
      if (error != null) Text(text = error.orEmpty(), color = com.horsegallop.core.theme.LocalTextColors.current.error)
      Button(onClick = {
        if (email.isBlank() || password.length < 6) {
          error = "Geçerli email ve 6+ karakter şifre girin"
        } else {
          loading = true
          error = null
          val auth = FirebaseAuth.getInstance()
          auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
              val user = auth.currentUser
              loading = false
              if (user != null && user.isEmailVerified) {
                scope.launch {
                  snackbarHostState.currentSnackbarData?.dismiss()
                  snackbarHostState.showSnackbar("Giriş başarılı")
                  onSignedIn()
                }
              } else {
                scope.launch {
                  snackbarHostState.currentSnackbarData?.dismiss()
                  snackbarHostState.showSnackbar("Lütfen e-postanı doğrula. Doğrulama maili gönderildi.")
                }
                runCatching { user?.sendEmailVerification() }
                auth.signOut()
              }
            }
            .addOnFailureListener { e -> loading = false; error = e.localizedMessage ?: "Giriş başarısız" }
        }
      }, enabled = !loading) { Text("Giriş Yap") }
      TextButton(onClick = onSignup) { Text("Hesabın yok mu? Kaydol") }
      TextButton(onClick = onBack) { Text("Geri") }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
  }
}

@Preview(showBackground = true, name = "EmailLoginScreen")
@Composable
private fun PreviewEmailLoginScreen() {
  MaterialTheme {
    EmailLoginScreen(onBack = {}, onSignup = {}, onSignedIn = {})
  }
}
