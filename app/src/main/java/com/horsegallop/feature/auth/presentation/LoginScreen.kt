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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                        "auth_error_firebase" -> R.string.auth_error_firebase
                        "auth_error_cancelled" -> R.string.auth_error_cancelled
                        "auth_error_token_missing" -> R.string.auth_error_token_missing
                        "login_verify_email_sent",
                        "verification_email_sent" -> R.string.login_verify_email_sent
                        "Email not verified" -> R.string.error_email_not_verified
                        else -> {
                            AppLog.e("LoginScreen", "Unhandled key: $msgKey")
                            R.string.error_unknown
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
            // ── Hero ──────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.weight(0.45f))

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(96.dp)
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

            // ── Auth card ─────────────────────────────────────────────────────
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

                    // Google — first option
                    GoogleSignInButton(loading = uiState.isLoading) {
                        if (!uiState.isLoading) {
                            scope.launch(Dispatchers.IO) {
                                val available = GoogleApiAvailability.getInstance()
                                    .isGooglePlayServicesAvailable(context)
                                withContext(Dispatchers.Main) {
                                    if (available != ConnectionResult.SUCCESS) {
                                        feedback.showError(R.string.auth_error_play_services)
                                    } else {
                                        val acct = GoogleSignIn.getLastSignedInAccount(context)
                                        if (acct != null && !acct.idToken.isNullOrEmpty()) {
                                            vm.loginWithGoogle(acct.idToken!!)
                                        } else {
                                            launcher.launch(googleClient.signInIntent)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Divider
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

                    // Email — second option (navigates to EmailLoginScreen)
                    OutlinedButton(
                        onClick = onEmailClick,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_email_icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.signin_email),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign-up link
            TextButton(onClick = onSignupClick) {
                Text(
                    text = stringResource(R.string.prompt_create_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Terms
            Text(
                text = stringResource(R.string.terms_consent),
                style = MaterialTheme.typography.bodySmall,
                color = LocalTextColors.current.bodyTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, name = "LoginScreen")
@Composable
private fun PreviewLoginScreen() {
    MaterialTheme {
        LoginScreen()
    }
}

@Composable
fun GoogleSignInButton(loading: Boolean = false, onClick: () -> Unit) {
    val semantic = LocalSemanticColors.current
    Surface(
        onClick = if (loading) ({}) else onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = semantic.cardElevated,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .let { base -> if (loading) base.shimmer() else base }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = stringResource(R.string.cd_google_logo),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
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
    val semantic = LocalSemanticColors.current
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_email_icon),
                contentDescription = stringResource(R.string.cd_email_icon),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.continue_with_email),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
