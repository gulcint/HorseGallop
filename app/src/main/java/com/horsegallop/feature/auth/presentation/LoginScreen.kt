package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.horsegallop.theme.AppColors
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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.delay
import androidx.compose.ui.res.dimensionResource
import com.valentinilk.shimmer.shimmer
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusDirection
import android.util.Patterns
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import android.content.Context
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.view.Gravity
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import com.horsegallop.compose.HorseLoadingOverlay
 

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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) vm.onGoogleResult(res.data) else vm.onSignInCancelled()
    }

    LaunchedEffect(uiState.errorMessage) {
        val msgKey = uiState.errorMessage
        if (msgKey != null) {
            val msg = when (msgKey) {
                "auth_error_google" -> context.getString(com.horsegallop.core.R.string.auth_error_google)
                "auth_error_firebase" -> context.getString(com.horsegallop.core.R.string.auth_error_firebase)
                "auth_error_token_missing" -> context.getString(com.horsegallop.core.R.string.auth_error_token_missing)
                "login_verify_email_sent" -> context.getString(com.horsegallop.core.R.string.login_verify_email_sent)
                "Email not verified" -> context.getString(com.horsegallop.R.string.error_email_not_verified)
                else -> context.getString(com.horsegallop.core.R.string.error_unknown)
            }
            showLogoToast(context, msg, true)
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            showLogoToast(context, context.getString(com.horsegallop.core.R.string.auth_success), false)
            onGoogleClick()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5F5),
                        Color.White
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = stringResource(com.horsegallop.core.R.string.app_name),
                    modifier = Modifier
                        .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xxxl))
                )
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
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
            ) {
                HorseLoadingOverlay(visible = uiState.loading)
                val focusManager = LocalFocusManager.current
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm),
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
                                vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)
                            ),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
                    ) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_xs)))
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                errorBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                errorLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                errorBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                errorLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            TextButton(onClick = onForgotPasswordClick) {
                                Text(
                                    text = stringResource(com.horsegallop.core.R.string.forgot_password),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Button(
                            onClick = vm::login,
                            enabled = !uiState.loading && uiState.isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
                            shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
                        ) {
                            Text(
                                text = if (uiState.loading) stringResource(com.horsegallop.core.R.string.login_button_loading) else stringResource(com.horsegallop.core.R.string.login_button),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = onSignupClick) {
                                Text(text = stringResource(com.horsegallop.core.R.string.prompt_create_account), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Divider)
                    Text(
                        text = stringResource(com.horsegallop.core.R.string.or_label),
                        modifier = Modifier.padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.Divider)
                }
                GoogleSignInButton(loading = uiState.loading, onClick = {
                    if (!uiState.loading) {
                        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                        if (availability != ConnectionResult.SUCCESS) {
                            scope.launch {
                                showLogoToast(context, context.getString(com.horsegallop.core.R.string.auth_error_play_services), true)
                            }
                        } else {
                            vm.trySilentSignIn { intent ->
                                if (intent != null) launcher.launch(intent)
                            }
                        }
                    }
                })
                Text(
                    text = stringResource(com.horsegallop.core.R.string.terms_consent),
                    style = MaterialTheme.typography.bodySmall,
                    color = com.horsegallop.core.theme.LocalTextColors.current.bodyTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_content_horizontal))
                        .padding(top = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    
}

private fun showLogoToast(context: Context, text: String, isError: Boolean) {
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
                painter = painterResource(id = R.drawable.ic_google_logo),
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
                painter = painterResource(id = R.drawable.ic_email_icon),
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
