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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.text.input.KeyboardType
import android.util.Patterns
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
  val countryCodes = listOf("+90", "+1", "+44", "+49", "+33", "+34", "+39", "+61", "+81", "+86", "+971", "+7")
  val citySuggestions = stringArrayResource(com.horsegallop.R.array.city_list).toList()
  LaunchedEffect(Unit) { vm.loadLottieConfig() }
  

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text(text = stringResource(com.horsegallop.R.string.enrollment_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
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
              label = { Text(stringResource(com.horsegallop.R.string.label_first_name)) },
              placeholder = { Text(stringResource(com.horsegallop.R.string.label_first_name)) },
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
              label = { Text(stringResource(com.horsegallop.R.string.label_last_name)) },
              placeholder = { Text(stringResource(com.horsegallop.R.string.label_last_name)) },
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
          
          
          OutlinedTextField(
            value = ui.email,
            onValueChange = vm::updateEmail,
            label = { Text(stringResource(com.horsegallop.R.string.label_email)) },
            placeholder = { Text(stringResource(com.horsegallop.R.string.label_email)) },
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
            label = { Text(stringResource(com.horsegallop.R.string.label_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
              focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(),
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
          val indicatorColor = if (score >= 5) MaterialTheme.colorScheme.primary else lerp(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.primary,
            progress.coerceIn(0f, 1f)
          )
          LinearProgressIndicator(progress = { progress }, color = indicatorColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
          val strengthText = when (score) { 0,1 -> stringResource(com.horsegallop.R.string.strength_weak); 2,3 -> stringResource(com.horsegallop.R.string.strength_medium); else -> stringResource(com.horsegallop.R.string.strength_strong) }
          Text(text = stringResource(com.horsegallop.R.string.password_strength_prefix, strengthText), color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = stringResource(com.horsegallop.R.string.password_guidance),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
            style = MaterialTheme.typography.bodySmall
          )
          
        }
      }

      if (ui.error != null) {
        Text(text = stringResource(ui.error), color = MaterialTheme.colorScheme.error)
      }

      val nameValid = ui.firstName.isNotBlank() && ui.lastName.isNotBlank()
      val emailValid = Patterns.EMAIL_ADDRESS.matcher(ui.email).matches()
      val hasLen = ui.password.length >= 10
      val score = listOf(
        ui.password.any { it.isUpperCase() },
        ui.password.any { it.isLowerCase() },
        ui.password.any { it.isDigit() },
        ui.password.any { !it.isLetterOrDigit() }
      ).count { it }
      val strong = hasLen

      val disabledReasons = buildList {
        if (!nameValid) add(stringResource(com.horsegallop.R.string.error_name_required))
        if (!emailValid) add(stringResource(com.horsegallop.R.string.error_email_invalid))
        if (!hasLen) add(stringResource(com.horsegallop.R.string.error_password_length))
      }
      if (disabledReasons.isNotEmpty()) {
        Text(
          text = disabledReasons.joinToString(" • "),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodySmall
        )
      }

      Button(
        onClick = { if (nameValid && emailValid && strong) vm.signUp() },
        enabled = !ui.loading && nameValid && emailValid && strong,
        modifier = Modifier
          .fillMaxWidth()
          .height(dimensionResource(id = com.horsegallop.core.R.dimen.height_button_xl)),
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
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

      

      if (ui.loading) {
        androidx.compose.ui.window.Dialog(
          onDismissRequest = {},
          properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
          )
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
          ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.horsegallop.R.raw.horse))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(200.dp))
          }
        }
      }

  if (ui.verificationSent) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
      border = androidx.compose.foundation.BorderStroke(
        dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
        MaterialTheme.colorScheme.primary
      )
    ) {
      Column(
        modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
      ) {
        Text(
          text = stringResource(com.horsegallop.R.string.verification_sent_info),
          color = MaterialTheme.colorScheme.onSurface
        )
        if (ui.verificationError != null) {
          Text(text = ui.verificationError ?: "", color = MaterialTheme.colorScheme.error)
        }
          OutlinedTextField(
            value = ui.verificationCode,
            onValueChange = vm::updateVerificationCode,
            label = { Text(text = stringResource(com.horsegallop.R.string.label_verification_code)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            )
          )
        Row(
          horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
        ) {
          OutlinedButton(onClick = { vm.resendVerificationEmail() }, enabled = !ui.verifying) {
            Text(text = stringResource(com.horsegallop.R.string.btn_resend_verification))
          }
          Button(onClick = { vm.checkEmailVerified(onVerified = onSignedUp) }, enabled = !ui.verifying) { Text(text = stringResource(com.horsegallop.R.string.btn_confirm_verified)) }
          Button(onClick = {
            vm.applyVerificationCode { ok ->
              // result overlay will show via ui state
              if (ok) {
                onSignedUp()
              }
            }
          }, enabled = !ui.verifying) {
            Text(text = stringResource(com.horsegallop.R.string.btn_apply_code))
          }
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
  }

  
}


@Preview(showBackground = true, name = "EnrollmentScreen")
@Composable
private fun PreviewEnrollmentScreen() {
  MaterialTheme { EnrollmentScreen(onBack = {}, onSignedUp = {}) }
}
