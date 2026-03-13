package com.horsegallop.feature.horse.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.core.components.ChipSelector
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.ui.theme.LocalSemanticColors

private val COAT_COLORS = listOf(
    "Doru", "Kula", "Kır", "Yağız", "Yağız Doru", "Al", "Boz", "Ala", "Diğer"
)

private val BIRTH_YEARS = (1985..(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))).toList().reversed()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHorseScreen(
    viewModel: HorseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current

    val breedOptions = uiState.breeds.ifEmpty {
        listOf(
            "Arabian", "Thoroughbred", "Holsteiner", "KWPB", "Hanoverian",
            "Trakehner", "Lusitano", "Andalusian", "Pony", "Selle Français",
            "Quarter Horse", "Mustang", "Akhal-Teke", "Turkish Horse", "Other"
        )
    }

    var name by remember { mutableStateOf("") }
    var selectedBreed by remember { mutableStateOf("") }
    var breedExpanded by remember { mutableStateOf(false) }
    var selectedBirthYear by remember { mutableStateOf<Int?>(null) }
    var showYearPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var weightSlider by remember { mutableFloatStateOf(450f) }
    var weightEnabled by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf(HorseGender.UNKNOWN) }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            viewModel.clearSaveState()
            onBack()
        }
    }

    if (showYearPicker) {
        YearPickerDialog(
            years = BIRTH_YEARS,
            selected = selectedBirthYear,
            onSelect = { selectedBirthYear = it; showYearPicker = false },
            onDismiss = { showYearPicker = false }
        )
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("At Ekle", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("At Adı *") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) ({ Text("At adı zorunludur") }) else null,
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            ExposedDropdownMenuBox(
                expanded = breedExpanded,
                onExpandedChange = { breedExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedBreed,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Irk") },
                    placeholder = { Text("Seçiniz") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = breedExpanded,
                    onDismissRequest = { breedExpanded = false }
                ) {
                    breedOptions.forEach { breed ->
                        DropdownMenuItem(
                            text = { Text(breed) },
                            onClick = { selectedBreed = breed; breedExpanded = false }
                        )
                    }
                }
            }

            ChipSelector(
                title = "Cinsiyet",
                options = HorseGender.entries,
                selected = selectedGender,
                onSelect = { selectedGender = it },
                label = { it.displayName }
            )

            ChipSelector(
                title = "Renk",
                options = COAT_COLORS,
                selected = selectedColor,
                onSelect = { selectedColor = it },
                label = { it }
            )

            OutlinedTextField(
                value = selectedBirthYear?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Doğum Yılı") },
                placeholder = { Text("Seçiniz") },
                trailingIcon = {
                    TextButton(onClick = { showYearPicker = true }) {
                        Text("Seç", style = MaterialTheme.typography.labelSmall)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ağırlık",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    if (weightEnabled) {
                        Text(
                            text = "${weightSlider.toInt()} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        TextButton(onClick = { weightEnabled = true }) {
                            Text("Ekle", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (weightEnabled) {
                    Slider(
                        value = weightSlider,
                        onValueChange = { weightSlider = it },
                        valueRange = 200f..700f,
                        steps = 99,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("200 kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("700 kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            uiState.saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    viewModel.addHorse(
                        name = name,
                        breed = selectedBreed,
                        birthYear = selectedBirthYear?.toString() ?: "",
                        color = selectedColor ?: "",
                        gender = selectedGender,
                        weightKg = if (weightEnabled) weightSlider.toInt().toString() else ""
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.saving
            ) {
                if (uiState.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp).padding(2.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Kaydet")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun YearPickerDialog(
    years: List<Int>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Doğum Yılı Seç") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                years.forEach { year ->
                    TextButton(
                        onClick = { onSelect(year) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (year == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (year == selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (year != years.last()) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}
