@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.auth.presentation

import android.app.DatePickerDialog
import android.Manifest
import android.location.LocationManager
import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import android.util.Patterns
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.lerp
import java.text.SimpleDateFormat
import java.util.Calendar
import com.airbnb.lottie.compose.*
import com.horsegallop.R
import com.horsegallop.navigation.Dest
import com.horsegallop.compose.HorseLoadingOverlay

@Composable
fun EnrollmentScreen(
    onBack: () -> Unit,
    onSignedUp: () -> Unit
) {
    val vm: EnrollmentViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val ui by vm.ui.collectAsState()

    var showVerifySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Synchronize Sheet visibility with ViewModel state
    LaunchedEffect(ui.verificationSent) {
        if (ui.verificationSent) {
            showVerifySheet = true
        }
    }

    // App Foreground/Background detection for verification polling
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    var isForeground by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            isForeground = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(isForeground) {
        if (isForeground && showVerifySheet) {
            vm.checkEmailVerified(onVerified = {
                vm.dismissVerificationResult()
                onSignedUp()
            })
        }
    }

    Scaffold(
        topBar = {
            EnrollmentTopBar(onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            EnrollmentContent(
                ui = ui,
                vm = vm
            )
            HorseLoadingOverlay(visible = ui.loading)
        }
    }

    if (showVerifySheet) {
        ModalBottomSheet(
            onDismissRequest = { showVerifySheet = false },
            sheetState = sheetState,
            dragHandle = {},
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("verify_email_sheet")
        ) {
            VerificationSheetContent(
                email = ui.currentUserEmail ?: ui.email,
                isVerifying = ui.verifying,
                onOpenMail = { openEmailApp(context) }
            )
        }
    }

    if (ui.showVerificationResult) {
        VerificationResultDialog(
            ui = ui,
            onClose = { vm.dismissVerificationResult() },
            onContinue = {
                vm.dismissVerificationResult()
                onSignedUp()
            }
        )
    }
}

@Composable
private fun EnrollmentTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.enrollment_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun EnrollmentContent(
    ui: EnrollmentUiState,
    vm: EnrollmentViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NameFieldsSection(
                    firstName = ui.firstName,
                    lastName = ui.lastName,
                    onFirstNameChange = vm::updateFirstName,
                    onLastNameChange = vm::updateLastName
                )

                OutlinedTextField(
                    value = ui.email,
                    onValueChange = vm::updateEmail,
                    label = { Text(stringResource(R.string.label_email), style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text(stringResource(R.string.label_email), style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )

                PasswordFieldSection(
                    password = ui.password,
                    onPasswordChange = vm::updatePassword
                )

                if (ui.validationErrors.isNotEmpty()) {
                    val errorMessages = ui.validationErrors.map { stringResource(it) }
                    Text(
                        text = errorMessages.joinToString(" • "),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (ui.errorMessage != null || ui.error != null) {
            Text(
                text = ui.errorMessage ?: stringResource(ui.error!!),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { if (ui.isFormValid) vm.signUp() },
            enabled = !ui.loading && ui.isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("enroll_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ui.isFormValid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            Text(
                stringResource(R.string.enrollment_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VerificationResultDialog(
    ui: EnrollmentUiState,
    onClose: () -> Unit,
    onContinue: () -> Unit
) {
    val url = if (ui.verificationSuccess == true) ui.successLottieUrl else ui.errorLottieUrl
    val composition by rememberLottieComposition(LottieCompositionSpec.Url(url))
    val progress by animateLottieCompositionAsState(composition, iterations = 1)

    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(220.dp))
                Text(
                    text = if (ui.verificationSuccess == true) 
                        stringResource(R.string.verification_result_success) 
                    else 
                        stringResource(R.string.verification_result_error),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onClose) { Text(stringResource(R.string.btn_close)) }
                    if (ui.verificationSuccess == true) {
                        Button(onClick = onContinue) { Text(stringResource(R.string.btn_continue)) }
                    }
                }
            }
        }
    }
}

private fun openEmailApp(context: android.content.Context) {
    val emailIntent = android.content.Intent(android.content.Intent.ACTION_MAIN)
        .addCategory(android.content.Intent.CATEGORY_APP_EMAIL)
    
    runCatching { context.startActivity(emailIntent) }
        .onFailure {
            val chooser = android.content.Intent.createChooser(
                android.content.Intent(android.content.Intent.ACTION_SENDTO).apply { 
                    data = android.net.Uri.parse("mailto:") 
                },
                "Open Email"
            )
            runCatching { context.startActivity(chooser) }
        }
}


@Preview(showBackground = true, name = "EnrollmentScreen")
@Composable
private fun PreviewEnrollmentScreen() {
  MaterialTheme { EnrollmentScreen(onBack = {}, onSignedUp = {}) }
}
