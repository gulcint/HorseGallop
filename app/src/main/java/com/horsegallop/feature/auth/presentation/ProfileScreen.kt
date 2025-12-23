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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.unit.dp
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.components.HorseGallopDatePicker
import com.horsegallop.core.components.HorseGallopDropdown
// import com.horsegallop.core.util.DateUtils
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


  Scaffold(
      contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
      topBar = {
        CenterAlignedTopAppBar(
          title = { Text(text = stringResource(id = com.horsegallop.core.R.string.profile)) },
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
  ) { innerPadding ->
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
      

      // Profile Card
      Surface(
        modifier = Modifier.padding(top = dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md)),
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
              text = if (nameDisplay.isNotBlank()) nameDisplay else "User",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
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

            ProfileInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = com.horsegallop.core.R.string.label_phone), value = formatMaskedPhone(displayProfile.countryCode, displayProfile.phone.filter { it.isDigit() }))
              HorizontalDivider()
              ProfileInfoRow(icon = Icons.Filled.CalendarToday, label = stringResource(id = com.horsegallop.core.R.string.label_birth_date), value = displayProfile.birthDate)
              HorizontalDivider()
              ProfileInfoRow(icon = Icons.Filled.Email, label = stringResource(id = com.horsegallop.core.R.string.label_email), value = displayProfile.email)

            HorizontalDivider()
            ProfileInfoRow(icon = Icons.Filled.LocationOn, label = stringResource(id = com.horsegallop.core.R.string.label_city), value = displayProfile.city)
          } else {
            EditableInfoRow(icon = Icons.Filled.Person, label = stringResource(id = com.horsegallop.core.R.string.label_first_name)) {
              OutlinedTextField(
                value = displayProfile.firstName,
                onValueChange = { viewModel.updateDraft(firstName = it) },
                singleLine = true,

                modifier = Modifier.fillMaxWidth().heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),

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

                modifier = Modifier.fillMaxWidth().heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),

                textStyle = MaterialTheme.typography.bodySmall,
                shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                )
              )
            }
            val countryCodes = state.countryCodes
            EditableInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = com.horsegallop.core.R.string.label_phone)) {
              Row(
                  horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm)),
                verticalAlignment = Alignment.CenterVertically
              ) {
                HorseGallopDropdown(
                    value = displayProfile.countryCode,
                    onValueChange = { viewModel.updateDraft(countryCode = it) },
                    options = countryCodes,
                    modifier = Modifier.width(100.dp)
                        .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md))
                )

                OutlinedTextField(

                  value = displayProfile.phone,
                  onValueChange = { if (it.length <= 15 && it.all { c -> c.isDigit() }) viewModel.updateDraft(phone = it) },

                  singleLine = true,
                  modifier = Modifier.weight(1f).heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md)),
                  textStyle = MaterialTheme.typography.bodySmall,
                  shape = RoundedCornerShape(dimensionResource(id = com.horsegallop.core.R.dimen.radius_lg)),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                  ),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),

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

            EditableInfoRow(icon = Icons.Filled.CalendarToday, label = "Date of Birth") {
                HorseGallopDatePicker(
                    value = displayProfile.birthDate,
                    onDateSelected = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md))
                )
            }

            // City
            val cities = stringArrayResource(com.horsegallop.core.R.array.city_list).toList()
            EditableInfoRow(icon = Icons.Filled.LocationOn, label = stringResource(id = com.horsegallop.core.R.string.label_city)) {
                HorseGallopDropdown(
                    value = displayProfile.city,
                    onValueChange = { viewModel.updateDraft(city = it) },
                    options = cities,
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = dimensionResource(id = com.horsegallop.core.R.dimen.height_button_md))
                )
            }


          }
        }
      }

      // Buttons
      if (state.isEditing) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 64.dp),
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
      Spacer(modifier = Modifier.height(140.dp))
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
