package com.example.feature_auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalizedContent

@Composable
fun LoginScreen(
  onGoogleClick: () -> Unit,
  onAppleClick: () -> Unit,
  onEmailClick: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(text = LocalizedContent.getString(com.example.core.R.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
    Button(onClick = onGoogleClick, modifier = Modifier.padding(top = 16.dp)) {
      Text(text = LocalizedContent.getString(com.example.core.R.string.signin_google))
    }
    Button(onClick = onAppleClick, modifier = Modifier.padding(top = 8.dp)) {
      Text(text = LocalizedContent.getString(com.example.core.R.string.signin_apple))
    }
    Button(onClick = onEmailClick, modifier = Modifier.padding(top = 8.dp)) {
      Text(text = LocalizedContent.getString(com.example.core.R.string.signin_email))
    }
  }
}

@Preview
@Composable
private fun LoginPreview() {
  LoginScreen(onGoogleClick = {}, onAppleClick = {}, onEmailClick = {})
}
