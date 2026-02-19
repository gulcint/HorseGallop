package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.horsegallop.core.R
import com.horsegallop.core.theme.AppColors
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPasswordStrength by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            android.widget.Toast.makeText(context, context.getString(R.string.password_reset_email_sent), android.widget.Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    LaunchedEffect(uiState.resetSuccess) {
        if (uiState.resetSuccess) {
            android.widget.Toast.makeText(context, context.getString(R.string.password_reset_success_login), android/widget.Toast.LENGTH_LONG).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.forgot_password_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                    .padding(dimensionResource(id = R.dimen.padding_screen_horizontal))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Modern Header Card
                Surface(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_xl)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = dimensionResource(id = R.dimen.elevation_sm),
                    border = androidx.compose.foundation.BorderStroke(
                        dimensionResource(id = R.dimen.width_divider_thin),
                        MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_lg)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
                    ) {
                        // Icon Card
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(AppColors.ActionLesson.copy(alpha = 0.15f))
                                .border(
                                    androidx.compose.foundation.BorderStroke(
                                        dimensionResource(id = R.dimen.width_divider_thin),
                                        AppColors.ActionLesson
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = AppColors.ActionLesson,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = if (uiState.isResetMode) {
                                stringResource(R.string.reset_password)
                            } else {
                                stringResource(R.string.forgot_password_title)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (uiState.isResetMode) {
                                stringResource(R.string.enter_new_password)
                            } else {
                                stringResource(R.string.forgot_password_subtitle)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isResetMode) {
                    // Reset Password Form
                    PasswordStrengthIndicator(
                        password = uiState.newPassword,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.updateNewPassword(it) },
                        label = { Text(stringResource(R.string.new_password), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (uiState.newPassword.length >= 6) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        supportingText = {
                            if (uiState.newPassword.length > 0 && uiState.newPassword.length < 6) {
                                Text(
                                    text = stringResource(R.string.password_too_short),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        label = { Text(stringResource(R.string.confirm_password), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        isError = uiState.errorMessage != null,
                        supportingText = {
                            if (uiState.errorMessage != null) {
                                val msg = when (uiState.errorMessage) {
                                    "passwords_do_not_match" -> stringResource(R.string.passwords_do_not_match)
                                    else -> uiState.errorMessage!!
                                }
                                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            } else if (uiState.newPassword.length >= 6) {
                                val strength = PasswordStrengthIndicator.calculateStrength(uiState.newPassword)
                                if (strength > 0) {
                                    Text(
                                        text = when (strength) {
                                            1 -> stringResource(R.string.password_weak)
                                            2 -> stringResource(R.string.password_fair)
                                            3 -> stringResource(R.string.password_strong)
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when (strength) {
                                            1 -> AppColors.Warning
                                            2 -> AppColors.Info
                                            3 -> AppColors.Success
                                            else -> Color.Transparent
                                        }
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (uiState.errorMessage == null && uiState.confirmPassword.length > 0) {
                                MaterialTheme.colorScheme.primary
                            } else if (uiState.errorMessage != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                            },
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::confirmReset,
                        enabled = !uiState.loading && 
                                uiState.newPassword.length >= 6 && 
                                uiState.confirmPassword == uiState.newPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.height_button_xl)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.email.isNotBlank()) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.change_password), style = MaterialTheme.typography.labelLarge)
                    }

                } else {
                    // Email Reset Form
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text(stringResource(R.string.login_email_label), style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text(stringResource(R.string.login_email_placeholder), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.errorMessage != null,
                        supportingText = {
                            if (uiState.errorMessage != null) {
                                val msg = when (uiState.errorMessage) {
                                    "email_error_invalid" -> stringResource(R.string.email_error_invalid)
                                    else -> uiState.errorMessage!!
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
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::sendResetLink,
                        enabled = !uiState.loading && uiState.email.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.height_button_xl)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.email.isNotBlank()) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.send_reset_link), style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Helper Card
                Surface(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        dimensionResource(id = R.dimen.width_divider_thin),
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.padding_card_md))
                            .clickable { /* TODO: Open help */ },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_sm))
                    ) {
                        Icon(
                            Icons.Filled.Help,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.help_center),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.stringNeed_help_resetting_your_password),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = PasswordStrengthIndicator.calculateStrength(password)
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val isFilled = index < strength
            val color = when (index) {
                0 -> AppColors.Warning
                1 -> AppColors.Info
                2 -> AppColors.Success
                else -> Color.Gray
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isFilled) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline
                    )
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, color),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }

    if (password.length > 0) {
        Text(
            text = when (strength) {
                1 -> stringResource(R.string.password_weak)
                2 -> stringResource(R.string.password_fair)
                3 -> stringResource(R.string.password_strong)
                else -> ""
            },
            style = MaterialTheme.typography.labelSmall,
            color = when (strength) {
                1 -> AppColors.Warning
                2 -> AppColors.Info
                3 -> AppColors.Success
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

object PasswordStrengthIndicator {
    fun calculateStrength(password: String): Int {
        var score = 0
        
        if (password.length >= 6) score++
        if (password.length >= 10) score++
        
        val hasNumber = password.any { it.isDigit() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        
        if (hasNumber) score++
        if (hasUpper && hasLower) score++
        if (hasSpecial) score++
        
        return minOf(3, score / 2)
    }
}
