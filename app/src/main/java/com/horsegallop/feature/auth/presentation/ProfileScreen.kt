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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.os.Build
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.horsegallop.R

@Composable
fun ProfileScreen(
  onBack: () -> Unit,
  onLogout: () -> Unit
) {
  val auth = FirebaseAuth.getInstance()
  val user = auth.currentUser
  val db = Firebase.firestore
  val ctx = LocalContext.current
  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var birthDate by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("") }
  var email by remember { mutableStateOf(user?.email ?: "") }
  var isEditing by remember { mutableStateOf(false) }
  var editFirstName by remember { mutableStateOf("") }
  var editLastName by remember { mutableStateOf("") }
  var editPhone by remember { mutableStateOf("") }
  var editBirthDate by remember { mutableStateOf("") }
  var editCity by remember { mutableStateOf("") }
  var editEmail by remember { mutableStateOf("") }
  var showDatePicker by remember { mutableStateOf(false) }
  val isGoogle = remember(user) { user?.providerData?.any { it.providerId == "google.com" } == true }
  var profileImageUri by remember { mutableStateOf<Uri?>(null) }
  var profileImageUrl by remember { mutableStateOf<String?>(null) }
  
  val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
    profileImageUri = uri
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid != null && uri != null) {
      val ref = Firebase.storage.reference.child("profiles/$uid.jpg")
      ref.putFile(uri).addOnSuccessListener {
        ref.downloadUrl.addOnSuccessListener { url ->
          val urlStr = url.toString()
          profileImageUrl = urlStr
          Firebase.firestore.collection("users").document(uid).update(mapOf("photoUrl" to urlStr))
        }
      }
    }
  }
  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
    if (granted) {
      pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
  }

  LaunchedEffect(user?.uid) {
    val uid = user?.uid
    if (uid != null) {
      db.collection("users").document(uid).get()
        .addOnSuccessListener { doc ->
          firstName = doc.getString("firstName") ?: ""
          lastName = doc.getString("lastName") ?: ""
          phone = doc.getString("phone") ?: ""
          birthDate = doc.getString("birthDate") ?: ""
          city = doc.getString("city") ?: ""
          email = doc.getString("email") ?: (user?.email ?: "")
          profileImageUrl = doc.getString("photoUrl")
        }
    }
  }

  // Derlenen adı başlıkta kullan
  val nameDisplay = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
  val fallbackProfile = stringResource(id = com.horsegallop.core.R.string.profile)
  val titleName = when {
    nameDisplay.isNotBlank() -> nameDisplay
    !user?.displayName.isNullOrBlank() -> user?.displayName ?: ""
    email.isNotBlank() -> email
    else -> fallbackProfile
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
      // Kompakt başlık satırı (TopAppBar yerine daha az yükseklik)
      Row(
        modifier = Modifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = com.horsegallop.core.R.dimen.spacing_sm))
      ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF8B4513)) }
        Text(
          text = fallbackProfile,
          style = MaterialTheme.typography.bodyLarge,
          fontSize = 24.sp,
          color = androidx.compose.ui.graphics.Color(0xFF8B4513)
        )
      }
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
            .padding(dimensionResource(id = com.horsegallop.core.R.dimen.padding_card_xl)),
          horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)
          ),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier.size(92.dp),
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
              when {
                profileImageUri != null -> {
                  coil.compose.AsyncImage(
                    model = profileImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                  )
                }
                !profileImageUrl.isNullOrBlank() -> {
                  coil.compose.AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                  )
                }
                else -> {
                  Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                  )
                }
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
          if (profileImageUri != null) {
            TextButton(onClick = { profileImageUri = null }) { Text(text = stringResource(id = com.horsegallop.core.R.string.delete)) }
          }
          Column(
            verticalArrangement = Arrangement.spacedBy(
              dimensionResource(id = com.horsegallop.core.R.dimen.text_spacing_sm)
            )
          ) {
            val nameDisplay = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            Text(
              text = nameDisplay,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = email,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

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
          if (!isEditing) {
            ProfileInfoRow(icon = Icons.Filled.Person, label = stringResource(id = com.horsegallop.core.R.string.label_full_name), value = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" "))
            HorizontalDivider()
            ProfileInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = com.horsegallop.core.R.string.label_phone), value = phone)
            HorizontalDivider()
            ProfileInfoRow(icon = Icons.Filled.CalendarToday, label = stringResource(id = com.horsegallop.core.R.string.label_birth_date), value = birthDate)
            HorizontalDivider()
            ProfileInfoRow(icon = Icons.Filled.Email, label = stringResource(id = com.horsegallop.core.R.string.label_email), value = email)
            HorizontalDivider()
            ProfileInfoRow(icon = Icons.Filled.LocationOn, label = stringResource(id = com.horsegallop.R.string.label_city), value = city)
          } else {
            OutlinedTextField(
              value = editFirstName,
              onValueChange = { editFirstName = it },
              label = { Text(stringResource(id = com.horsegallop.core.R.string.label_first_name)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
              )
            )
            OutlinedTextField(
              value = editLastName,
              onValueChange = { editLastName = it },
              label = { Text(stringResource(id = com.horsegallop.core.R.string.label_last_name)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
              )
            )
            OutlinedTextField(
              value = editPhone,
              onValueChange = { editPhone = it },
              label = { Text(stringResource(id = com.horsegallop.core.R.string.label_phone)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
              )
            )
            var cityMenuExpanded by remember { mutableStateOf(false) }
            val citySuggestions = stringArrayResource(com.horsegallop.R.array.city_list).toList()
            val filteredCities = remember(editCity, citySuggestions) {
              if (editCity.isBlank()) citySuggestions else citySuggestions.filter { it.contains(editCity, ignoreCase = true) }
            }
            Box {
              OutlinedTextField(
                value = editCity,
                onValueChange = { editCity = it; cityMenuExpanded = it.isNotBlank() },
                label = { Text(stringResource(id = com.horsegallop.R.string.label_city)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { IconButton(onClick = { cityMenuExpanded = !cityMenuExpanded }) { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary) } },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                )
              )
              DropdownMenu(expanded = cityMenuExpanded, onDismissRequest = { cityMenuExpanded = false }) {
                filteredCities.forEach { cityItem ->
                  DropdownMenuItem(
                    text = { Text(cityItem) },
                    onClick = { editCity = cityItem; cityMenuExpanded = false }
                  )
                }
              }
            }
            OutlinedTextField(
              value = editBirthDate,
              onValueChange = { editBirthDate = it },
              label = { Text(stringResource(id = com.horsegallop.core.R.string.label_birth_date)) },
              singleLine = true,
              readOnly = true,
              modifier = Modifier.fillMaxWidth(),
              trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                  Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
              )
            )
            }
            OutlinedTextField(
              value = editEmail,
              onValueChange = { editEmail = it },
              label = { Text(stringResource(id = com.horsegallop.core.R.string.label_email)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
              enabled = !isGoogle,
              readOnly = isGoogle,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
              )
            )
          
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
              editEmail = email
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
          }
        } else {
          OutlinedButton(
            onClick = {
              isEditing = false
            },
            modifier = Modifier.weight(1f)
          ) { Text(text = stringResource(id = com.horsegallop.core.R.string.button_cancel)) }
          Button(
            onClick = {
              val uid = user?.uid
              if (uid != null) {
                val updates = hashMapOf(
                  "firstName" to editFirstName,
                  "lastName" to editLastName,
                  "phone" to editPhone,
                  "birthDate" to editBirthDate,
                  "city" to editCity
                )
                if (!isGoogle) { updates["email"] = editEmail }
                db.collection("users").document(uid).update(updates as Map<String, Any>).addOnSuccessListener {
                  firstName = editFirstName
                  lastName = editLastName
                  phone = editPhone
                  birthDate = editBirthDate
                  city = editCity
                  if (!isGoogle) { email = editEmail }
                  isEditing = false
                }
              }
            },
            modifier = Modifier.weight(1f)
          ) { Text(text = stringResource(id = com.horsegallop.core.R.string.button_save)) }
        }
      }
      Spacer(modifier = Modifier.height(dimensionResource(id = com.horsegallop.core.R.dimen.section_spacing_md)))
      if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState()
        androidx.compose.material3.DatePickerDialog(
          onDismissRequest = { showDatePicker = false },
          confirmButton = {
            TextButton(onClick = {
              val millis = datePickerState.selectedDateMillis
              if (millis != null) {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = millis
                val y = cal.get(java.util.Calendar.YEAR)
                val m = cal.get(java.util.Calendar.MONTH) + 1
                val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
                editBirthDate = String.format("%04d-%02d-%02d", y, m, d)
              }
              showDatePicker = false
            }) { Text(text = stringResource(id = com.horsegallop.core.R.string.ok)) }
          },
          dismissButton = {
            TextButton(onClick = { showDatePicker = false }) { Text(text = stringResource(id = com.horsegallop.core.R.string.cancel)) }
          }
        ) {
          androidx.compose.material3.DatePicker(
            state = datePickerState,
            colors = androidx.compose.material3.DatePickerDefaults.colors(
              selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
              selectedDayContentColor = MaterialTheme.colorScheme.onSecondary
            )
          )
        }
      }
      
    }
  }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(
      dimensionResource(id = com.horsegallop.core.R.dimen.spacing_md)
    ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Preview(showBackground = true, name = "ProfileScreen")
@Composable
private fun PreviewProfileScreen() {
  MaterialTheme { ProfileScreen(onBack = {}, onLogout = {}) }
}
