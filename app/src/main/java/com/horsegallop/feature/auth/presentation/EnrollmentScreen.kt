package com.horsegallop.feature.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.core.components.HorseGallopTextField
import com.horsegallop.core.R as CoreR
import com.horsegallop.R as AppR
import com.horsegallop.core.theme.AppColors

// Modern Color Constants for HorseGallop App
private const val PrimaryHorseColor = 0xFF8B5A2B  // Updated to match Material 3 theme
private const val SecondaryPastelColor = 0xFFF5E6D3
private const val AccentGoldColor = 0xFFC8A25E
private const val TextDarkColor = 0xFF1A1A1A
private const val TextLightColor = 0xFF666666
private const val BackgroundLightColor = 0xFFF5EFE6

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
                        fontWeight = FontWeight.Medium
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // Modern Header Card
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(CoreR.dimen.radius_xl)),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = dimensionResource(CoreR.dimen.elevation_sm),
                        border = androidx.compose.foundation.BorderStroke(
                            dimensionResource(CoreR.dimen.width_divider_thin),
                            MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(dimensionResource(CoreR.dimen.padding_card_lg)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(CoreR.dimen.spacing_md))
                        ) {
                            // Icon Card
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.ActionLesson.copy(alpha = 0.15f))
                                    .border(
                                        androidx.compose.foundation.BorderStroke(
                                            dimensionResource(CoreR.dimen.width_divider_thin),
                                            AppColors.ActionLesson
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    tint = AppColors.ActionLesson,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = stringResource(CoreR.string.signup_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = stringResource(CoreR.string.signup_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step Indicator
                    if (uiState.step > 0) {
                        StepIndicator(currentStep = uiState.step, totalSteps = 3)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // User Type Selection Card (Modern)
                    if (uiState.step == 0) {
                        UserTypeSelectionCard(
                            selectedType = uiState.userType,
                            onTypeSelected = { viewModel.updateUserType(it) }
                        )
                    } else {
                        // Name Fields
                        NameFieldsSection(
                            firstName = uiState.firstName,
                            lastName = uiState.lastName,
                            onFirstNameChange = viewModel::updateFirstName,
                            onLastNameChange = viewModel::updateLastName
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorseGallopTextField(
                            value = uiState.email,
                            onValueChange = viewModel::updateEmail,
                            label = stringResource(AppR.string.label_email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button
                    if (uiState.step == 0) {
                        HorseGallopButton(
                            text = stringResource(AppR.string.continue_button),
                            onClick = { viewModel.nextStep() },
                            enabled = uiState.userType != null && !uiState.loading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        HorseGallopButton(
                            text = stringResource(AppR.string.enrollment_title),
                            onClick = viewModel::signUp,
                            enabled = uiState.isFormValid && !uiState.loading,
                            isLoading = uiState.loading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (uiState.step > 0) {
                        TextButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                            Text(stringResource(CoreR.string.back))
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(CoreR.dimen.spacing_xl)))
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isCompleted = step < currentStep
            val isActive = step == currentStep
            
            Surface(
                shape = RoundedCornerShape(dimensionResource(CoreR.dimen.radius_full)),
                color = if (isCompleted) AppColors.Success else if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(dimensionResource(CoreR.dimen.radius_full)))
            ) {
                if (isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }

    Text(
        text = when (currentStep) {
            1 -> stringResource(AppR.string.step_user_type)
            2 -> stringResource(AppR.string.step_account_details)
            else -> ""
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun UserTypeSelectionCard(
    selectedType: String?,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(CoreR.dimen.radius_xl)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensionResource(CoreR.dimen.elevation_sm),
        border = androidx.compose.foundation.BorderStroke(
            dimensionResource(CoreR.dimen.width_divider_thin),
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(CoreR.dimen.padding_card_lg)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(CoreR.dimen.spacing_md))
        ) {
            Text(
                text = stringResource(AppR.string.select_user_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            val userTypes = listOf(
                UserTypeItem("rider", Icons.Filled.Motorcycle, stringResource(AppR.string.user_type_rider), stringResource(AppR.string.user_type_rider_desc)),
                UserTypeItem("horse_owner", Icons.Filled.Pets, stringResource(AppR.string.user_type_horse_owner), stringResource(AppR.string.user_type_horse_owner_desc)),
                UserTypeItem("barn_owner", Icons.Filled.Home, stringResource(AppR.string.user_type_barn_owner), stringResource(AppR.string.user_type_barn_owner_desc))
            )

            userTypes.forEach { userType ->
                UserSelectionCard(
                    userType = userType,
                    isSelected = selectedType == userType.id,
                    onClick = { onTypeSelected(userType.id) }
                )
            }
        }
    }
}

@Composable
fun UserSelectionCard(
    userType: UserTypeItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(CoreR.dimen.radius_lg)),
        border = androidx.compose.foundation.BorderStroke(
            dimensionResource(CoreR.dimen.width_divider_thin),
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(CoreR.dimen.padding_card_md)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(CoreR.dimen.spacing_md))
        ) {
            Surface(
                shape = RoundedCornerShape(dimensionResource(CoreR.dimen.radius_md)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(userType.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Column {
                Text(
                    text = userType.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = userType.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Filled.RadioButtonUnchecked, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class UserTypeItem(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val description: String
)

@Composable
fun PasswordStrengthIndicator(password: String) {
    val hasLen = password.length >= 10
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val score = listOf(hasLen, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    val progress = score / 5f
    val indicatorColor = if (score >= 4) MaterialTheme.colorScheme.tertiary else lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.primary,
        progress.coerceIn(0f, 1f)
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            color = indicatorColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp))
        )
        val strengthText = when (score) { 
            0,1 -> stringResource(AppR.string.strength_weak)
            2,3 -> stringResource(AppR.string.strength_medium)
            else -> stringResource(AppR.string.strength_strong) 
        }
        Text(
            text = stringResource(AppR.string.password_strength_prefix, strengthText),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationSheetContent(
    email: String?,
    onOpenMail: () -> Unit,
    isVerifying: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            painter = androidx.compose.ui.res.painterResource(id = AppR.mipmap.ic_launcher),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(AppR.string.verify_email_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = if (email != null) stringResource(AppR.string.verification_sent_to_email, email) else stringResource(AppR.string.verification_sent_info),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        if (isVerifying) {
            LinearProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
        }

        Button(
            onClick = onOpenMail,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(CoreR.dimen.radius_lg))
        ) {
            Text(text = stringResource(AppR.string.open_mail_app))
        }
    }
}
