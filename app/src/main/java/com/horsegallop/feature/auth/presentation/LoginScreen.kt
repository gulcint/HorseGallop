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
import androidx.compose.ui.res.dimensionResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.hilt.navigation.compose.hiltViewModel
 

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onAppleClick: () -> Unit = {},
    onEmailClick: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val webClientId: String = remember {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    }
    val hasWebClientId = remember(webClientId) {
        webClientId.isNotBlank() && !webClientId.equals("YOUR_WEB_CLIENT_ID", ignoreCase = true)
    }
    val gso = remember(webClientId, hasWebClientId) {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (hasWebClientId) {
            builder.requestIdToken(webClientId)
        }
        builder.build()
    }
    val googleClient = remember(gso) { GoogleSignIn.getClient(context, gso) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account.idToken
            if (token.isNullOrEmpty()) {
                Toast.makeText(
                    context,
                    "Google OAuth yapılandırması eksik: Web Client ID gerekli.",
                    Toast.LENGTH_LONG
                ).show()
                return@rememberLauncherForActivityResult
            }
            viewModel.signInWithGoogleIdToken(token)
        } catch (e: ApiException) {
            Toast.makeText(
                context,
                "Google giriş başarısız (${e.statusCode})",
                Toast.LENGTH_SHORT
            ).show()
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
                GoogleSignInButton(
                    onClick = {
                        onGoogleClick()
                        launcher.launch(googleClient.signInIntent)
                    }
                )
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

    when (val s = uiState) {
        AuthUiState.Loading -> {
            // optional: show a small loading indicator, keep UI simple
        }
        is AuthUiState.Error -> {
            // optional: show a snackbar/text - kept minimal to not alter layout
        }
        AuthUiState.Success -> {
            LaunchedEffect(Unit) { onLoginSuccess() }
        }
        else -> {}
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
