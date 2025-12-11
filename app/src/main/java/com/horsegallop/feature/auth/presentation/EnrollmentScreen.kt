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
import androidx.compose.material.icons.filled.CheckCircle
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
import com.horsegallop.core.debug.AppLog
import kotlinx.coroutines.launch

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
  val verifiedMsg = stringResource(com.horsegallop.R.string.verification_result_success)
  val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
  var isForeground by remember { mutableStateOf(true) }
  DisposableEffect(lifecycleOwner) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      isForeground = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
  
  
  LaunchedEffect(ui.verificationSent) {
    if (ui.verificationSent) {
      com.horsegallop.core.debug.AppLog.i("EnrollmentUI", "verificationSent true")
      showVerifySheet = true
    }
  }
  LaunchedEffect(ui.loading) { AppLog.d("EnrollmentUI", "loading=${ui.loading}") }
  LaunchedEffect(ui.error) {
    val res = ui.error
    if (res != null) AppLog.e("EnrollmentUI", "error=${ctx.getString(res)}")
  }
  LaunchedEffect(ui.verificationError) {
    val msg = ui.verificationError
    if (msg != null) com.horsegallop.core.debug.AppLog.e("EnrollmentUI", "verificationError=$msg")
  }
  
  LaunchedEffect(Unit) {
    val act = ctx as? android.app.Activity
    val uri = act?.intent?.data
    if (uri != null && uri.scheme == "horsegallop" && uri.host == "auto-enroll") {
      com.horsegallop.core.debug.AppLog.i("EnrollmentUI", "auto-enroll ${uri}")
      val first = uri.getQueryParameter("first").orEmpty()
      val last = uri.getQueryParameter("last").orEmpty()
      val emailPrefill = uri.getQueryParameter("email").orEmpty()
      val passPrefill = uri.getQueryParameter("password").orEmpty()
      val dob = uri.getQueryParameter("dob").orEmpty()
      val auto = uri.getQueryParameter("auto") == "1" || uri.getQueryParameter("auto") == "true"
      if (first.isNotBlank()) vm.updateFirstName(first)
      if (last.isNotBlank()) vm.updateLastName(last)
      if (emailPrefill.isNotBlank()) vm.updateEmail(emailPrefill)
      if (passPrefill.isNotBlank()) vm.updatePassword(passPrefill)
      if (dob.isNotBlank()) vm.setBirthDate(dob)
      if (auto) vm.signUp()
      act?.setIntent(android.content.Intent())
    }
  }

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
        Text(text = stringResource(ui.error), color = com.horsegallop.core.theme.LocalTextColors.current.error)
      }

      val nameValid = ui.firstName.trim().isNotEmpty() && ui.lastName.trim().isNotEmpty()
      val emailValid = Patterns.EMAIL_ADDRESS.matcher(ui.email.trim()).matches()
      val hasLen = ui.password.length >= 10
      
      val strong = hasLen && ui.password.any { it.isUpperCase() } && ui.password.any { it.isLowerCase() } && ui.password.any { it.isDigit() }
      val needsUpper = !ui.password.any { it.isUpperCase() }
      val needsLower = !ui.password.any { it.isLowerCase() }
      val needsDigit = !ui.password.any { it.isDigit() }
      val needsSpecial = !ui.password.any { !it.isLetterOrDigit() }

      val disabledReasons = buildList {
        if (!nameValid) add(stringResource(com.horsegallop.R.string.error_name_required))
        if (!emailValid) add(stringResource(com.horsegallop.R.string.error_email_invalid))
        if (!hasLen) add(stringResource(com.horsegallop.R.string.error_password_length))
        if (!strong) {
          val parts = buildList {
            if (needsUpper) add(stringResource(com.horsegallop.R.string.password_needs_upper))
            if (needsLower) add(stringResource(com.horsegallop.R.string.password_needs_lower))
            if (needsDigit) add(stringResource(com.horsegallop.R.string.password_needs_digit))
            if (needsSpecial) add(stringResource(com.horsegallop.R.string.password_needs_special))
          }
          if (parts.isNotEmpty()) add(parts.joinToString(", "))
        }
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
          disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
      dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() },
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
      ) {
        val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
        val horseComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.horsegallop.R.raw.horse))
        val horseProgress by animateLottieCompositionAsState(horseComposition, iterations = LottieConstants.IterateForever)
        androidx.compose.animation.AnimatedVisibility(visible = true, enter = fadeIn() + slideInHorizontally(), exit = fadeOut()) {
          androidx.compose.material3.Surface(
            tonalElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
            color = MaterialTheme.colorScheme.secondaryContainer
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_lg)),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))
            ) {
              Box(
                modifier = Modifier
                  .size(dimensionResource(id = com.horsegallop.core.R.dimen.icon_xxl))
                  .clip(androidx.compose.foundation.shape.CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                LottieAnimation(composition = horseComposition, progress = { horseProgress }, modifier = Modifier.fillMaxSize(fraction = 0.7f))
              }
              Text(text = stringResource(com.horsegallop.R.string.verify_email_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
              Text(text = if (email != null) stringResource(com.horsegallop.R.string.verification_sent_to_email, email) else stringResource(com.horsegallop.R.string.verification_sent_info), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
            }
          }
        }
        androidx.compose.animation.AnimatedVisibility(
          visible = showBanner,
          enter = androidx.compose.animation.fadeIn(),
          exit = androidx.compose.animation.fadeOut()
        ) {
          androidx.compose.material3.Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
              horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
            ) {
              androidx.compose.material3.Icon(imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
              Text(text = verifiedMsg, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
          }
        }
        if (ui.verifying) {
          androidx.compose.material3.LinearProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        val ctxLocal = androidx.compose.ui.platform.LocalContext.current
        Button(onClick = {
          AppLog.i("EnrollmentUI", "open_inbox_click")
          val pm = ctxLocal.packageManager
          val candidates = listOf(
            "com.google.android.gm",
            "com.microsoft.office.outlook",
            "com.samsung.android.email.provider",
            "com.yahoo.mobile.client.android.mail",
            "com.readdle.spark"
          )
          val launchIntent = candidates.firstNotNullOfOrNull { pkg -> pm.getLaunchIntentForPackage(pkg) }
          if (launchIntent != null) {
            try {
              ctxLocal.startActivity(launchIntent)
            } catch (e: Exception) {
              com.horsegallop.core.debug.AppLog.e("EnrollmentUI", "Inbox launch error: ${e.localizedMessage}")
            }
          } else {
            try {
              val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_APP_EMAIL)
              val chooser = android.content.Intent.createChooser(intent, "E‑posta uygulaması seç")
              ctxLocal.startActivity(chooser)
            } catch (e: android.content.ActivityNotFoundException) {
              com.horsegallop.core.debug.AppLog.w("EnrollmentUI", "No email app: ${e.localizedMessage}")
              android.widget.Toast.makeText(ctxLocal, "E‑posta uygulaması bulunamadı", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
              com.horsegallop.core.debug.AppLog.e("EnrollmentUI", "Email open error: ${e.localizedMessage}")
            }
          }
        }, modifier = Modifier.fillMaxWidth()) { Text(text = "E‑posta uygulamasını aç") }

        
      }
      val act = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
      LaunchedEffect(Unit) {
        val uri = act?.intent?.data
        if (uri != null && uri.scheme == "horsegallop" && uri.host == "verify-complete") {
          com.horsegallop.core.debug.AppLog.i("EnrollmentUI", "verify-complete deepLink")
          showBanner = true
          scope.launch {
            kotlinx.coroutines.delay(1200)
            showVerifySheet = false
            onSignedUp()
            act?.setIntent(android.content.Intent())
          }
        }
      }
      
    }
  }
}
