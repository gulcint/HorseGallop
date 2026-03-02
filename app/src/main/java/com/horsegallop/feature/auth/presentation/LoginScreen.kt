package com.horsegallop.feature.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.horsegallop.R
import com.horsegallop.core.components.HorseLoadingOverlay
import com.horsegallop.core.debug.AppLog
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.ui.theme.LocalSemanticColors
import com.horsegallop.ui.theme.LocalTextColors
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: LoginViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val semantic = LocalSemanticColors.current
    val feedback = LocalAppFeedbackController.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val data = res.data
        if (data == null) {
            if (res.resultCode == Activity.RESULT_OK) {
                vm.onGoogleSignInError("auth_error_google")
            } else {
                vm.onSignInCancelled()
            }
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (!token.isNullOrEmpty()) {
                vm.loginWithGoogle(token)
            } else {
                vm.onGoogleSignInError("auth_error_token_missing")
            }
        } catch (e: ApiException) {
            vm.onGoogleSignInError("google_error_code:${e.statusCode},resultCode:${res.resultCode}")
        } catch (_: Throwable) {
            vm.onGoogleSignInError("auth_error_google")
        }
    }

    LaunchedEffect(vm.effect) {
        vm.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onGoogleClick()
                is LoginEffect.ShowSnackbarError -> {
                    val msgKey = effect.message
                    val messageResId = when (msgKey) {
                        "auth_error_google" -> R.string.auth_error_google
                        "auth_error_firebase" -> R.string.auth_error_firebase
                        "auth_error_cancelled" -> R.string.auth_error_cancelled
                        "auth_error_token_missing" -> R.string.auth_error_token_missing
                        "login_verify_email_sent" -> R.string.login_verify_email_sent
                        "verification_email_sent" -> R.string.login_verify_email_sent
                        "Email not verified" -> R.string.error_email_not_verified
                        else -> when {
                            msgKey.startsWith("google_error_code:") -> {
                                AppLog.e("LoginScreen", "Google sign-in technical error: $msgKey")
                                R.string.auth_error_google
                            }
                            msgKey.startsWith("auth_error_firebase:") -> {
                                AppLog.e("LoginScreen", "Firebase auth technical error: $msgKey")
                                R.string.auth_error_firebase
                            }
                            else -> {
                                AppLog.e("LoginScreen", "Unhandled login feedback key: $msgKey")
                                R.string.error_unknown
                            }
                        }
                    }
                    if (msgKey.contains("sent")) {
                        feedback.showSuccess(messageResId)
                    } else {
                        feedback.showError(messageResId)
                    }
                }
                is LoginEffect.ShowVerificationEmailSent -> {
                    feedback.showSuccess(R.string.login_verify_email_sent)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                        semantic.screenBase
                    )
                )
            )
    ) {
        HorseLoadingOverlay(visible = uiState.isLoading)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_screen_horizontal),
                    vertical = dimensionResource(id = R.dimen.padding_screen_vertical)
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
        ) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_md)))
            LoginHeader(
                title = uiState.title ?: stringResource(R.string.login_title_brand),
                subtitle = uiState.subtitle ?: stringResource(R.string.login_subtitle)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                color = semantic.cardElevated,
                shadowElevation = dimensionResource(id = R.dimen.elevation_sm),
                border = BorderStroke(
                    dimensionResource(id = R.dimen.width_divider_thin),
                    semantic.cardStroke
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.padding_content_horizontal),
                            vertical = dimensionResource(id = R.dimen.spacing_md)
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_sm))
                ) {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = vm::updateEmail,
                        singleLine = true,
                        label = {
                            Text(
                                text = stringResource(R.string.login_email_label),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.login_email_placeholder),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = semantic.cardElevated,
                            unfocusedContainerColor = semantic.cardElevated
                        )
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = vm::updatePassword,
                        singleLine = true,
                        label = {
                            Text(
                                text = stringResource(R.string.login_password_label),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.login_password_placeholder),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = vm::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) {
                                        Icons.Filled.VisibilityOff
                                    } else {
                                        Icons.Filled.Visibility
                                    },
                                    contentDescription = null
                                )
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        visualTransformation = if (uiState.isPasswordVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (!uiState.isLoading && uiState.isFormValid) {
                                    vm.login()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = semantic.cardElevated,
                            unfocusedContainerColor = semantic.cardElevated
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                text = stringResource(R.string.forgot_password),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onEmailClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.height_button_xl)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.signin_email),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = vm::login,
                        enabled = !uiState.isLoading && uiState.isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.height_button_xl)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (uiState.isLoading) {
                                stringResource(R.string.login_button_loading)
                            } else {
                                stringResource(R.string.login_button)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (uiState.showResendVerification) {
                        TextButton(
                            onClick = vm::resendVerification,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.btn_resend_verification),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = onSignupClick) {
                            Text(
                                text = stringResource(R.string.prompt_create_account),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen.spacing_sm)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = stringResource(R.string.or_label),
                    modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.spacing_md)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            GoogleSignInButton(loading = uiState.isLoading) {
                if (!uiState.isLoading) {
                    scope.launch(Dispatchers.IO) {
                        val availability = GoogleApiAvailability
                            .getInstance()
                            .isGooglePlayServicesAvailable(context)
                        withContext(Dispatchers.Main) {
                            if (availability != ConnectionResult.SUCCESS) {
                                feedback.showError(R.string.auth_error_play_services)
                            } else {
                                val account = GoogleSignIn.getLastSignedInAccount(context)
                                if (account != null && !account.idToken.isNullOrEmpty()) {
                                    vm.loginWithGoogle(account.idToken!!)
                                } else {
                                    launcher.launch(googleClient.signInIntent)
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.terms_consent),
                style = MaterialTheme.typography.bodySmall,
                color = LocalTextColors.current.bodyTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_content_horizontal))
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.section_spacing_md)))
        }
    }
}

@Composable
private fun LoginHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_sm))
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_xxxl))
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = LocalTextColors.current.titlePrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalTextColors.current.bodySecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "LoginScreen")
@Composable
private fun PreviewLoginScreen() {
    MaterialTheme {
        LoginScreen(
            onGoogleClick = {},
            onEmailClick = {},
            onSignupClick = {}
        )
    }
}

@Composable
fun GoogleSignInButton(loading: Boolean = false, onClick: () -> Unit) {
    val semantic = LocalSemanticColors.current
    Surface(
        onClick = if (loading) ({}) else onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.height_button_xl)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
        color = semantic.cardElevated,
        shadowElevation = dimensionResource(id = R.dimen.elevation_sm),
        border = BorderStroke(
            dimensionResource(id = R.dimen.width_divider_thin),
            semantic.cardStroke
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_content_horizontal))
                .let { base -> if (loading) base.shimmer() else base }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = stringResource(R.string.cd_google_logo),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_md))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_md)))
            Text(
                text = stringResource(R.string.signin_google),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmailSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.height_button_xl)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = dimensionResource(id = R.dimen.elevation_sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_content_horizontal))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_email_icon),
                contentDescription = stringResource(R.string.cd_email_icon),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_md))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_md)))
            Text(
                text = stringResource(R.string.continue_with_email),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
