@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.auth.presentation

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.core.components.HorseGallopDatePicker
import com.horsegallop.core.components.HorseGallopDropdown
import com.horsegallop.core.components.HorseGallopTextField
import com.horsegallop.core.components.HorseLoadingOverlay
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors
import java.util.Calendar
import java.util.Locale

// ─── Entry composable ─────────────────────────────────────────────────────────

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val feedback = LocalAppFeedbackController.current

    val cities = remember {
        runCatching { context.resources.getStringArray(R.array.city_list).toList() }
            .getOrElse { emptyList() }
    }

    val saveLabel = stringResource(id = R.string.button_save)
    val saveChangesLabel = stringResource(id = R.string.save_changes)

    LaunchedEffect(Unit) { viewModel.startEditSession() }

    LaunchedEffect(state.errorMessageResId) {
        state.errorMessageResId?.let { resId ->
            feedback.showError(resId)
            viewModel.clearMessages()
        }
    }

    BackHandler {
        viewModel.discardEditSession()
        onBack()
    }

    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                viewModel.updateDraft(birthDate = formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    EditProfileContent(
        state = state,
        cities = cities,
        saveLabel = saveLabel,
        saveChangesLabel = saveChangesLabel,
        onBack = {
            viewModel.discardEditSession()
            onBack()
        },
        onSave = { viewModel.saveProfile(onSuccess = onBack) },
        onFirstNameChange = { viewModel.updateDraft(firstName = it) },
        onLastNameChange = { viewModel.updateDraft(lastName = it) },
        onCountryCodeChange = { viewModel.updateDraft(countryCode = it) },
        onPhoneChange = { viewModel.updateDraft(phone = it) },
        onCityChange = { viewModel.updateDraft(city = it) },
        onBirthDateClick = { datePickerDialog.show() },
        onWeightChange = { viewModel.updateDraft(weightInput = it) }
    )
}

// ─── Content (Preview-able) ───────────────────────────────────────────────────

@Composable
private fun EditProfileContent(
    state: ProfileUiState,
    cities: List<String>,
    saveLabel: String,
    saveChangesLabel: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onCountryCodeChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onBirthDateClick: () -> Unit,
    onWeightChange: (String) -> Unit
) {
    val semantic = LocalSemanticColors.current
    val profile = state.draftProfile
    val weightInputRegex = remember { Regex("^\\d{0,3}(\\.\\d{0,2})?$") }

    val fullName = remember(profile.firstName, profile.lastName) {
        "${profile.firstName} ${profile.lastName}".trim()
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.edit_profile_title)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = !state.isSaving
                    ) {
                        Text(text = saveLabel)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isSaving) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            // ── Hero kartı ───────────────────────────────────────────────────
            item {
                ProfileHeroCard(
                    profile = profile,
                    fullName = fullName,
                    onPhotoClick = { /* TODO: photo picker */ }
                )
            }

            // ── Kişisel Bilgiler ─────────────────────────────────────────────
            item {
                ProfileSectionCard(
                    title = stringResource(id = R.string.profile_section_personal),
                    subtitle = stringResource(id = R.string.profile_description)
                ) {
                    // Ad + Soyad yan yana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorseGallopTextField(
                            value = profile.firstName,
                            onValueChange = onFirstNameChange,
                            label = stringResource(id = R.string.label_first_name),
                            isError = state.formErrors.firstNameResId != null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(ProfileTestTags.FirstNameField)
                        )
                        HorseGallopTextField(
                            value = profile.lastName,
                            onValueChange = onLastNameChange,
                            label = stringResource(id = R.string.label_last_name),
                            isError = state.formErrors.lastNameResId != null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(ProfileTestTags.LastNameField)
                        )
                    }

                    // Hata mesajları
                    val firstErr = state.formErrors.firstNameResId
                    val lastErr = state.formErrors.lastNameResId
                    if (firstErr != null || lastErr != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (firstErr != null) {
                                Text(
                                    text = stringResource(id = firstErr),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            if (lastErr != null) {
                                Text(
                                    text = stringResource(id = lastErr),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // E-posta (salt okunur)
                    OutlinedTextField(
                        value = profile.email,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = {
                            Text(
                                text = stringResource(id = R.string.label_email),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = semantic.cardSubtle,
                            unfocusedContainerColor = semantic.cardSubtle,
                            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // ── İletişim ─────────────────────────────────────────────────────
            item {
                ProfileSectionCard(
                    title = stringResource(id = R.string.profile_section_contact)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorseGallopDropdown(
                            value = profile.countryCode,
                            onValueChange = onCountryCodeChange,
                            options = state.countryCodes,
                            modifier = Modifier.width(110.dp)
                        )
                        HorseGallopTextField(
                            value = profile.phone,
                            onValueChange = { input ->
                                if (input.length <= 15 && input.all { c -> c.isDigit() }) {
                                    onPhoneChange(input)
                                }
                            },
                            label = stringResource(id = R.string.label_phone),
                            isError = state.formErrors.phoneResId != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .weight(1f)
                                .testTag(ProfileTestTags.PhoneField)
                        )
                    }
                    state.formErrors.phoneResId?.let { resId ->
                        Text(
                            text = stringResource(id = resId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    HorseGallopDropdown(
                        value = profile.city,
                        onValueChange = onCityChange,
                        options = cities,
                        label = stringResource(id = R.string.label_city),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Sağlık ───────────────────────────────────────────────────────
            item {
                ProfileSectionCard(
                    title = stringResource(id = R.string.profile_section_health)
                ) {
                    HorseGallopDatePicker(
                        value = profile.birthDate,
                        onDateSelected = onBirthDateClick,
                        label = stringResource(id = R.string.label_birth_date),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.draftWeightInput,
                        onValueChange = { input ->
                            if (input.isBlank() || weightInputRegex.matches(input)) {
                                onWeightChange(input)
                            }
                        },
                        singleLine = true,
                        label = {
                            Text(
                                text = stringResource(id = R.string.label_weight),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            Text(
                                text = stringResource(id = R.string.unit_kg),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        isError = state.formErrors.weightResId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag(ProfileTestTags.WeightField),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = semantic.cardElevated,
                            unfocusedContainerColor = semantic.cardElevated,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                        )
                    )
                    state.formErrors.weightResId?.let { resId ->
                        Text(
                            text = stringResource(id = resId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── Kaydet Butonu ────────────────────────────────────────────────
            item {
                HorseGallopButton(
                    text = saveChangesLabel,
                    onClick = onSave,
                    enabled = !state.isSaving,
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(
                        WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding() + 12.dp
                    )
                )
            }
        }
    }

    HorseLoadingOverlay(visible = state.isLoading)
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    AppTheme {
        val fakeState = ProfileUiState(
            draftProfile = UserProfile(
                firstName = "Ayşe",
                lastName = "Yılmaz",
                email = "ayse@horsegallop.com",
                phone = "5551234567",
                countryCode = "+90",
                city = "İstanbul",
                birthDate = "1990-05-15"
            ),
            draftWeightInput = "65",
            countryCodes = listOf("+90", "+1", "+44")
        )
        EditProfileContent(
            state = fakeState,
            cities = listOf("İstanbul", "Ankara", "İzmir"),
            saveLabel = "Kaydet",
            saveChangesLabel = "Değişiklikleri Kaydet",
            onBack = {},
            onSave = {},
            onFirstNameChange = {},
            onLastNameChange = {},
            onCountryCodeChange = {},
            onPhoneChange = {},
            onCityChange = {},
            onBirthDateClick = {},
            onWeightChange = {}
        )
    }
}
