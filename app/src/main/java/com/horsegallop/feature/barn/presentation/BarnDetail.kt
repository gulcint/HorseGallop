@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.R
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.valentinilk.shimmer.shimmer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.horsegallop.ui.theme.LocalSemanticColors
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun BarnDetailScreen(
    onBack: () -> Unit,
    viewModel: BarnDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current
    val topBarTitle = when (val state = uiState) {
        is BarnDetailUiState.Success -> state.barn.barn.name
        else -> stringResource(id = R.string.barn_detail_title)
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = topBarTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                    scrolledContainerColor = semantic.panelOverlay,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is BarnDetailUiState.Loading -> BarnDetailShimmer()
                is BarnDetailUiState.Success -> BarnDetailContent(barn = state.barn)
                is BarnDetailUiState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = { viewModel.refresh() }) {
                            Text(text = stringResource(id = R.string.retry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarnDetailContent(barn: BarnWithLocation) {
    val feedback = LocalAppFeedbackController.current
    val coroutineScope = rememberCoroutineScope()
    val semantic = LocalSemanticColors.current
    var showReservationSheet by remember { mutableStateOf(false) }
    val ratingText = if (barn.barn.rating > 0.0) {
        String.format(Locale.US, "%.1f", barn.barn.rating)
    } else {
        "4.8"
    }
    val reviewSummary = if (barn.barn.reviewCount > 0) {
        "${barn.barn.reviewCount} reviews"
    } else {
        stringResource(id = R.string.barn_detail_top_rated)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        semantic.screenBase,
                        semantic.cardSubtle
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 118.dp)
        ) {
            item {
                val barnLocation = LatLng(barn.lat, barn.lng)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(barnLocation, 15f)
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    if (!barn.barn.heroImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = barn.barn.heroImageUrl,
                            contentDescription = barn.barn.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                rotationGesturesEnabled = false,
                                mapToolbarEnabled = false
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = barnLocation),
                                title = barn.barn.name
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, semantic.imageOverlayStrong)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = barn.barn.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = semantic.onImageOverlay
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = semantic.onImageOverlay.copy(alpha = 0.92f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = barn.barn.location.ifEmpty { stringResource(id = R.string.unknown_location) },
                                style = MaterialTheme.typography.labelLarge,
                                color = semantic.onImageOverlay.copy(alpha = 0.92f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = semantic.imageOverlaySoft.copy(alpha = 0.26f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = semantic.ratingStar,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = ratingText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = semantic.onImageOverlay,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = semantic.imageOverlaySoft.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = reviewSummary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = semantic.onImageOverlay
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = semantic.cardElevated,
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(id = R.string.barn_detail_description),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = barn.barn.description.ifEmpty { stringResource(id = R.string.barn_description_fallback) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                val barnLocation = LatLng(barn.lat, barn.lng)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(barnLocation, 14f)
                }

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = semantic.cardElevated,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(id = R.string.label_location),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                googleMapOptionsFactory = {
                                    GoogleMapOptions().liteMode(true)
                                },
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    scrollGesturesEnabled = false,
                                    zoomGesturesEnabled = false,
                                    tiltGesturesEnabled = false,
                                    rotationGesturesEnabled = false
                                )
                            ) {
                                Marker(
                                    state = MarkerState(position = barnLocation),
                                    title = barn.barn.name
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = semantic.cardElevated,
                    tonalElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(id = R.string.barn_detail_amenities),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(barn.barn.tags.ifEmpty { listOf("Parking", "Cafe", "Lessons", "Trail") }) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                                    )
                                ) {
                                    Text(
                                        text = tag.replace("_", " ").replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shadowElevation = 24.dp,
            tonalElevation = 6.dp,
            color = semantic.cardElevated,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Call action */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Text(stringResource(id = com.horsegallop.R.string.contact))
                }
                Button(
                    onClick = { showReservationSheet = true },
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(id = com.horsegallop.R.string.book_lesson))
                }
            }
        }

        if (showReservationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showReservationSheet = false },
                containerColor = semantic.cardElevated,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ReservationContent(
                    onConfirm = {
                        showReservationSheet = false
                        coroutineScope.launch {
                            feedback.showSuccess(R.string.reservation_request_sent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReservationContent(onConfirm: () -> Unit) {
    val semantic = LocalSemanticColors.current
    var selectedDateIndex by remember { mutableIntStateOf(0) }
    var selectedTimeIndex by remember { mutableIntStateOf(-1) }
    var selectedInstructorIndex by remember { mutableIntStateOf(-1) }

    // Mock Instructors - TODO: Fetch from backend
    val instructors = listOf(
        "Ahmet Yilmaz" to "Dressage",
        "Ayse Kaya" to "Show Jumping",
        "Mehmet Demir" to "Beginner Basics"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            stringResource(id = com.horsegallop.R.string.select_date_time),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Mock Date Selection
        // TODO: Fetch from backend (This data will be fetched from backend)
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Column {
            Text(stringResource(id = com.horsegallop.R.string.label_date), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(7) { index ->
                    val isSelected = selectedDateIndex == index
                    Surface(
                        onClick = { selectedDateIndex = index },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.size(width = 64.dp, height = 76.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                days[index % 7],
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${25 + index}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Mock Time Selection
        Column {
            Text(stringResource(id = com.horsegallop.R.string.label_time), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // TODO: Fetch from backend (This data will be fetched from backend)
                listOf("09:00", "11:00", "14:00", "16:00").forEachIndexed { index, time ->
                    val isSelected = selectedTimeIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeIndex = index },
                        label = { Text(time) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Mock Instructor Selection
        Column {
            Text(stringResource(id = com.horsegallop.R.string.label_instructor), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(instructors.size) { index ->
                    val (name, specialty) = instructors[index]
                    val isSelected = selectedInstructorIndex == index
                    
                    Surface(
                        onClick = { selectedInstructorIndex = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else semantic.panelOverlay,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.width(140.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.first().toString(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = specialty,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onConfirm,
            enabled = selectedTimeIndex >= 0 && selectedInstructorIndex >= 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(id = com.horsegallop.R.string.confirm_booking))
        }
    }
}

@Composable
fun BarnDetailShimmer() {
    val shimmerBase = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(shimmerBase)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(200.dp)
                .height(32.dp)
                .background(shimmerBase, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(150.dp)
                .height(24.dp)
                .background(shimmerBase, RoundedCornerShape(8.dp))
        )
    }
}
