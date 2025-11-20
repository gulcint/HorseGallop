package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 

@Composable
fun LoginScreen(
    onGoogleClick: () -> Unit = {},
    onAppleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: LoginViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
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
                else -> context.getString(com.horsegallop.core.R.string.error_unknown)
            }
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            delay(250)
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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(visible = uiState.loading, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        AnimatedVisibility(visible = uiState.success, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher),
                        contentDescription = stringResource(com.horsegallop.core.R.string.app_name),
                        modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_sm))
                    )
                    Text(
                        text = stringResource(id = com.horsegallop.core.R.string.auth_success),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
        androidx.compose.material3.SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.TopCenter))
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.horsegallop.core.theme.LocalTextColors.current.titlePrimary
                )
                Text(
                    text = stringResource(com.horsegallop.core.R.string.login_subtitle),
                    fontSize = 14.sp,
                    color = com.horsegallop.core.theme.LocalTextColors.current.bodySecondary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
            ) {
                GoogleSignInButton(onClick = {
                    if (!uiState.loading) {
                        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                        if (availability != ConnectionResult.SUCCESS) {
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = context.getString(com.horsegallop.core.R.string.auth_error_play_services)
                                )
                            }
                        } else {
                            vm.trySilentSignIn { intent ->
                                if (intent != null) launcher.launch(intent)
                            }
                        }
                    }
                })
                AppleSignInButton(onClick = onAppleClick)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = AppColors.Divider)
                    Text(
                        text = stringResource(com.horsegallop.core.R.string.or_label),
                        modifier = Modifier.padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Divider(modifier = Modifier.weight(1f), color = AppColors.Divider)
                }
                EmailSignInButton(onClick = onEmailClick)
                Text(
                    text = stringResource(com.horsegallop.core.R.string.terms_consent),
                    fontSize = 12.sp,
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

@Preview(showBackground = true, name = "LoginScreen")
@Composable
private fun PreviewLoginScreen() {
    MaterialTheme {
        LoginScreen(
            onGoogleClick = {},
            onAppleClick = {},
            onEmailClick = {}
        )
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
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
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = stringResource(com.horsegallop.core.R.string.cd_google_logo),
                modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_md))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
            Text(
                text = stringResource(com.horsegallop.core.R.string.continue_with_google),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@Composable
fun AppleSignInButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        color = Color(0xFF000000),
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
                painter = painterResource(id = R.drawable.ic_apple_logo),
                contentDescription = stringResource(com.horsegallop.core.R.string.cd_apple_logo),
                modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_md))
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)))
            Text(
                text = stringResource(com.horsegallop.core.R.string.continue_with_apple),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 0.2.sp
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
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
        }
    }
}
