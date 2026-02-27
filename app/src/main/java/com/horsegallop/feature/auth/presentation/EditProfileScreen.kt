@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.auth.presentation

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.HorseGallopDatePicker
import com.horsegallop.core.components.HorseGallopDropdown
import com.horsegallop.core.components.HorseLoadingOverlay
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.ui.theme.LocalSemanticColors
import java.util.Calendar
import java.util.Locale

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val semantic = LocalSemanticColors.current
    val feedback = LocalAppFeedbackController.current

    val profile = state.draftProfile
    val cities = remember {
        runCatching { context.resources.getStringArray(R.array.city_list).toList() }
            .getOrElse { emptyList() }
    }

    val weightInputRegex = remember { Regex("^\\d{0,3}(\\.\\d{0,2})?$") }

    val saveAction: () -> Unit = {
        viewModel.saveProfile(onSuccess = onBack)
    }

    LaunchedEffect(Unit) {
        viewModel.startEditSession()
    }

    LaunchedEffect(state.errorMessageResId) {
        state.errorMessageResId?.let { messageResId ->
            feedback.showError(messageResId)
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

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.edit_profile_title)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(
                        onClick = {
                            viewModel.discardEditSession()
                            onBack()
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = saveAction,
                        enabled = !state.isSaving
                    ) {
                        Text(text = stringResource(id = R.string.button_save))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = saveAction,
                    enabled = !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag(ProfileTestTags.SaveButton),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.save_changes))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.14f),
                            semantic.screenBase
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.isSaving) {
                    item {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                item {
                    ProfileSectionCard(
                        title = stringResource(id = R.string.profile_section_personal),
                        subtitle = stringResource(id = R.string.profile_description)
                    ) {
                        FormSectionTitle(
                            icon = Icons.Filled.Person,
                            title = stringResource(id = R.string.label_first_name)
                        )
                        OutlinedTextField(
                            value = profile.firstName,
                            onValueChange = { viewModel.updateDraft(firstName = it) },
                            singleLine = true,
                            isError = state.formErrors.firstNameResId != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag(ProfileTestTags.FirstNameField),
                            shape = RoundedCornerShape(14.dp),
                            colors = editFieldColors()
                        )
                        ValidationMessage(state.formErrors.firstNameResId)

                    FormSectionTitle(
                        icon = Icons.Filled.Badge,
                        title = stringResource(id = R.string.label_last_name)
                    )
                    OutlinedTextField(
                        value = profile.lastName,
                        onValueChange = { viewModel.updateDraft(lastName = it) },
                        singleLine = true,
                        isError = state.formErrors.lastNameResId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag(ProfileTestTags.LastNameField),
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors()
                    )
                    ValidationMessage(state.formErrors.lastNameResId)

                    FormSectionTitle(
                        icon = Icons.Filled.Email,
                        title = stringResource(id = R.string.label_email)
                    )
                    OutlinedTextField(
                        value = profile.email,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors()
                    )
                }
            }

            item {
                ProfileSectionCard(
                    title = stringResource(id = R.string.profile_section_contact)
                ) {
                    FormSectionTitle(
                        icon = Icons.Filled.ContactPhone,
                        title = stringResource(id = R.string.label_phone)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorseGallopDropdown(
                            value = profile.countryCode,
                            onValueChange = { viewModel.updateDraft(countryCode = it) },
                            options = state.countryCodes,
                            modifier = Modifier.width(110.dp)
                        )

                        OutlinedTextField(
                            value = profile.phone,
                            onValueChange = {
                                if (it.length <= 15 && it.all { c -> c.isDigit() }) {
                                    viewModel.updateDraft(phone = it)
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = state.formErrors.phoneResId != null,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag(ProfileTestTags.PhoneField),
                            shape = RoundedCornerShape(14.dp),
                            colors = editFieldColors()
                        )
                    }
                    ValidationMessage(state.formErrors.phoneResId)

                    FormSectionTitle(
                        icon = Icons.Filled.LocationOn,
                        title = stringResource(id = R.string.label_city)
                    )
                    HorseGallopDropdown(
                        value = profile.city,
                        onValueChange = { viewModel.updateDraft(city = it) },
                        options = cities,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    )
                }
            }

            item {
                ProfileSectionCard(
                    title = stringResource(id = R.string.profile_section_health)
                ) {
                    FormSectionTitle(
                        icon = Icons.Filled.CalendarToday,
                        title = stringResource(id = R.string.label_birth_date)
                    )
                    HorseGallopDatePicker(
                        value = profile.birthDate,
                        onDateSelected = { datePickerDialog.show() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    )

                    FormSectionTitle(
                        icon = Icons.Filled.MonitorWeight,
                        title = stringResource(id = R.string.label_weight)
                    )
                    OutlinedTextField(
                        value = state.draftWeightInput,
                        onValueChange = { input ->
                            if (input.isBlank() || weightInputRegex.matches(input)) {
                                viewModel.updateDraft(weightInput = input)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        trailingIcon = {
                            Text(
                                text = stringResource(id = R.string.unit_kg),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        isError = state.formErrors.weightResId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag(ProfileTestTags.WeightField),
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors()
                    )
                    ValidationMessage(state.formErrors.weightResId)
                }
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
    }

    HorseLoadingOverlay(visible = state.isLoading)
}

@Composable
private fun ValidationMessage(messageResId: Int?) {
    if (messageResId == null) return
    Text(
        text = stringResource(id = messageResId),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LocalSemanticColors.current.cardElevated,
    unfocusedContainerColor = LocalSemanticColors.current.cardElevated,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    errorBorderColor = MaterialTheme.colorScheme.error,
    errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
)
