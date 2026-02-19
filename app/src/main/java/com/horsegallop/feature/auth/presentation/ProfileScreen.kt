@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
package com.horsegallop.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.horsegallop.core.R
import com.horsegallop.core.theme.AppColors
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.horsegallop.core.theme.LocalTextColors

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = androidx.compose.foundation.layout.LocalContext.current

    // Modern HorseGallop Theme Colors
    val primaryColor = androidx.compose.ui.graphics.Color(0xFF6B954A)
    val accentGoldColor = androidx.compose.ui.graphics.Color(0xFFC8A25E)
    val pastelColor = androidx.compose.ui.graphics.Color(0xFFF5E6D3)

    // Image Picker
    val pickMediaLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.updateProfileImage(uri)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            pickMediaLauncher.launch(
                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        }
    }

    // Determine which profile to show
    val displayProfile = if (state.isEditing) state.draftProfile else state.userProfile
    val nameDisplay =
        listOf(displayProfile.firstName, displayProfile.lastName).filter { it.isNotBlank() }
            .joinToString(" ")
    val fallbackProfile = stringResource(id = R.string.profile)

    // Toast Messages
    if (state.error != null) {
        LaunchedEffect(state.error) {
            android.widget.Toast.makeText(ctx, state.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    if (state.successMessage != null) {
        LaunchedEffect(state.successMessage) {
            android.widget.Toast.makeText(ctx, state.successMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // Error Toasts
    if (state.error != null) {
        LaunchedEffect(state.error) {
            android.widget.Toast.makeText(ctx, state.error, android.widget.Toast.LENGTH_LONG)
                .show()
        }
    }

    // Success Toast
    if (state.successMessage != null) {
        LaunchedEffect(state.successMessage) {
            android.widget.Toast.makeText(ctx, state.successMessage, android.widget.Toast.LENGTH_SHORT)
                .show()
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.profile),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.accessibility_back),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
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
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_screen_horizontal)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.section_spacing_md))
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Modern Profile Header Card
                ProfileHeaderCard(
                    displayProfile = displayProfile,
                    isEditing = state.isEditing,
                    onEditClick = { viewModel.toggleEdit() },
                    onPhotoClick = {
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            pickMediaLauncher.launch(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                    onAddPhotoClick = {
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            pickMediaLauncher.launch(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Stats Row - Modern Card Design
                ProfileStatsRow(
                    totalRides = state.totalRides ?: 0,
                    totalTimeHours = state.totalTimeHours ?: 0f,
                    totalDistanceKm = state.totalDistanceKm ?: 0f,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Fields Card
                Surface(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_xl)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = dimensionResource(id = R.dimen.elevation_sm),
                    border = androidx.compose.foundation.BorderStroke(
                        dimensionResource(id = R.dimen.width_divider_thin),
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_md)),
                        verticalArrangement = Arrangement.spacedBy(
                            dimensionResource(id = R.dimen.list_item_spacing_md)
                        )
                    ) {
                        if (!state.isEditing) {
                            ProfileInfoRow(
                                icon = Icons.Filled.Person,
                                label = stringResource(id = R.string.label_full_name),
                                value = nameDisplay
                            )
                            HorizontalDivider()
                            ProfileInfoRow(
                                icon = Icons.Filled.Phone,
                                label = stringResource(id = R.string.label_phone),
                                value = formatMaskedPhone(
                                    displayProfile.countryCode,
                                    displayProfile.phone.filter { it.isDigit() }
                                )
                            )
                            HorizontalDivider()
                            ProfileInfoRow(
                                icon = Icons.Filled.CalendarToday,
                                label = stringResource(id = R.string.label_birth_date),
                                value = displayProfile.birthDate
                            )
                            HorizontalDivider()
                            ProfileInfoRow(
                                icon = Icons.Filled.Email,
                                label = stringResource(id = R.string.label_email),
                                value = displayProfile.email
                            )
                            HorizontalDivider()
                            ProfileInfoRow(
                                icon = Icons.Filled.LocationOn,
                                label = stringResource(id = R.string.label_city),
                                value = displayProfile.city
                            )
                        } else {
                            // Edit Mode Fields
                            EditableInfoRow(
                                icon = Icons.Filled.Person,
                                label = stringResource(id = R.string.label_first_name)
                            ) {
                                OutlinedTextField(
                                    value = displayProfile.firstName,
                                    onValueChange = { viewModel.updateDraft(firstName = it) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(
                                            min = dimensionResource(id = R.dimen.height_button_md)
                                        ),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.30f
                                        )
                                    )
                                )
                            }
                            EditableInfoRow(
                                icon = Icons.Filled.Person,
                                label = stringResource(id = R.string.label_last_name)
                            ) {
                                OutlinedTextField(
                                    value = displayProfile.lastName,
                                    onValueChange = { viewModel.updateDraft(lastName = it) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(
                                            min = dimensionResource(id = R.dimen.height_button_md)
                                        ),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.30f
                                        )
                                    )
                                )
                            }

                            val countryCodes = state.countryCodes
                            EditableInfoRow(icon = Icons.Filled.Phone, label = stringResource(id = R.string.label_phone)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_sm)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorseGallopDropdown(
                                        value = displayProfile.countryCode,
                                        onValueChange = { viewModel.updateDraft(countryCode = it) },
                                        options = countryCodes,
                                        modifier = Modifier.width(100.dp)
                                            .heightIn(
                                                min = dimensionResource(id = R.dimen.height_button_md)
                                            )
                                    )

                                    OutlinedTextField(
                                        value = displayProfile.phone,
                                        onValueChange = {
                                            if (it.length <= 15 && it.all { c -> c.isDigit() }) viewModel.updateDraft(
                                                phone = it
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).heightIn(
                                            min = dimensionResource(id = R.dimen.height_button_md)
                                        ),
                                        textStyle = MaterialTheme.typography.bodySmall,
                                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.30f
                                            )
                                        ),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                        ),
                                        visualTransformation = PhoneVisualTransformation(
                                            displayProfile.countryCode
                                        )
                                    )
                                }
                            }

                            val context = androidx.compose.foundation.layout.LocalContext.current
                            EditableInfoRow(
                                icon = Icons.Filled.CalendarToday,
                                label = "Date of Birth"
                            ) {
                                HorseGallopDatePicker(
                                    value = displayProfile.birthDate,
                                    onDateSelected = {
                                        val calendar = java.util.Calendar.getInstance()
                                        val datePickerDialog = android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val formatted =
                                                    String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                                viewModel.updateDraft(birthDate = formatted)
                                            },
                                            calendar.get(java.util.Calendar.YEAR),
                                            calendar.get(java.util.Calendar.MONTH),
                                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                        )
                                        datePickerDialog.show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                        .heightIn(
                                            min = dimensionResource(id = R.dimen.height_button_md)
                                        )
                                )
                            }

                            val cities = remember(context) {
                                try {
                                    context.resources.getStringArray(R.array.city_list).toList()
                                } catch (e: Exception) {
                                    com.horsegallop.core.debug.AppLog.e(
                                        "ProfileScreen",
                                        "Error loading city list: ${e.message}"
                                    )
                                    emptyList()
                                }
                            }
                            EditableInfoRow(
                                icon = Icons.Filled.LocationOn,
                                label = stringResource(id = R.string.label_city)
                            ) {
                                HorseGallopDropdown(
                                    value = displayProfile.city,
                                    onValueChange = { viewModel.updateDraft(city = it) },
                                    options = cities,
                                    modifier = Modifier.fillMaxWidth()
                                        .heightIn(
                                            min = dimensionResource(id = R.dimen.height_button_md)
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Card
                Surface(
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_xl)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = dimensionResource(id = R.dimen.elevation_sm),
                    border = androidx.compose.foundation.BorderStroke(
                        dimensionResource(id = R.dimen.width_divider_thin),
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_md)),
                        verticalArrangement = Arrangement.spacedBy(
                            dimensionResource(id = R.dimen.list_item_spacing_md)
                        )
                    ) {
                        ActionButton(
                            icon = Icons.Filled.Settings,
                            label = stringResource(id = R.string.label_settings),
                            onClick = { /* TODO */ },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ActionButton(
                            icon = Icons.Filled.Notifications,
                            label = stringResource(id = R.string.label_notifications),
                            onClick = { /* TODO */ },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ActionButton(
                            icon = Icons.Filled.Support,
                            label = stringResource(id = R.string.label_help_center),
                            onClick = { /* TODO */ },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                if (!state.isEditing) {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg))
                    ) {
                        Text(text = stringResource(id = R.string.button_logout))
                    }
                } else {
                    // Edit Mode Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = androidx.compose.foundation.layout.WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleEdit() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                            enabled = !state.isLoading
                        ) {
                            Text(text = stringResource(id = R.string.button_cancel))
                        }
                        Button(
                            onClick = { viewModel.saveProfile() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = stringResource(id = R.string.button_save))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        HorseLoadingOverlay(visible = state.isLoading)
    }
}

@Composable
fun ProfileHeaderCard(
    displayProfile: com.horsegallop.domain.auth.model.Profile,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onAddPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_xl)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = dimensionResource(id = R.dimen.elevation_md),
        border = androidx.compose.foundation.BorderStroke(
            dimensionResource(id = R.dimen.width_divider_thin),
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_lg)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar with Edit Button - Modern Design
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        if (displayProfile.photoUrl != null) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (displayProfile.photoUrl != null) {
                    // Modern AsyncImage Placeholder - Circle with border
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                            .border(
                                androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                                CircleShape
                            )
                    ) {
                        // Add your image loading library here ( Coil/Glide/Jetpack Compose Image)
                        androidx.compose.material3.Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Default avatar with horse-themed placeholder
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(AppColors.ActionLesson.copy(alpha = 0.15f))
                            .border(
                                androidx.compose.foundation.BorderStroke(3.dp, AppColors.ActionLesson),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.PedalCycle,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = AppColors.ActionLesson
                        )
                    }
                }

                // Edit Badge - Modern floating action style
                if (!isEditing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(
                                androidx.compose.foundation.BorderStroke(
                                    dimensionResource(id = R.dimen.width_divider_thin),
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                ),
                                shape = CircleShape
                            )
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.accessibility_edit),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Add photo button in edit mode - Modern style
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppColors.ActionLesson)
                            .border(
                                androidx.compose.foundation.BorderStroke(
                                    dimensionResource(id = R.dimen.width_divider_thin),
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                ),
                                shape = CircleShape
                            )
                            .clickable { onAddPhotoClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = stringResource(id = R.string.accessibility_add_photo),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Name - Modern typography
            androidx.compose.material3.Text(
                text = if (nameDisplay.isNotBlank()) nameDisplay else stringResource(id = R.string.default_user_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email with icon
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Text(
                    text = displayProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Type Badge
            if (displayProfile.userType != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeColor = when (displayProfile.userType) {
                        "rider" -> AppColors.ActionLesson
                        "horse_owner" -> AppColors.WarmClay
                        "barn_owner" -> AppColors.Success
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(
                                androidx.compose.foundation.BorderStroke(1.dp, badgeColor),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Default.PedalCycle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = badgeColor
                            )
                            androidx.compose.material3.Text(
                                text = when (displayProfile.userType) {
                                    "rider" -> stringResource(id = R.string.user_type_rider)
                                    "horse_owner" -> stringResource(id = R.string.user_type_horse_owner)
                                    "barn_owner" -> stringResource(id = R.string.user_type_barn_owner)
                                    else -> displayProfile.userType
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatsRow(
    totalRides: Int,
    totalTimeHours: Float,
    totalDistanceKm: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ride Card 1
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
            color = AppColors.ActionLesson.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                dimensionResource(id = R.dimen.width_divider_thin),
                AppColors.ActionLesson
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_sm)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.Icon(
                    Icons.Filled.DirectionsRide,
                    contentDescription = null,
                    tint = AppColors.ActionLesson,
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.material3.Text(
                    text = totalRides.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.ActionLesson
                )
                androidx.compose.material3.Text(
                    text = stringResource(id = R.string.label_rides),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.ActionLesson
                )
            }
        }

        // Ride Card 2
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
            color = AppColors.ActionSchedule.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                dimensionResource(id = R.dimen.width_divider_thin),
                AppColors.ActionSchedule
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_sm)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.Icon(
                    Icons.Filled.HourglassEmpty,
                    contentDescription = null,
                    tint = AppColors.ActionSchedule,
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.material3.Text(
                    text = String.format("%.1f", totalTimeHours),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.ActionSchedule
                )
                androidx.compose.material3.Text(
                    text = stringResource(id = R.string.label_hours),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.ActionSchedule
                )
            }
        }

        // Ride Card 3
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)),
            color = AppColors.ActionRestaurant.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(
                dimensionResource(id = R.dimen.width_divider_thin),
                AppColors.ActionRestaurant
            )
        ) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_card_sm)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.Icon(
                    Icons.Filled.Map,
                    contentDescription = null,
                    tint = AppColors.ActionRestaurant,
                    modifier = Modifier.size(24.dp)
                )
                androidx.compose.material3.Text(
                    text = "${totalDistanceKm.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.ActionRestaurant
                )
                androidx.compose.material3.Text(
                    text = stringResource(id = R.string.label_km),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.ActionRestaurant
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_lg)))
            .clickable { onClick() }
            .padding(dimensionResource(id = R.dimen.padding_card_md)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_md))
    ) {
        androidx.compose.material3.Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        androidx.compose.material3.Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
        androidx.compose.material3.Icon(
            Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            androidx.compose.material3.Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.material3.Text(if (value.isNotBlank()) value else "-", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun EditableInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            androidx.compose.material3.Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

class PhoneVisualTransformation(val countryCode: String) : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
    }
}

fun formatMaskedPhone(countryCode: String, phone: String): String {
    return if (phone.isNotBlank()) "$countryCode $phone" else "-"
}

private val nameDisplay = ""
