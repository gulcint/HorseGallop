@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.foundation.border
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Build
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.horsegallop.R
import com.google.android.gms.common.api.ApiException
import com.horsegallop.core.theme.AppColors
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.util.DateUtils
import java.util.Calendar

@Composable
fun ProfileScreen(
  onBack: () -> Unit,
  onLogout: () -> Unit,
  viewModel: ProfileViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsState()
  val ctx = LocalContext.current




  // Image Picker
  val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    if (uri != null) {
        viewModel.updateProfileImage(uri)
    }
  }
  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
    if (granted) {
      pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
  }

  // Determine which profile to show
  val displayProfile = if (state.isEditing) state.draftProfile else state.userProfile
  val nameDisplay = listOf(displayProfile.firstName, displayProfile.lastName).filter { it.isNotBlank() }.joinToString(" ")
  val fallbackProfile = stringResource(id = com.horsegallop.core.R.string.profile)

  // Error Toasts
  if (state.error != null) {
      LaunchedEffect(state.error) {
          android.widget.Toast.makeText(ctx, state.error, android.widget.Toast.LENGTH_LONG).show()
      }
  }


  Scaffold(contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.padding_screen_horizontal))
        ,
      verticalArrangement = Arrangement.spacedBy(
        dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md)
      )
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
      ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        Text(
          text = if (nameDisplay.isNotBlank()) nameDisplay else fallbackProfile,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Profile Card
      Surface(
        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_xl)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensionResource(id = com.horsegallop.core.R.dimen.elevation_sm),
        border = androidx.compose.foundation.BorderStroke(
          dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
          MaterialTheme.colorScheme.outlineVariant
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_md)),
          horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)
          ),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f))
                .clickable {
                  if (Build.VERSION.SDK_INT >= 33) {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              if (displayProfile.photoUrl != null) {
                  coil.compose.AsyncImage(
                    model = displayProfile.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                  )
              } else {
                  Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.secondary
                  )
              }
            }
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .zIndex(1f)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(
                  androidx.compose.foundation.BorderStroke(
                    dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                  ),
                  shape = CircleShape
                )
                .clickable {
                   if (Build.VERSION.SDK_INT >= 33) {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                  } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Column(
            verticalArrangement = Arrangement.spacedBy(
              dimensionResource(id = com.horsegallop.core.R.dimen.text_spacing_sm)
            )
          ) {
            Text(
              text = if (nameDisplay.isNotBlank()) nameDisplay else stringResource(com.horsegallop.core.R.string.profile_description),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = displayProfile.email,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Info Fields
      Surface(
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
          verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = com.horsegallop.core.R.dimen.list_item_spacing_md)
          )
        ) {
          if (!state.isEditing) {
            ProfileInfoRow(icon = Icons.Filled.Person, label = stringResource(id = com.horsegallop.core.R.string.label_full_name), value = nameDisplay)
            HorizontalDivider()
<<<<<<< Updated upstream
            ProfileInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = com.horsegallop.core.R.string.label_phone), value = phone)
=======
            ProfileInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = com.horsegallop.core.R.string.label_phone), value = formatMaskedPhone(displayProfile.countryCode, displayProfile.phone.filter { it.isDigit() }))
              HorizontalDivider()
              ProfileInfoRow(icon = Icons.Filled.CalendarToday, label = stringResource(id = com.horsegallop.core.R.string.label_birth_date), value = displayProfile.birthDate)
              HorizontalDivider()
              ProfileInfoRow(icon = Icons.Filled.Email, label = stringResource(id = com.horsegallop.core.R.string.label_email), value = displayProfile.email)
>>>>>>> Stashed changes
            HorizontalDivider()
            ProfileInfoRow(icon = Icons.Filled.LocationOn, label = stringResource(id = com.horsegallop.core.R.string.label_city), value = displayProfile.city)
          } else {
            EditableInfoRow(icon = Icons.Filled.Person, label = stringResource(id = com.horsegallop.core.R.string.label_first_name)) {
              OutlinedTextField(
                value = displayProfile.firstName,
                onValueChange = { viewModel.updateDraft(firstName = it) },
                singleLine = true,
<<<<<<< Updated upstream
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg)),
=======
                modifier = Modifier.fillMaxWidth().heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
>>>>>>> Stashed changes
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                )
              )
            }
            EditableInfoRow(icon = Icons.Filled.Person, label = stringResource(id = com.horsegallop.core.R.string.label_last_name)) {
              OutlinedTextField(
                value = displayProfile.lastName,
                onValueChange = { viewModel.updateDraft(lastName = it) },
                singleLine = true,
<<<<<<< Updated upstream
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg)),
=======
                modifier = Modifier.fillMaxWidth().heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
