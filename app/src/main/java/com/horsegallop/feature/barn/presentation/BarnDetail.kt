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
import androidx.compose.material.icons.filled.*
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import android.widget.Toast
import com.horsegallop.domain.barn.model.BarnWithLocation
import com.horsegallop.core.R
import com.valentinilk.shimmer.shimmer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BarnDetailScreen(
    onBack: () -> Unit,
    viewModel: BarnDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            val title = (uiState as? BarnDetailUiState.Success)?.barn?.barn?.name ?: ""
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
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
    var showReservationSheet by remember { mutableStateOf(false) }
    var showContactSheet by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                val heroImage = "https://images.unsplash.com/photo-1534448177492-6d6c629f5f92?auto=format&fit=crop&w=1200&q=80"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    AsyncImage(
                        model = heroImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = barn.barn.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("4.8", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = barn.barn.location.ifEmpty { "Istanbul, TR" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amenities Section - Modern Design
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(id = com.horsegallop.core.R.string.barn_detail_amenities),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val tags = if (barn.barn.tags.isEmpty()) listOf("Parking", "Cafe", "Lessons", "Trail", "Vet", "Outdoor") else barn.barn.tags
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        tags.forEach { tag ->
                            AmenityChip(tag = tag)
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = com.horsegallop.core.R.string.barn_detail_description),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = barn.barn.description.ifEmpty { stringResource(id = com.horsegallop.core.R.string.barn_description_fallback) },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            item {
                val barnLocation = LatLng(barn.lat, barn.lng)
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        text = "Find on Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(barnLocation, 14f) },
                            googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) },
                            uiSettings = MapUiSettings(zoomControlsEnabled = false)
                        ) {
                            Marker(state = MarkerState(position = barnLocation), title = barn.barn.name)
                        }
                    }
                }
            }
        }
        
        // Actions
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { showContactSheet = true },
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Chat, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                
                Button(
                    onClick = { showReservationSheet = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Book a Lesson", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showContactSheet) {
            ModalBottomSheet(
                onDismissRequest = { showContactSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                ContactOptionsContent(
                    phoneNumber = "02125555555", // In real app use barn.phone
                    onDismiss = { showContactSheet = false }
                )
            }
        }

        if (showReservationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showReservationSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ReservationContent(
                    onConfirm = {
                        showReservationSheet = false
                        Toast.makeText(context, "Reservation sent!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun AmenityChip(tag: String) {
    val icon = when (tag.lowercase()) {
        "cafe" -> Icons.Default.LocalCafe
        "parking" -> Icons.Default.LocalParking
        "lessons" -> Icons.Default.School
        "trail" -> Icons.Default.Terrain
        "indoor" -> Icons.Default.Home
        "outdoor" -> Icons.Default.WbSunny
        "vet" -> Icons.Default.MedicalServices
        else -> Icons.Default.CheckCircle
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tag.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ContactOptionsContent(phoneNumber: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Contact Barn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("How would you like to reach out?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ContactMethodItem(
            icon = Icons.Default.Phone,
            title = "Phone Call",
            subtitle = phoneNumber,
            color = Color(0xFF4CAF50),
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") }
                context.startActivity(intent)
                onDismiss()
            }
        )
        
        ContactMethodItem(
            icon = Icons.Default.Message,
            title = "Send Message",
            subtitle = "Quick chat via WhatsApp",
            color = Color(0xFF25D366),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://wa.me/$phoneNumber") }
                context.startActivity(intent)
                onDismiss()
            }
        )

        ContactMethodItem(
            icon = Icons.Default.Email,
            title = "Email",
            subtitle = "Send an inquiry",
            color = Color(0xFF2196F3),
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:info@horsegallop.com") }
                context.startActivity(intent)
                onDismiss()
            }
        )
    }
}

@Composable
fun ContactMethodItem(icon: ImageVector, title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

@Composable
fun ReservationContent(onConfirm: () -> Unit) {
    var selectedDateIndex by remember { mutableStateOf(0) }
    var selectedTimeIndex by remember { mutableStateOf<Int?>(null) }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Select Lesson Time", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        
        Column {
            Text("Date", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(7) { index ->
                    val isSelected = selectedDateIndex == index
                    Surface(
                        onClick = { selectedDateIndex = index },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(60.dp, 80.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(days[index], style = MaterialTheme.typography.labelSmall, color = if (isSelected) Color.White else Color.Gray)
                            Text("${25 + index}", fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
                        }
                    }
                }
            }
        }

        Column {
            Text("Available Hours", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("09:00", "11:00", "14:00", "16:00", "18:00").forEachIndexed { index, time ->
                    val isSelected = selectedTimeIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeIndex = index },
                        label = { Text(time) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
        
        Button(
            onClick = onConfirm,
            enabled = selectedTimeIndex != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Confirm Booking")
        }
    }
}

@Composable
fun BarnDetailShimmer() {
    Column(modifier = Modifier.fillMaxSize().shimmer()) {
        Box(modifier = Modifier.fillMaxWidth().height(280.dp).background(Color.LightGray))
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.padding(horizontal = 20.dp).width(200.dp).height(30.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.padding(horizontal = 20.dp).width(150.dp).height(20.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
    }
}
