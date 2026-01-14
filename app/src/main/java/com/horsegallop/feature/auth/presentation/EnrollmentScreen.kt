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
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CoreR.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                    Text(
                        text = stringResource(CoreR.string.signup_prompt),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

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
                        text = stringResource(AppR.string.enrollment_title),
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(CoreR.dimen.padding_screen_horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(CoreR.string.login_verify_email_sent),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(AppR.string.verification_sent_to_email, uiState.email),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (uiState.resendCooldownRemaining > 0) 
                        "${stringResource(AppR.string.btn_resend_verification)} (${uiState.resendCooldownRemaining}s)" 
                    else 
                        stringResource(AppR.string.btn_resend_verification)
                )
            }
        }
        
        if (uiState.verificationError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.verificationError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
