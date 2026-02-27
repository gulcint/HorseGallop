package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.HorseLoadingOverlay
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedback = LocalAppFeedbackController.current
    val semantic = LocalSemanticColors.current

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            feedback.showSuccess(R.string.password_reset_email_sent)
            onBack()
        }
    }

    LaunchedEffect(uiState.resetSuccess) {
        if (uiState.resetSuccess) {
            feedback.showSuccess(R.string.password_reset_success_login)
            onBack()
        }
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.forgot_password_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.20f),
                            semantic.screenBase
                        )
                    )
                )
        ) {
            HorseLoadingOverlay(visible = uiState.loading)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_screen_horizontal),
                        vertical = dimensionResource(id = R.dimen.padding_screen_vertical)
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
            ) {
                ForgotPasswordHero(isResetMode = uiState.isResetMode)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                    color = semantic.cardElevated,
                    border = BorderStroke(1.dp, semantic.cardStroke)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.padding_card_lg)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
                    ) {
                        if (uiState.isResetMode) {
                            Text(
                                text = stringResource(R.string.enter_new_password),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = uiState.newPassword,
                                onValueChange = viewModel::updateNewPassword,
                                label = { Text(stringResource(R.string.new_password), style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = semantic.cardSubtle,
                                    unfocusedContainerColor = semantic.cardSubtle
                                )
                            )

                            OutlinedTextField(
                                value = uiState.confirmPassword,
                                onValueChange = viewModel::updateConfirmPassword,
                                label = { Text(stringResource(R.string.confirm_password), style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                isError = uiState.errorMessage != null,
                                supportingText = {
                                    uiState.errorMessage?.let {
                                        Text(
                                            text = if (it == "passwords_do_not_match") stringResource(R.string.passwords_do_not_match) else it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    errorLabelColor = MaterialTheme.colorScheme.error,
                                    focusedContainerColor = semantic.cardSubtle,
                                    unfocusedContainerColor = semantic.cardSubtle,
                                    errorContainerColor = semantic.cardSubtle
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
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.change_password),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.forgot_password_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = viewModel::updateEmail,
                                label = { Text(stringResource(R.string.login_email_label), style = MaterialTheme.typography.bodySmall) },
                                placeholder = { Text(stringResource(R.string.login_email_placeholder), style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                isError = uiState.errorMessage != null,
                                supportingText = {
                                    uiState.errorMessage?.let {
                                        Text(
                                            text = if (it == "email_error_invalid") stringResource(R.string.email_error_invalid) else it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
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
                                    errorLabelColor = MaterialTheme.colorScheme.error,
                                    focusedContainerColor = semantic.cardSubtle,
                                    unfocusedContainerColor = semantic.cardSubtle,
                                    errorContainerColor = semantic.cardSubtle
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
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.send_reset_link),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.back),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordHero(isResetMode: Boolean) {
    val semantic = LocalSemanticColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = semantic.cardElevated,
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isResetMode) Icons.Filled.Lock else Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (isResetMode) R.string.change_password else R.string.forgot_password_title
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        if (isResetMode) R.string.enter_new_password else R.string.forgot_password_subtitle
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
