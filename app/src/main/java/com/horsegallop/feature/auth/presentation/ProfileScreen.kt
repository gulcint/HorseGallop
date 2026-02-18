@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.horsegallop.feature.auth.presentation

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.horsegallop.core.R
import com.horsegallop.core.components.HorseGallopDatePicker
import com.horsegallop.core.components.HorseGallopDropdown
import com.horsegallop.core.theme.*
import java.util.Calendar

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    // Error Toast
    if (state.error != null) {
        LaunchedEffect(state.error) {
            android.widget.Toast.makeText(ctx, state.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Success Toast
    if (state.successMessage != null) {
        LaunchedEffect(state.successMessage) {
            android.widget.Toast.makeText(ctx, state.successMessage, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = WarmClay,
                    titleContentColor = Primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card
            ProfileHeaderCard(
                profile = state.userProfile,
                isEditing = state.isEditing,
                onEditClick = { viewModel.toggleEdit() },
                onChangePhotoClick = {
                    if (state.isEditing) {
                        // Trigger photo change
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Info Section
            ProfileInfoSection(
                profile = state.userProfile,
                isEditing = state.isEditing,
                onValueChange = { key, value ->
                    when (key) {
                        "firstName" -> viewModel.updateDraft(firstName = value)
                        "lastName" -> viewModel.updateDraft(lastName = value)
                        "phone" -> viewModel.updateDraft(phone = value)
                        "birthDate" -> viewModel.updateDraft(birthDate = value)
                        "city" -> viewModel.updateDraft(city = value)
                    }
                },
                countryCodes = state.countryCodes,
                cities = remember(ctx) {
                    try {
                        ctx.resources.getStringArray(R.array.city_list).toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Actions
            if (state.isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.toggleEdit() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { viewModel.saveProfile() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !state.isLoading
                    ) {
                        Text(
                            text = stringResource(id = R.string.button_save),
                            color = Color.White
                        )
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.toggleEdit() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmClay,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.button_edit), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    HorseLoadingOverlay(visible = state.isLoading)
}

@Composable
fun ProfileHeaderCard(
    profile: com.horsegallop.domain.model.UserProfile,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onChangePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Avatar with Edit Badge
            Box(modifier = Modifier.size(100.dp)) {
                // Main avatar circle
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f))
                        .clickable { onChangePhotoClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.photoUrl != null) {
                        android.widget.ImageView(
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alpha = androidx.compose.ui.graphics.graphicsLayer { },
                            imageLoader = androidx.compose.ui.graphics.painter.Painter(),
                            onCommit = {}
                        )
                        // Use AsyncImage for real implementation
                        androidx.compose.foundation.Image(
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alpha = androidx.compose.ui.graphics.graphicsLayer { },
                            imageLoader = androidx.compose.ui.graphics.painter.Painter(),
                            onCommit = {}
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = WarmClay
                        )
                    }
                }

                // Edit badge
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 12.dp, y = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WarmClay)
                            .border(
                                3.dp,
                                Color.White,
                                CircleShape
                            )
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 12.dp, y = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WarmClay)
                            .border(
                                3.dp,
                                Color.White,
                                CircleShape
                            )
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = listOf(profile.firstName, profile.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" "),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileInfoSection(
    profile: com.horsegallop.domain.model.UserProfile,
    isEditing: Boolean,
    onValueChange: (String, String) -> Unit,
    countryCodes: List<String>,
    cities: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            if (!isEditing) {
                // View Mode - Two Column Layout
                ProfileInfoRow(
                    icon = Icons.Filled.Person,
                    label = stringResource(id = R.string.label_full_name),
                    value = listOf(profile.firstName, profile.lastName).filter { it.isNotBlank() }.joinToString(" "),
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoRow(
                    icon = Icons.Filled.Phone,
                    label = stringResource(id = R.string.label_phone),
                    value = formatMaskedPhone(profile.countryCode, profile.phone.filter { it.isDigit() }),
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoRow(
                    icon = Icons.Filled.CalendarToday,
                    label = stringResource(id = R.string.label_birth_date),
                    value = profile.birthDate.ifBlank { "-" },
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoRow(
                    icon = Icons.Filled.Email,
                    label = stringResource(id = R.string.label_email),
                    value = profile.email,
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoRow(
                    icon = Icons.Filled.LocationOn,
                    label = stringResource(id = R.string.label_city),
                    value = profile.city.ifBlank { "-" },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Edit Mode - Input Fields
                ProfileInfoField(
                    icon = Icons.Filled.Person,
                    label = stringResource(id = R.string.label_first_name),
                    value = profile.firstName,
                    onValueChange = { onValueChange("firstName", it) },
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoField(
                    icon = Icons.Filled.Person,
                    label = stringResource(id = R.string.label_last_name),
                    value = profile.lastName,
                    onValueChange = { onValueChange("lastName", it) },
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoField(
                    icon = Icons.Filled.Phone,
                    label = stringResource(id = R.string.label_phone),
                    value = profile.phone,
                    onValueChange = { onValueChange("phone", it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoField(
                    icon = Icons.Filled.CalendarToday,
                    label = stringResource(id = R.string.label_birth_date),
                    value = profile.birthDate,
                    onValueChange = { onValueChange("birthDate", it) },
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileInfoField(
                    icon = Icons.Filled.LocationOn,
                    label = stringResource(id = R.string.label_city),
                    value = profile.city,
                    onValueChange = { onValueChange("city", it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmClay.copy(alpha = 0.1f))
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = WarmClay
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (value.isNotBlank()) value else "-",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Primary
            )
        }
    }
}

@Composable
fun ProfileInfoField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmClay.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = WarmClay
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WarmClay,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedLabelColor = WarmClay,
                cursorColor = WarmClay
            ),
            keyboardOptions = keyboardOptions
        )
    }
}

@Composable
fun formatMaskedPhone(countryCode: String, phone: String): String {
    return if (phone.isNotBlank()) "$countryCode $phone" else "-"
}
