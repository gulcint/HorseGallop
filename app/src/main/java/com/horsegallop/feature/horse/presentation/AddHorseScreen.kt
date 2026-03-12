package com.horsegallop.feature.horse.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHorseScreen(
    viewModel: HorseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current

    // Breeds loaded dynamically from backend — fallback list shown while loading
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
    var birthYear by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(HorseGender.UNKNOWN) }
    var genderExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            viewModel.clearSaveState()
            onBack()
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))

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
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { birthYear = it },
                    label = { Text("Doğum Yılı") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Örn: 2018") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = { Text("Ağırlık (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Renk") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Örn: Doru, Kır, Kula") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedGender.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cinsiyet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    HorseGender.entries.forEach { gender ->
                        DropdownMenuItem(
                            text = { Text(gender.displayName) },
                            onClick = { selectedGender = gender; genderExpanded = false }
                        )
                    }
                }
            }

            uiState.saveError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    viewModel.addHorse(name, selectedBreed, birthYear, color, selectedGender, weightKg)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.saving
            ) {
                if (uiState.saving) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp).then(Modifier.then(Modifier.padding(2.dp))), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                }
                Text("Kaydet")
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
}
