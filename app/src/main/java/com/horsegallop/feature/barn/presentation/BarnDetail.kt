@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import android.widget.Toast
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.core.R
import com.valentinilk.shimmer.shimmer

@Composable
fun BarnDetailScreen(
    onBack: () -> Unit,
    viewModel: BarnDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is BarnDetailUiState.Loading -> BarnDetailShimmer()
                is BarnDetailUiState.Success -> BarnDetailContent(barn = state.barn)
                is BarnDetailUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
        }
    }
}

@Composable
fun BarnDetailContent(barn: BarnWithLocation) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?w=1200", // Placeholder or from barn object
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Title and Rating
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = barn.barn.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        // Location
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.barn_detail_location),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = barn.barn.location.ifEmpty { "Unknown Location" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Description
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.barn_detail_description),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = barn.barn.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Amenities / Tags
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.barn_detail_amenities),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(barn.barn.tags) { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(text = tag.replace("_", " ").replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
        }
    }
    
    // Bottom Action Button
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = {
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.reserve),
                        Toast.LENGTH_SHORT
                    )
                    .show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.barn_detail_book_now))
        }
    }
}

@Composable
fun BarnDetailShimmer() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(200.dp)
                .height(32.dp)
                .background(Color.LightGray, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(150.dp)
                .height(24.dp)
                .background(Color.LightGray, RoundedCornerShape(4.dp))
        )
    }
}
