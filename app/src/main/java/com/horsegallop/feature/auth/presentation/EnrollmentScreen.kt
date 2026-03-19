package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.core.components.HorseGallopTextField
import com.horsegallop.ui.theme.LocalSemanticColors
import com.horsegallop.R as CoreR
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
    val semantic = LocalSemanticColors.current

    LaunchedEffect(Unit) {
        viewModel.loadLottieConfig()
    }

    Scaffold(
        containerColor = semantic.screenBase,
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
                    containerColor = semantic.screenBase  // arka planla bütünleşik, ayrık toolbar görünmez
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
                EnrollmentFormContent(
                    uiState = uiState,
                    onFirstNameChange = viewModel::updateFirstName,
                    onLastNameChange = viewModel::updateLastName,
                    onEmailChange = viewModel::updateEmail,
                    onPasswordChange = viewModel::updatePassword,
                    onSignUpClick = viewModel::signUp,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = dimensionResource(CoreR.dimen.padding_screen_horizontal))
                )
            }
        }
    }
}

@Composable
internal fun EnrollmentFormContent(
    uiState: EnrollmentUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val title = uiState.enrollTitle ?: stringResource(CoreR.string.login_title_brand)
    val subtitle = uiState.enrollSubtitle ?: stringResource(CoreR.string.signup_prompt)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(CoreR.dimen.spacing_lg))
    ) {
        AuthHeader(
            title = title,
            subtitle = subtitle
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = semantic.cardElevated,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, semantic.cardStroke)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NameFieldsSection(
                    firstName = uiState.firstName,
                    lastName = uiState.lastName,
                    onFirstNameChange = onFirstNameChange,
                    onLastNameChange = onLastNameChange,
                    firstNameModifier = Modifier.semantics { testTag = "enrollment_first_name" },
                    lastNameModifier = Modifier.semantics { testTag = "enrollment_last_name" }
                )

                HorseGallopTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(AppR.string.label_email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "enrollment_email" }
                )

                PasswordFieldSection(
                    password = uiState.password,
                    onPasswordChange = onPasswordChange,
                    fieldModifier = Modifier.semantics { testTag = "enrollment_password" }
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
                    onClick = onSignUpClick,
                    enabled = uiState.isFormValid && !uiState.loading,
                    isLoading = uiState.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "signup_button" }
                )

                Text(
                    text = stringResource(CoreR.string.terms_consent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensionResource(CoreR.dimen.spacing_xl)))
    }
}

@Preview(showBackground = true)
@Composable
private fun EnrollmentFormContentPreview() {
    com.horsegallop.ui.theme.AppTheme {
        EnrollmentFormContent(
            uiState = EnrollmentUiState(
                firstName = "Elif",
                lastName = "Yılmaz",
                email = "elif@horsegallop.com",
                password = "",
                isFormValid = false
            ),
            onFirstNameChange = {},
            onLastNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onSignUpClick = {}
        )
    }
}

@Composable
private fun AuthHeader(
    title: String,
    subtitle: String
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val semantic = LocalSemanticColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(primary.copy(alpha = 0.95f), secondary.copy(alpha = 0.85f))
                )
            )
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = semantic.onImageOverlay
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = semantic.onImageOverlay.copy(alpha = 0.9f)
            )
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
    val semantic = LocalSemanticColors.current

    // Onboarding'deki at animasyonunu kullan (URL yerine yerel kaynak)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(AppR.raw.horse))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(semantic.screenBase)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Animated horse in circle
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(semantic.calloutInfoContainer),
            contentAlignment = Alignment.Center
        ) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(150.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MarkEmailRead,
                    contentDescription = null,
                    tint = semantic.calloutOnContainer,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title
        Text(
            text = stringResource(CoreR.string.login_verify_email_sent),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email chip
        if (uiState.email.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = semantic.calloutInfoContainer
            ) {
                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = semantic.calloutOnContainer,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Steps card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
            border = BorderStroke(1.dp, semantic.cardStroke),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VerificationStepRow(number = "1", label = stringResource(CoreR.string.verification_step_1))
                VerificationStepRow(number = "2", label = stringResource(CoreR.string.verification_step_2))
                VerificationStepRow(number = "3", label = stringResource(CoreR.string.verification_step_3))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spam hint
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = stringResource(AppR.string.verification_spam_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary CTA
        HorseGallopButton(
            text = stringResource(AppR.string.btn_confirm_verified),
            onClick = onVerifiedCheck,
            isLoading = uiState.verifying,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "confirm_verified_button" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Resend button
        FilledTonalButton(
            onClick = onResendClick,
            enabled = uiState.resendCooldownRemaining == 0 && !uiState.verifying,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "resend_verification_button" }
        ) {
            Text(
                text = if (uiState.resendCooldownRemaining > 0)
                    stringResource(AppR.string.verification_resend_countdown, uiState.resendCooldownRemaining)
                else
                    stringResource(AppR.string.btn_resend_verification)
            )
        }

        if (uiState.verificationError != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.verificationError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun VerificationStepRow(number: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VerificationSentContentPreview() {
    com.horsegallop.ui.theme.AppTheme {
        VerificationSentContent(
            uiState = EnrollmentUiState(
                email = "binici@horsegallop.com",
                verificationSent = true,
                resendCooldownRemaining = 45
            ),
            onResendClick = {},
            onVerifiedCheck = {},
            onDismiss = {}
        )
    }
}
