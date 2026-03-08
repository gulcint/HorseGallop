package com.horsegallop.feature.horse.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.domain.horse.model.Horse
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorseListScreen(
    viewModel: HorseViewModel = hiltViewModel(),
    onAddHorse: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current
    var deleteTarget by remember { mutableStateOf<Horse?>(null) }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Atlarım", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHorse,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "At Ekle", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        when {
            uiState.loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.horses.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🐴", style = MaterialTheme.typography.displayLarge)
                        Text("Henüz at eklemediniz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Atlarınızı ekleyerek antrenmanlarda takip edebilirsiniz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(4.dp))
                        Button(onClick = onAddHorse) { Text("İlk Atı Ekle") }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.horses, key = { it.id }) { horse ->
                        HorseCard(horse = horse, onDelete = { deleteTarget = horse })
                    }
                }
            }
        }
    }

    deleteTarget?.let { horse ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Atı Sil") },
            text = { Text("\"${horse.name}\" adlı atı silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHorse(horse.id)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun HorseCard(horse: Horse, onDelete: () -> Unit) {
    val semantic = LocalSemanticColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🐴", style = MaterialTheme.typography.titleLarge)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(horse.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (horse.breed.isNotBlank()) {
                        Text(horse.breed, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val details = buildList {
                        if (horse.age > 0) add("${horse.age} yaşında")
                        if (horse.color.isNotBlank()) add(horse.color)
                        if (horse.gender.displayName != "Bilinmiyor") add(horse.gender.displayName)
                    }
                    if (details.isNotEmpty()) {
                        Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