>>>>>>> Stashed changes
                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                )
              )
            }
            val countryCodes = state.countryCodes
            var ccExpanded by remember { mutableStateOf(false) }
            EditableInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = com.horsegallop.core.R.string.label_phone)) {
              Row(
                  horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
                verticalAlignment = Alignment.CenterVertically
              ) {
                ExposedDropdownMenuBox(expanded = ccExpanded, onExpandedChange = { ccExpanded = it }) {
                  OutlinedTextField(
                    value = displayProfile.countryCode,
                    onValueChange = { },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ccExpanded) },
<<<<<<< Updated upstream
                    modifier = Modifier
                      .widthIn(min = 96.dp, max = 120.dp)
                      .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg))
                      .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
=======
                    modifier = Modifier.menuAnchor().width(100.dp).heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
>>>>>>> Stashed changes
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MaterialTheme.colorScheme.primary,
                      unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                    )
                  )
                  ExposedDropdownMenu(expanded = ccExpanded, onDismissRequest = { ccExpanded = false }) {
                    countryCodes.forEach { code ->
                      DropdownMenuItem(
                        text = { Text(code) },
                        onClick = {
                          viewModel.updateDraft(countryCode = code)
                          ccExpanded = false
                        }
                      )
                    }
                  }
                }
                OutlinedTextField(
<<<<<<< Updated upstream
                  value = editPhone,
                  onValueChange = {
                    val digits = it.filter { ch -> ch.isDigit() }.take(15)
                    editPhone = digits
                    val minLen = if (editCountryCode == "+33") 9 else 10
                    phoneError = if (digits.length in minLen..15) null else ctx.getString(com.horsegallop.R.string.error_phone_invalid)
                  },
=======
                  value = displayProfile.phone,
                  onValueChange = { if (it.length <= 15 && it.all { c -> c.isDigit() }) viewModel.updateDraft(phone = it) },
>>>>>>> Stashed changes
                  singleLine = true,
                  modifier = Modifier.weight(1f).heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
                  textStyle = MaterialTheme.typography.bodySmall,
                  shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                  ),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
<<<<<<< Updated upstream
                  isError = phoneError != null,
                  modifier = Modifier
                    .weight(1f)
                    .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg)),
                  visualTransformation = MaskedPhoneTransformation(editCountryCode),
                  textStyle = MaterialTheme.typography.bodySmall,
                  shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                  )
                )
              }
              if (phoneError != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.text_spacing_sm)))
                Text(text = phoneError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
              }
            }
            var cityExpanded by remember { mutableStateOf(false) }
            var cityQuery by remember { mutableStateOf(editCity) }
            val filteredCities: List<String> = remember(cityQuery, citySuggestions) {
              if (cityQuery.isBlank()) citySuggestions else citySuggestions.filter { it.contains(cityQuery, ignoreCase = true) }
            }
            EditableInfoRow(icon = Icons.Filled.LocationOn, label = stringResource(id = com.horsegallop.R.string.label_city)) {
              ExposedDropdownMenuBox(expanded = cityExpanded, onExpandedChange = { cityExpanded = it }) {
                OutlinedTextField(
                  value = cityQuery,
                  onValueChange = { cityQuery = it },
                  singleLine = true,
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded, modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.SecondaryEditable)) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg))
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryEditable),
                  textStyle = MaterialTheme.typography.bodySmall,
                  shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
                    focusedTrailingIconColor = MaterialTheme.colorScheme.secondary,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.secondary
                  )
                )
                ExposedDropdownMenu(
                  expanded = cityExpanded,
                  onDismissRequest = { cityExpanded = false },
                  modifier = Modifier.background(MaterialTheme.colorScheme.surface).border(
                    androidx.compose.foundation.BorderStroke(
                      dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
                      MaterialTheme.colorScheme.outlineVariant
                    ),
                    RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_sm))
                  )
                ) {
                  filteredCities.forEach { cityItem ->
                    DropdownMenuItem(
                      text = { Text(cityItem, color = MaterialTheme.colorScheme.onSurface) },
                      onClick = {
                        editCity = cityItem
                        cityQuery = cityItem
                        cityExpanded = false
                      },
                      contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md),
                        vertical = dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)
                      )
=======
                  visualTransformation = PhoneVisualTransformation(displayProfile.countryCode)
                )
              }
            }
            // Date Picker (using simple TextField for now to match original logic but wired to VM)
            // Original logic used a DatePickerDialog, I should ideally restore it.
            // But for brevity and "simple explanation" I will use the text field with placeholder or simple click.
            // Let's restore the DatePickerDialog behavior
            val context = LocalContext.current
            val calendar = Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    viewModel.updateDraft(birthDate = formatted)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            EditableInfoRow(icon = Icons.Filled.CalendarToday, label = stringResource(id = com.horsegallop.core.R.string.label_birth_date)) {
                Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                    OutlinedTextField(
                        value = displayProfile.birthDate,
                        onValueChange = { },
                        singleLine = true,
                        readOnly = true, // Make it read-only so user has to pick date
                        modifier = Modifier.fillMaxWidth().heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                        ),
                        enabled = false // Disable direct input
>>>>>>> Stashed changes
                    )
                    // Overlay a transparent box to capture clicks if enabled=false blocks clicks
                    Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
                }
            }

            // City
            val cities = stringArrayResource(com.horsegallop.core.R.array.city_list).toList()
            var cityExpanded by remember { mutableStateOf(false) }
            EditableInfoRow(icon = Icons.Filled.LocationOn, label = stringResource(id = com.horsegallop.core.R.string.label_city)) {
                 ExposedDropdownMenuBox(expanded = cityExpanded, onExpandedChange = { cityExpanded = it }) {
                  OutlinedTextField(
                    value = displayProfile.city,
                    onValueChange = { },
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MaterialTheme.colorScheme.primary,
                      unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                    )
                  )
                  ExposedDropdownMenu(expanded = cityExpanded, onDismissRequest = { cityExpanded = false }) {
                    cities.forEach { c ->
                      DropdownMenuItem(
                        text = { Text(c) },
                        onClick = {
                          viewModel.updateDraft(city = c)
                          cityExpanded = false
                        }
                      )
                    }
                  }
                }
            }
