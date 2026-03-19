package com.horsegallop.feature.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Landing / auth-selection screen.
 * Shows Google and Email sign-in options — no inline form here.
 * Email form lives in [EmailLoginScreen].
 */
@Composable
fun LoginScreen(
    // AppNav wires this for Google-success navigation
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

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
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
                    val resId = when (msgKey) {
                        "auth_error_google" -> R.string.auth_error_google
                        "auth_error_cancelled" -> R.string.auth_error_cancelled
                        "auth_error_token_missing" -> R.string.auth_error_token_missing
                        "login_verify_email_sent",
                        "verification_email_sent" -> R.string.login_verify_email_sent
                        "Email not verified" -> R.string.error_email_not_verified
                        else -> when {
                            msgKey.startsWith("auth_error_firebase") -> R.string.auth_error_firebase
                            msgKey.startsWith("google_error_code:") -> R.string.auth_error_google
                            else -> {
                                AppLog.e("LoginScreen", "Unhandled key: $msgKey")
                                R.string.error_unknown
                            }
                        }
                    }
                    if (msgKey.contains("sent")) feedback.showSuccess(resId)
                    else feedback.showError(resId)
                }
                is LoginEffect.ShowVerificationEmailSent ->
                    feedback.showSuccess(R.string.login_verify_email_sent)
            }
        }
    }

        LoginScreenContent(
            uiState = uiState,
            onGoogleClick = {
                if (!uiState.isLoading && uiState.agreementAccepted) {
                    scope.launch(Dispatchers.IO) {
                        val available = GoogleApiAvailability.getInstance()
                            .isGooglePlayServicesAvailable(context)
                        withContext(Dispatchers.Main) {
                            if (available != ConnectionResult.SUCCESS) {
                                feedback.showError(R.string.auth_error_play_services)
                            } else {
                                googleClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleClient.signInIntent)
                                }
                            }
                        }
                    }
                }
            },
            onEmailClick = onEmailClick,
            onSignupClick = onSignupClick,
            onToggleAgreement = { vm.toggleAgreement() }
        )
}

/** Test edilebilir içerik composable — ViewModel bağımlılığı yok. */
@Composable
internal fun LoginScreenContent(
    uiState: LoginUiState,
    onGoogleClick: () -> Unit,
    onEmailClick: () -> Unit,
    onSignupClick: () -> Unit,
    onToggleAgreement: () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                        semantic.screenBase,
                        semantic.screenBase
                    )
                )
            )
    ) {
        HorseLoadingOverlay(visible = uiState.isLoading)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.45f))

            // ic_launcher_round: tam daire, brown arka plan + horse — beyaz kare yok
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_round),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = LocalTextColors.current.titlePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalTextColors.current.bodySecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.45f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = semantic.cardElevated,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, semantic.cardStroke)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.login_title_brand),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LocalTextColors.current.titlePrimary
                    )

                    AuthOptionButton(
                        title = stringResource(R.string.signin_google),
                        subtitle = stringResource(R.string.login_google_helper),
                        enabled = !uiState.isLoading,
                        modifier = Modifier,
                        onClick = onGoogleClick,
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = stringResource(R.string.cd_google_logo),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = stringResource(R.string.or_label),
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    AuthOptionButton(
                        title = stringResource(R.string.signin_email),
                        subtitle = stringResource(R.string.login_email_helper),
                        onClick = onEmailClick,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .semantics { testTag = "email_login_button" },
                        accentTint = MaterialTheme.colorScheme.primary,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.agreementAccepted,
                            onCheckedChange = { onToggleAgreement() },
                            modifier = Modifier.semantics {
                                testTag = "agreement_checkbox"
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val textColor = LocalTextColors.current.bodySecondary
                        val termsText = stringResource(R.string.agreement_terms_link)
                        val privacyText = stringResource(R.string.agreement_privacy_link)
                        val fullLabel = buildAnnotatedString {
                            // Android XML trims trailing/leading spaces — explicit boşluklar ekleniyor
                            append(stringResource(R.string.agreement_label_prefix).trimEnd())
                            append(" ")
                            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                                append(termsText)
                            }
                            append(" ")
                            append(stringResource(R.string.agreement_label_connector).trim())
                            append(" ")
                            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                                append(privacyText)
                            }
                            append(stringResource(R.string.agreement_label_suffix))
                        }
                        Text(
                            text = fullLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignupClick) {
                Text(
                    text = stringResource(R.string.prompt_create_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, name = "LoginScreen – agreement unchecked")
@Composable
private fun PreviewLoginScreen() {
    MaterialTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, name = "LoginScreen – agreement checked")
@Composable
private fun PreviewLoginScreenAgreementAccepted() {
    MaterialTheme {
        LoginScreen()
    }
}

@Composable
private fun AuthOptionButton(
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    icon: @Composable () -> Unit
) {
    val semantic = LocalSemanticColors.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Surface(
        onClick = if (enabled) onClick else ({}),
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) semantic.cardElevated else semantic.cardSubtle,
        shadowElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            if (enabled) semantic.cardStroke else semantic.cardStroke.copy(alpha = 0.45f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        accentTint.copy(alpha = if (isDark) 0.18f else 0.12f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.login_continue_short),
                style = MaterialTheme.typography.labelMedium,
                color = accentTint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
