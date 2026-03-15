@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.barnmanagement.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horsegallop.R
import com.horsegallop.ui.theme.LocalSemanticColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CreateLessonScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateLessonViewModel = hiltViewModel()
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.success) {
        if (uiState.success) onSuccess()
    }

    CreateLessonContent(
        isSubmitting = uiState.isSubmitting,
        error = uiState.error,
        onBack = onBack,
        onSubmit = { title, instructor, startTimeMs, duration, level, price, capacity ->
            viewModel.createLesson(title, instructor, startTimeMs, duration, level, price, capacity)
        },
        onClearError = viewModel::clearError
    )
}

@Composable
fun CreateLessonContent(
    isSubmitting: Boolean,
    error: String?,
    onBack: () -> Unit,
    onSubmit: (title: String, instructor: String, startTimeMs: Long, durationMin: Int, level: String, price: Double, spotsTotal: Int) -> Unit,
    onClearError: () -> Unit
) {
    val semantic = LocalSemanticColors.current

    var title by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("60") }
    var priceText by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("8") }
    var selectedLevel by remember { mutableStateOf("") }
    var levelMenuExpanded by remember { mutableStateOf(false) }

    val levels = listOf(
        stringResource(R.string.barn_level_beginner),
        stringResource(R.string.barn_level_intermediate),
        stringResource(R.string.barn_level_advanced)
    )

    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) } }
    var selectedDateMs by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateTimeLabel = remember(selectedDateMs) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(selectedDateMs)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMs
    )
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = 0
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        val cal = Calendar.getInstance().apply { timeInMillis = ms }
                        val timeCal = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
                        cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                        cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                        selectedDateMs = cal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    selectedDateMs = cal.timeInMillis
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.barn_create_lesson_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.barn_lesson_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = instructor,
                onValueChange = { instructor = it },
                label = { Text(stringResource(R.string.barn_instructor_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = dateTimeLabel,
                onValueChange = {},
                label = { Text(stringResource(R.string.date_time)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Row {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(stringResource(R.string.date_time).take(4))
                        }
                        TextButton(onClick = { showTimePicker = true }) {
                            Text("HH:mm")
                        }
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) durationText = it },
                    label = { Text(stringResource(R.string.barn_lesson_duration_label)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) capacityText = it },
                    label = { Text(stringResource(R.string.barn_lesson_capacity_label)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            ExposedDropdownMenuBox(
                expanded = levelMenuExpanded,
                onExpandedChange = { levelMenuExpanded = !levelMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLevel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.barn_lesson_level_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = levelMenuExpanded,
                    onDismissRequest = { levelMenuExpanded = false }
                ) {
                    levels.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level) },
                            onClick = {
                                selectedLevel = level
                                levelMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = priceText,
                onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d*\\.?\\d*"))) priceText = it },
                label = { Text(stringResource(R.string.barn_lesson_price_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("₺") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 60
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val capacity = capacityText.toIntOrNull() ?: 8
                    onSubmit(title, instructor, selectedDateMs, duration, selectedLevel, price, capacity)
                },
                enabled = !isSubmitting && title.isNotBlank() && instructor.isNotBlank() && selectedLevel.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.barn_lesson_save),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateLessonPreview() {
    MaterialTheme {
        CreateLessonContent(
            isSubmitting = false,
            error = null,
            onBack = {},
            onSubmit = { _, _, _, _, _, _, _ -> },
            onClearError = {}
        )
    }
}
