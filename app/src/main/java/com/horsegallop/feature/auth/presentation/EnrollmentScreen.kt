@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.auth.presentation

import android.app.DatePickerDialog
import android.Manifest
import android.location.LocationManager
import android.location.Geocoder
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.input.KeyboardType
import android.util.Patterns
import com.horsegallop.compose.HorseLoadingOverlay
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.lerp
import java.text.SimpleDateFormat
import java.util.Calendar
import com.airbnb.lottie.compose.*

@Composable
fun EnrollmentScreen(
  onBack: () -> Unit,
  onSignedUp: () -> Unit
) {
  val vm: EnrollmentViewModel = androidx.hilt.navigation.compose.hiltViewModel()
  val ui = vm.ui.collectAsState().value
  val ctx = LocalContext.current

  var showVerifySheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  var showBanner by remember { mutableStateOf(false) }
  
  val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
  var isForeground by remember { mutableStateOf(true) }
  DisposableEffect(lifecycleOwner) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      isForeground = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(text = stringResource(com.horsegallop.R.string.enrollment_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
        navigationIcon = {
          IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary) }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
      )
    }
  , containerColor = MaterialTheme.colorScheme.background) { padding ->
    
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .windowInsetsPadding(WindowInsets.ime)
        .navigationBarsPadding()
        .padding(
          horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal),
          vertical = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_vertical)
        )
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md))
    ) {
      

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm),
        border = androidx.compose.foundation.BorderStroke(
          dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
          MaterialTheme.colorScheme.outlineVariant
        )
      ) {
        Column(
          modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
          verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.list_item_spacing_md))
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))) {
            OutlinedTextField(
              value = ui.firstName,
              onValueChange = vm::updateFirstName,
              label = { Text(stringResource(com.horsegallop.R.string.label_first_name), style = MaterialTheme.typography.bodySmall) },
              placeholder = { Text(stringResource(com.horsegallop.R.string.label_first_name), style = MaterialTheme.typography.bodySmall) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                errorBorderColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
              value = ui.lastName,
              onValueChange = vm::updateLastName,
              label = { Text(stringResource(com.horsegallop.R.string.label_last_name), style = MaterialTheme.typography.bodySmall) },
              placeholder = { Text(stringResource(com.horsegallop.R.string.label_last_name), style = MaterialTheme.typography.bodySmall) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                errorBorderColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
              ),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
          }
          
          
          var passwordVisible by remember { mutableStateOf(false) }
          OutlinedTextField(
            value = ui.email,
            onValueChange = vm::updateEmail,
            label = { Text(stringResource(com.horsegallop.R.string.label_email), style = MaterialTheme.typography.bodySmall) },
            placeholder = { Text(stringResource(com.horsegallop.R.string.label_email), style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
              focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            )
          )
          OutlinedTextField(
            value = ui.password,
            onValueChange = vm::updatePassword,
            label = { Text(stringResource(com.horsegallop.R.string.label_password), style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
              focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                  contentDescription = null
                )
              }
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            )
          )
          
          val hasLen = ui.password.length >= 10
          val hasUpper = ui.password.any { it.isUpperCase() }
          val hasLower = ui.password.any { it.isLowerCase() }
          val hasDigit = ui.password.any { it.isDigit() }
          val hasSpecial = ui.password.any { !it.isLetterOrDigit() }
          val score = listOf(hasLen, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
          val progress = score / 5f
          val indicatorColor = if (score >= 4) MaterialTheme.colorScheme.tertiary else lerp(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.primary,
            progress.coerceIn(0f, 1f)
          )
          LinearProgressIndicator(progress = { progress }, color = indicatorColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
          val strengthText = when (score) { 0,1 -> stringResource(com.horsegallop.R.string.strength_weak); 2,3 -> stringResource(com.horsegallop.R.string.strength_medium); else -> stringResource(com.horsegallop.R.string.strength_strong) }
          Text(text = stringResource(com.horsegallop.R.string.password_strength_prefix, strengthText), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = stringResource(com.horsegallop.R.string.password_guidance),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
            style = MaterialTheme.typography.bodySmall
          )
          
        }
      }

      if (ui.error != null) {

        Text(text = stringResource(ui.error), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
      }

      if (ui.validationErrors.isNotEmpty()) {
        val errorMessages = ui.validationErrors.map { stringResource(it) }

        Text(
          text = errorMessages.joinToString(" • "),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodySmall
        )
      }

      Button(
        onClick = { if (ui.isFormValid) vm.signUp() },
        enabled = !ui.loading && ui.isFormValid,
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (ui.isFormValid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          disabledContainerColor = MaterialTheme.colorScheme.primary,
          disabledContentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        if (ui.loading) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))) {
            CircularProgressIndicator(modifier = Modifier.size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_sm)), color = MaterialTheme.colorScheme.onPrimary)
            Text(stringResource(com.horsegallop.R.string.enrollment_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        } else {
          Text(stringResource(com.horsegallop.R.string.enrollment_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
      }

      

      HorseLoadingOverlay(visible = ui.loading)


  

  
    }
  }

    if (showVerifySheet) {
      ModalBottomSheet(
        onDismissRequest = { showVerifySheet = false },
        sheetState = sheetState,
        dragHandle = {},
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 24.dp),
          verticalArrangement = Arrangement.spacedBy(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
          val ctxLocal = androidx.compose.ui.platform.LocalContext.current

          Icon(
            painter = painterResource(id = com.horsegallop.R.drawable.ic_horse),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.tertiary
          )

          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = stringResource(com.horsegallop.R.string.verify_email_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )
            Text(
              text = if (email != null) stringResource(com.horsegallop.R.string.verification_sent_to_email, email) else stringResource(com.horsegallop.R.string.verification_sent_info),
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
          
          if (ui.verifying) {
            LinearProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
          }

          val pm = ctxLocal.packageManager
          val emailIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_APP_EMAIL)
          val defaultEmailActivity = emailIntent.resolveActivity(pm)
          if (defaultEmailActivity != null) {
            Button(
              onClick = { runCatching { ctxLocal.startActivity(emailIntent) } },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
            ) { androidx.compose.material3.Text(text = stringResource(com.horsegallop.R.string.open_mail_app)) }
          }
        }
        LaunchedEffect(isForeground) {
          if (isForeground) {
            vm.checkEmailVerified(onVerified = {
              onSignedUp()
            })
          }
        }
      val act = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
      LaunchedEffect(Unit) {
        val uri = act?.intent?.data
        if (uri != null && uri.scheme == "horsegallop" && uri.host == "verify-complete") {
          // com.horsegallop.core.debug.AppLog.i("EnrollmentUI", "verify-complete deepLink")
          showVerifySheet = false
          onSignedUp()
          act?.setIntent(android.content.Intent())

        }
      }
    }
  }
  if (ui.showVerificationResult) {
    val url = if (ui.verificationSuccess == true) ui.successLottieUrl else ui.errorLottieUrl
    val composition by rememberLottieComposition(LottieCompositionSpec.Url(url))
    val progress by animateLottieCompositionAsState(composition, iterations = 1)
    androidx.compose.ui.window.Dialog(onDismissRequest = { vm.dismissVerificationResult() }) {
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
          Text(text = if (ui.verificationSuccess == true) stringResource(com.horsegallop.R.string.verification_result_success) else stringResource(com.horsegallop.R.string.verification_result_error), color = MaterialTheme.colorScheme.onSurface)
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { vm.dismissVerificationResult() }) { Text(stringResource(com.horsegallop.R.string.btn_close)) }
            if (ui.verificationSuccess == true) {
              Button(onClick = { vm.dismissVerificationResult(); onSignedUp() }) { Text(stringResource(com.horsegallop.R.string.btn_continue)) }
            }
          }
        }
      }
    }
  }
}


@Preview(showBackground = true, name = "EnrollmentScreen")
@Composable
private fun PreviewEnrollmentScreen() {
  MaterialTheme { EnrollmentScreen(onBack = {}, onSignedUp = {}) }
}
