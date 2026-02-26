package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horsegallop.core.components.HorseLoadingOverlay
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            android.widget.Toast.makeText(context, context.getString(R.string.password_reset_email_sent), android.widget.Toast.LENGTH_LONG).show()
            onBack()
        }
    }
    
    LaunchedEffect(uiState.resetSuccess) {
        if (uiState.resetSuccess) {
            android.widget.Toast.makeText(context, context.getString(R.string.password_reset_success_login), android.widget.Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.forgot_password_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorseLoadingOverlay(visible = uiState.loading)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.padding_screen_horizontal)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
            ) {
                if (uiState.isResetMode) {
                    Text(
                        text = stringResource(R.string.enter_new_password),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = viewModel::updateNewPassword,
                        label = { Text(stringResource(R.string.new_password), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::updateConfirmPassword,
                        label = { Text(stringResource(R.string.confirm_password), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        isError = uiState.errorMessage != null,
                        supportingText = { 
                            if (uiState.errorMessage != null) {
                                val msg = if (uiState.errorMessage == "passwords_do_not_match") {
                                    stringResource(R.string.passwords_do_not_match)
                                } else {
                                    uiState.errorMessage!!
                                }
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        )
                    )

                    Button(
                        onClick = viewModel::confirmReset,
                        enabled = !uiState.loading && uiState.newPassword.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.height_button_xl)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.email.isNotBlank()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.change_password), style = MaterialTheme.typography.labelLarge)
                    }

                } else {
                    Text(
                        text = stringResource(R.string.forgot_password_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = { Text(stringResource(R.string.login_email_label), style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text(stringResource(R.string.login_email_placeholder), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.errorMessage != null,
                        supportingText = { 
                            if (uiState.errorMessage != null) {
                                val msg = if (uiState.errorMessage == "email_error_invalid") {
                                    stringResource(R.string.email_error_invalid)
                                } else {
                                    uiState.errorMessage!!
                                }
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            errorLabelColor = MaterialTheme.colorScheme.error
                        )
                    )

                    Button(
                        onClick = viewModel::sendResetLink,
                        enabled = !uiState.loading && uiState.email.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.height_button_xl)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.email.isNotBlank()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.send_reset_link), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
