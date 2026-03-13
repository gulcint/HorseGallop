package com.horsegallop.feature.horse.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.ChipSelector
import com.horsegallop.core.components.HorseGallopButton
import com.horsegallop.core.components.HorseGallopDatePicker
import com.horsegallop.core.components.HorseGallopDropdown
import com.horsegallop.core.components.HorseGallopTextField
import com.horsegallop.domain.horse.model.HorseGender
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

private val COAT_COLORS = listOf(
    "Doru", "Kula", "Kır", "Yağız", "Yağız Doru", "Al", "Boz", "Ala", "Diğer"
)

private val BIRTH_YEARS =
    (1985..(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))).toList().reversed()

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
                title = {
                    Text(
                        text = stringResource(R.string.add_horse_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Hero — at avatarı + motivasyon metni
            AddHorseHeroSection(name = name)

            // Temel Bilgiler Kartı
            AddHorseSection(title = stringResource(R.string.add_horse_section_basic)) {
                HorseGallopTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = stringResource(R.string.add_horse_name_label),
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError
                )
                if (nameError) {
                    Text(
                        text = stringResource(R.string.add_horse_name_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Irk — HorseGallopDropdown
                HorseGallopDropdown(
                    value = selectedBreed,
                    onValueChange = { selectedBreed = it },
                    options = breedOptions,
                    label = stringResource(R.string.add_horse_breed_label),
                    placeholder = stringResource(R.string.add_horse_select_hint),
                    modifier = Modifier.fillMaxWidth()
                )

                // Cinsiyet
                ChipSelector(
                    title = stringResource(R.string.add_horse_gender_label),
                    options = HorseGender.entries,
                    selected = selectedGender,
                    onSelect = { selectedGender = it },
                    label = { it.displayName }
                )
            }

            // Fiziksel Özellikler Kartı
            AddHorseSection(title = stringResource(R.string.add_horse_section_physical)) {
                // Renk
                ChipSelector(
                    title = stringResource(R.string.add_horse_color_label),
                    options = COAT_COLORS,
                    selected = selectedColor,
                    onSelect = { selectedColor = it },
                    label = { it }
                )

                // Doğum yılı — HorseGallopDatePicker stilinde tıklanabilir alan
                HorseGallopDatePicker(
                    value = selectedBirthYear?.toString() ?: "",
                    onDateSelected = { showYearPicker = true },
                    label = stringResource(R.string.add_horse_birth_year_label),
                    placeholder = stringResource(R.string.add_horse_select_hint),
                    modifier = Modifier.fillMaxWidth()
                )

                // Ağırlık slider
                WeightSection(
                    weightEnabled = weightEnabled,
                    weightSlider = weightSlider,
                    onEnable = { weightEnabled = true },
                    onWeightChange = { weightSlider = it }
                )
            }

            // Hata + Kaydet
            uiState.saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            HorseGallopButton(
                text = stringResource(R.string.add_horse_save_button),
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@HorseGallopButton
                    }
                    viewModel.addHorse(
                        name = name,
                        breed = selectedBreed,
                        birthYear = selectedBirthYear?.toString() ?: "",
                        color = selectedColor ?: "",
                        gender = selectedGender,
                        weightKg = if (weightEnabled) weightSlider.toInt().toString() else ""
                    )
                },
                isLoading = uiState.saving,
                enabled = !uiState.saving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Hero banner ────────────────────────────────────────────────────────────

@Composable
private fun AddHorseHeroSection(name: String) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val semantic = LocalSemanticColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(primary.copy(alpha = 0.92f), secondary.copy(alpha = 0.80f))
                )
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // At avatarı — baş harf veya 🐴 emoji
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(semantic.onImageOverlay.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (name.isNotBlank()) name.first().uppercaseChar().toString() else "🐴",
                    style = MaterialTheme.typography.headlineMedium,
                    color = semantic.onImageOverlay
                )
            }
            Column {
                Text(
                    text = if (name.isNotBlank()) name
                    else stringResource(R.string.add_horse_hero_placeholder),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = semantic.onImageOverlay
                )
                Text(
                    text = stringResource(R.string.add_horse_hero_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.onImageOverlay.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ─── Bölüm kartı ─────────────────────────────────────────────────────────────

@Composable
private fun AddHorseSection(
    title: String,
    content: @Composable () -> Unit
) {
    val semantic = LocalSemanticColors.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
            border = BorderStroke(1.dp, semantic.cardStroke)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}

// ─── Ağırlık slider ─────────────────────────────────────────────────────────

@Composable
private fun WeightSection(
    weightEnabled: Boolean,
    weightSlider: Float,
    onEnable: () -> Unit,
    onWeightChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.add_horse_weight_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (weightEnabled) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${weightSlider.toInt()} kg",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                TextButton(onClick = onEnable) {
                    Text(
                        text = stringResource(R.string.add_horse_weight_add),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        if (weightEnabled) {
            Slider(
                value = weightSlider,
                onValueChange = onWeightChange,
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
                Text(
                    "200 kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "700 kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// ─── Yıl seçici dialog ─────────────────────────────────────────────────────

@Composable
private fun YearPickerDialog(
    years: List<Int>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_horse_birth_year_dialog_title)) },
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
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_horse_cancel))
            }
        }
    )
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AddHorseScreenPreview() {
    AppTheme {
        AddHorseScreen(onBack = {})
    }
}
