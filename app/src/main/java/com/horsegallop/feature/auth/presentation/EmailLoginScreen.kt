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
import com.horsegallop.core.R as CoreR
import com.horsegallop.R as AppR
import androidx.compose.ui.graphics.Color

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

  LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
      when (effect) {
        is LoginEffect.NavigateToHome -> {
          snackbarHostState.showSnackbar(context.getString(CoreR.string.auth_success))
          onSignedIn()
        }
        is LoginEffect.ShowSnackbarError -> {
             val message = when (effect.message) {
                 "login_verify_email_sent" -> context.getString(CoreR.string.login_verify_email_sent)
                 "auth_error_cancelled" -> context.getString(CoreR.string.auth_error_cancelled)
                 "auth_error_token_missing" -> context.getString(CoreR.string.auth_error_token_missing)
                 "auth_error_firebase" -> context.getString(CoreR.string.auth_error_firebase)
                 "Email not verified" -> context.getString(AppR.string.error_email_not_verified)
                 else -> effect.message
             }
             snackbarHostState.showSnackbar(message)
         }
         is LoginEffect.ShowVerificationEmailSent -> {
              snackbarHostState.showSnackbar(context.getString(CoreR.string.login_verify_email_sent))
         }
      }
    }
  }

  // Removed redundant LaunchedEffect(uiState.errorMessage) as errors are handled by LoginEffect

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

      
      Button(
        onClick = onLoginClick,
        enabled = !uiState.isLoading,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
      ) { 
        if (uiState.isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)

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
