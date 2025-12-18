package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.feature.auth.presentation.LoginViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.horsegallop.core.R

@Composable
fun EmailLoginScreen(
  onBack: () -> Unit,
  onSignup: () -> Unit,
  onSignedIn: () -> Unit,
  viewModel: LoginViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.success) {
    if (uiState.success) {
      snackbarHostState.showSnackbar(context.getString(R.string.auth_success))
      onSignedIn()
    }
  }

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { errorKey ->
      val message = when (errorKey) {
        "login_verify_email_sent" -> context.getString(R.string.login_verify_email_sent)
        "auth_error_cancelled" -> context.getString(com.horsegallop.core.R.string.auth_error_cancelled)
        "auth_error_token_missing" -> context.getString(com.horsegallop.core.R.string.auth_error_token_missing)
        "auth_error_firebase" -> context.getString(com.horsegallop.core.R.string.auth_error_firebase)
        else -> errorKey
      }
      snackbarHostState.showSnackbar(message)
    }
  }

  EmailLoginContent(
    uiState = uiState,
    onEmailChange = viewModel::updateEmail,
    onPasswordChange = viewModel::updatePassword,
    onLoginClick = viewModel::login,
    onSignupClick = onSignup,
    onBackClick = onBack,
    snackbarHostState = snackbarHostState
  )
}

@Composable
fun EmailLoginContent(
  uiState: com.horsegallop.feature.auth.presentation.LoginUiState,
  onEmailChange: (String) -> Unit,
  onPasswordChange: (String) -> Unit,
  onLoginClick: () -> Unit,
  onSignupClick: () -> Unit,
  onBackClick: () -> Unit,
  snackbarHostState: SnackbarHostState
) {
  Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Column(
      modifier = Modifier.fillMaxWidth().align(Alignment.Center),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(text = stringResource(R.string.signin_email), style = MaterialTheme.typography.titleLarge)
      OutlinedTextField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.login_email_label)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        ),
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.login_password_label)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        ),
        modifier = Modifier.fillMaxWidth()
      )
<<<<<<< Updated upstream
      if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error)
      Button(onClick = {
        if (email.isBlank() || password.length < 6) {
          error = "Geçerli email ve 6+ karakter şifre girin"
=======
      
      Button(
        onClick = onLoginClick,
        enabled = !uiState.loading,
        modifier = Modifier.fillMaxWidth()
      ) { 
        if (uiState.loading) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
>>>>>>> Stashed changes
        } else {
          Text(stringResource(R.string.login_button)) 
        }
      }
      TextButton(onClick = onSignupClick) { Text(stringResource(R.string.login_signup_prompt)) }
      TextButton(onClick = onBackClick) { Text(stringResource(R.string.back)) }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
  }
}

@Preview(showBackground = true, name = "EmailLoginScreen")
@Composable
private fun PreviewEmailLoginScreen() {
  MaterialTheme {
    EmailLoginContent(
      uiState = com.horsegallop.feature.auth.presentation.LoginUiState(),
      onEmailChange = {},
      onPasswordChange = {},
      onLoginClick = {},
      onSignupClick = {},
      onBackClick = {},
      snackbarHostState = remember { SnackbarHostState() }
    )
  }
}
