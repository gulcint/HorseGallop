package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.core.components.HorseGallopTextField
import com.horsegallop.core.R as CoreR
import com.horsegallop.R as AppR
import android.content.Intent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    onBack: () -> Unit,
    onSignedUp: () -> Unit,
    viewModel: EnrollmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadLottieConfig()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(CoreR.string.signup_prompt),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreR.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.verificationSent) {
                VerificationSentContent(
                    uiState = uiState,
                    onResendClick = viewModel::resendVerificationEmail,
                    onVerifiedCheck = {
                        viewModel.checkEmailVerified { verified ->
                            if (verified) onSignedUp()
                        }
                    },
                    onDismiss = viewModel::dismissVerificationResult
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = dimensionResource(CoreR.dimen.padding_screen_horizontal)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(CoreR.dimen.spacing_lg))
                ) {
                    Spacer(modifier = Modifier.height(dimensionResource(CoreR.dimen.spacing_sm)))

                    NameFieldsSection(
                        firstName = uiState.firstName,
                        lastName = uiState.lastName,
                        onFirstNameChange = viewModel::updateFirstName,
                        onLastNameChange = viewModel::updateLastName
                    )

                    HorseGallopTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateEmail,
                        label = stringResource(AppR.string.label_email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    PasswordFieldSection(
                        password = uiState.password,
                        onPasswordChange = viewModel::updatePassword
                    )

                    if (uiState.error != null) {
                        Text(
                            text = stringResource(uiState.error!!),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    if (uiState.errorMessage != null) {
                         Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    HorseGallopButton(
                        text = stringResource(CoreR.string.signup_prompt),
                        onClick = viewModel::signUp,
                        enabled = uiState.isFormValid && !uiState.loading,
                        isLoading = uiState.loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(dimensionResource(CoreR.dimen.spacing_xl)))
                }
            }
        }
    }
}

@Composable
fun VerificationSentContent(
    uiState: EnrollmentUiState,
    onResendClick: () -> Unit,
    onVerifiedCheck: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(CoreR.dimen.padding_screen_horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = AppR.drawable.ic_email_icon),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(AppR.string.verify_email_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(AppR.string.verification_sent_to_email, uiState.email),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorseGallopButton(
            text = stringResource(AppR.string.open_mail_app),
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_EMAIL)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                     val gmailIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                     if (gmailIntent != null) {
                         context.startActivity(gmailIntent)
                     } else {
                         android.widget.Toast.makeText(context, "Email app not found", android.widget.Toast.LENGTH_SHORT).show()
                     }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        HorseGallopButton(
            text = stringResource(AppR.string.btn_confirm_verified),
            onClick = onVerifiedCheck,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onResendClick,
            enabled = uiState.resendCooldownRemaining == 0 && !uiState.verifying
        ) {
            if (uiState.verifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                     text = if (uiState.resendCooldownRemaining > 0) 
                         "${stringResource(AppR.string.btn_resend_verification)} (${uiState.resendCooldownRemaining})"
                     else 
                         stringResource(AppR.string.btn_resend_verification),
                     color = if (uiState.resendCooldownRemaining > 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (uiState.verificationError != null) {
             Spacer(modifier = Modifier.height(8.dp))
             Text(
                 text = uiState.verificationError,
                 color = MaterialTheme.colorScheme.error,
                 style = MaterialTheme.typography.bodySmall,
                 textAlign = androidx.compose.ui.text.style.TextAlign.Center
             )
        }
    }
}