<<<<<<< Updated upstream
            var bdExpanded by remember { mutableStateOf(false) }
            EditableInfoRow(icon = Icons.Filled.CalendarToday, label = stringResource(id = com.horsegallop.core.R.string.label_birth_date)) {
              ExposedDropdownMenuBox(expanded = bdExpanded, onExpandedChange = { bdExpanded = it }) {
                OutlinedTextField(
                  value = editBirthDate,
                  onValueChange = { },
                  singleLine = true,
                  readOnly = true,
                  trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                      expanded = bdExpanded,
                      modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.SecondaryEditable)
                    )
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_lg))
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
                  textStyle = MaterialTheme.typography.bodySmall,
                  shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                  )
                )
                ExposedDropdownMenu(
                  expanded = bdExpanded,
                  onDismissRequest = { bdExpanded = false },
                  modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                      androidx.compose.foundation.BorderStroke(
                        dimensionResource(id = com.horsegallop.core.R.dimen.width_divider_thin),
                        MaterialTheme.colorScheme.outlineVariant
                      ),
                      RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_sm))
                    )
                    .heightIn(max = 360.dp)
                ) {
                  val safeInitialMillis = remember(editBirthDateMillis) { editBirthDateMillis ?: System.currentTimeMillis() }
                  val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = safeInitialMillis)
                  Column(
                    modifier = Modifier.padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_sm)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.text_spacing_md))
                  ) {
                    androidx.compose.material3.DatePicker(
                      state = datePickerState,
                      colors = androidx.compose.material3.DatePickerDefaults.colors(
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                        dayContentColor = MaterialTheme.colorScheme.onSurface,
                        todayDateBorderColor = MaterialTheme.colorScheme.primary
                      )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md))) {
                      OutlinedButton(onClick = { bdExpanded = false }) { Text(text = stringResource(id = com.horsegallop.core.R.string.cancel)) }
                      Button(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                          val cal = java.util.Calendar.getInstance()
                          cal.timeInMillis = millis
                          val y = cal.get(java.util.Calendar.YEAR)
                          val m = cal.get(java.util.Calendar.MONTH) + 1
                          val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                          editBirthDate = String.format("%04d-%02d-%02d", y, m, d)
                          editBirthDateMillis = millis
                        }
                        bdExpanded = false
                      }) { Text(text = stringResource(id = com.horsegallop.core.R.string.ok)) }
                    }
                  }
                }
              }
            }
            }
          
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
          dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)
        )
      ) {
        if (!isEditing) {
          OutlinedButton(
            onClick = {
              editFirstName = firstName
              editLastName = lastName
              editPhone = phone
              editBirthDate = birthDate
              editCity = city
              isEditing = true
            },
            modifier = Modifier.weight(1f)
          ) { Text(text = stringResource(id = com.horsegallop.core.R.string.button_edit)) }
          Button(
            onClick = {
              auth.signOut()
              val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ctx.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
              GoogleSignIn.getClient(ctx, gso).signOut()
              onLogout()
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
          ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            Spacer(modifier = Modifier.width(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)))
            Text(text = stringResource(id = com.horsegallop.core.R.string.logout))
=======
>>>>>>> Stashed changes
          }
        }
      }

      // Buttons
      if (state.isEditing) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.toggleEdit() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
            ) {
                Text(text = stringResource(id = com.horsegallop.core.R.string.button_cancel))
            }
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(text = stringResource(id = com.horsegallop.core.R.string.button_save))
                }
            }
        }
      } else {
        Button(
            onClick = { viewModel.toggleEdit() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg))
        ) {
            Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(id = com.horsegallop.core.R.string.button_edit))
        }


      }
    }
  }




}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (value.isNotBlank()) value else "-", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun EditableInfoRow(icon: ImageVector, label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

class PhoneVisualTransformation(val countryCode: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(text, OffsetMapping.Identity) // Simplified for now
    }
}

fun formatMaskedPhone(countryCode: String, phone: String): String {
    return if (phone.isNotBlank()) "$countryCode $phone" else "-"
}
