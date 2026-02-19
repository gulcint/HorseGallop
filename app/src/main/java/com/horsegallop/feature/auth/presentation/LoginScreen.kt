package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.horsegallop.R
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay
import androidx.compose.ui.res.dimensionResource
import com.valentinilk.shimmer.shimmer
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import android.util.Patterns
import androidx.compose.ui.text.TextStyle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import com.horsegallop.core.components.HorseLoadingOverlay
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import com.horsegallop.core.theme.AppColors
import com.horsegallop.core.theme.LocalTextColors

@Composable
fun LoginScreen(
    onGoogleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: LoginViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()

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
                    val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    val msg = when (msgKey) {
                        "auth_error_google" -> context.getString(com.horsegallop.core.R.string.auth_error_google)
                        "auth_error_firebase" -> context.getString(com.horsegallop.core.R.string.auth_error_firebase)
                        "auth_error_cancelled" -> context.getString(com.horsegallop.core.R.string.auth_error_cancelled)
                        "auth_error_token_missing" -> context.getString(com.horsegallop.core.R.string.auth_error_token_missing)
                        "login_verify_email_sent" -> context.getString(com.horsegallop.core.R.string.login_verify_email_sent)
                        "verification_email_sent" -> "Verification email sent"
                        "Email not verified" -> context.getString(com.horsegallop.R.string.error_email_not_verified)
                        else -> {
                            if (msgKey.startsWith("google_error_code:")) {
                                val code = msgKey.removePrefix("google_error_code:")
                                if (isDebug) {
                                    "Google Sign-In error code: $code"
                                } else {
                                    context.getString(com.horsegallop.core.R.string.auth_error_google)
                                }
                            } else if (msgKey.startsWith("auth_error_firebase: ")) {
                                val error = msgKey.removePrefix("auth_error_firebase: ")
                                if (isDebug) {
                                    "Authentication failed: $error"
                                } else {
                                    context.getString(com.horsegallop.core.R.string.auth_error_firebase)
                                }
                            } else {
                                context.getString(com.horsegallop.core.R.string.error_unknown)
                            }
                        }
                    }
                    showLogoToast(context, msg, !msgKey.contains("sent"))
                }
                is LoginEffect.ShowVerificationEmailSent -> {
                    showLogoToast(context, "Verification email sent", false)
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val msgKey = uiState.errorMessage
        if (msgKey != null) {
            // Toast shown in effect collection above
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5EFE6),  // Soft cream
                        Color(0xFFFFFFFF)    // White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal),
                    vertical = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_vertical)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Modern Header with Animated Horse Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
            ) {
                AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xxxl))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.SaddleBrown.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = stringResource(com.horsegallop.core.R.string.app_name),
                            modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xxxl))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(com.horsegallop.core.R.string.login_title_brand),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.horsegallop.core.theme.LocalTextColors.current.titlePrimary
                )
                
                Text(
                    text = stringResource(com.horsegallop.core.R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.horsegallop.core.theme.LocalTextColors.current.bodySecondary,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
            
            // Glassmorphism Login Card
            val focusManager = LocalFocusManager.current
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl)),
                shadowElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_md),
                border = androidx.compose.foundation.BorderStroke(
                    dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_content_horizontal),
                            vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_lg)
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
                ) {
                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = vm::updateEmail,
                        singleLine = true,
                        label = { Text(stringResource(com.horsegallop.core.R.string.login_email_label), style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text(stringResource(com.horsegallop.core.R.string.login_email_placeholder), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = vm::updatePassword,
                        singleLine = true,
                        label = { Text(stringResource(com.horsegallop.core.R.string.login_password_label), style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text(stringResource(com.horsegallop.core.R.string.login_password_placeholder), style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = vm::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium,
                        visualTransformation = if (uiState.isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                text = stringResource(com.horsegallop.core.R.string.forgot_password),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In Button
                    Button(
                        onClick = vm::login,
                        enabled = !uiState.isLoading && uiState.isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
                        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                        elevation = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ).elevation
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(com.horsegallop.core.R.string.login_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resend Verification (if needed)
                    if (uiState.showResendVerification) {
                        TextButton(onClick = vm::resendVerification) {
                            Text(
                                text = "Resend Verification Email",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider with Or label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Text(
                            text = stringResource(com.horsegallop.core.R.string.or_label),
                            modifier = Modifier.padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign In Button
                    GoogleSignInButton(loading = uiState.isLoading, onClick = {
                        if (!uiState.isLoading) {
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    if (availability != ConnectionResult.SUCCESS) {
                                        showLogoToast(context, context.getString(com.horsegallop.core.R.string.auth_error_play_services), true)
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
                    })

                    Spacer(modifier = Modifier.height(12.dp))

                    // Create Account Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(com.horsegallop.core.R.string.no_account_prefix),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onSignupClick) {
                            Text(
                                text = stringResource(com.horsegallop.core.R.string.prompt_create_account),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Terms Consent
                    Text(
                        text = stringResource(com.horsegallop.core.R.string.terms_consent),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.horsegallop.core.theme.LocalTextColors.current.bodyTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_lg)))
            
            // Quick Actions / Features Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
            ) {
                Text(
                    text = stringResource(com.horsegallop.core.R.string.feature_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureItem(
                        icon = Icons.Filled.PedalCycle,
                        label = stringResource(com.horsegallop.core.R.string.feature_ride),
                        color = MaterialTheme.colorScheme.primary
                    )
                    FeatureItem(
                        icon = Icons.Filled.House,
                        label = stringResource(com.horsegallop.core.R.string.feature_barns),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    FeatureItem(
                        icon = Icons.Filled.Star,
                        label = stringResource(com.horsegallop.core.R.string.feature_leaderboard),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .border(
                    androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.3f)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun showLogoToast(context: android.content.Context, text: String, isError: Boolean) {
    val density = context.resources.displayMetrics.density
    fun dp(v: Int) = (v * density).toInt()
    val container = LinearLayout(context)
    container.orientation = LinearLayout.HORIZONTAL
    container.gravity = Gravity.CENTER_VERTICAL
    container.setPadding(dp(12), dp(10), dp(12), dp(10))
    val bg = GradientDrawable()
    bg.cornerRadius = dp(12).toFloat()
    bg.setColor(0xFFFFFFFF.toInt())
    bg.setStroke(dp(1), if (isError) 0xFFE57373.toInt() else 0xFFB0BEC5.toInt())
    container.background = bg
    val icon = ImageView(context)
    icon.setImageResource(com.horsegallop.R.mipmap.ic_launcher)
    val iconLp = LinearLayout.LayoutParams(dp(20), dp(20))
    iconLp.rightMargin = dp(8)
    icon.layoutParams = iconLp
    val tv = TextView(context)
    tv.text = text
    tv.setTextColor(0xFF212121.toInt())
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    val toast = android.widget.Toast(context)
    toast.duration = android.widget.Toast.LENGTH_SHORT
    toast.view = container.apply {
        addView(icon)
        addView(tv)
    }
    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(96))
    toast.show()
}

@Preview(showBackground = true)
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
    Surface(
        onClick = if (loading) ({}) else onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm),
        border = androidx.compose.foundation.BorderStroke(
            dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
            MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_content_horizontal))
                .let { m -> if (loading) m.shimmer() else m }
        ) {
            Image(
                painter = painterResource(id = com.horsegallop.R.drawable.ic_google_logo),
                contentDescription = stringResource(com.horsegallop.core.R.string.cd_google_logo),
                modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_md))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
            Text(
                text = stringResource(com.horsegallop.core.R.string.signin_google),
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
            .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_content_horizontal))
        ) {
            Image(
                painter = painterResource(id = com.horsegallop.R.drawable.ic_email_icon),
                contentDescription = stringResource(com.horsegallop.core.R.string.cd_email_icon),
                modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_md))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
            Text(
                text = stringResource(com.horsegallop.core.R.string.continue_with_email),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
