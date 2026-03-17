package com.horsegallop.feature.schedule.presentation

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.domain.schedule.model.Reservation
import com.horsegallop.domain.schedule.model.ReservationStatus
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onWriteReview: (lessonId: String, lessonTitle: String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.my_reservations_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        MyReservationsContent(
            uiState = uiState,
            onWriteReview = onWriteReview,
            onCancelReservation = { reservationId -> viewModel.cancelReservation(reservationId) },
            modifier = Modifier.padding(innerPadding),
            onBack = onBack
        )
    }
}

@Composable
fun MyReservationsContent(
    uiState: ScheduleUiState,
    onWriteReview: (lessonId: String, lessonTitle: String) -> Unit = { _, _ -> },
    onCancelReservation: (reservationId: String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var cancelTarget by remember { mutableStateOf<Reservation?>(null) }

    if (uiState.reservations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("\uD83D\uDC34", style = MaterialTheme.typography.displayMedium)
                Text(
                    stringResource(R.string.my_reservations_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.my_reservations_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(8.dp))
                Button(onClick = onBack) { Text(stringResource(R.string.my_reservations_go_to_schedule)) }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.reservations, key = { it.id }) { reservation ->
                ReservationCard(
                    reservation = reservation,
                    onCancel = { cancelTarget = reservation },
                    onReview = { onWriteReview(reservation.lessonId, reservation.lessonTitle) }
                )
            }
        }
    }

    cancelTarget?.let { reservation ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text(stringResource(R.string.reservation_cancel_title)) },
            text = { Text(stringResource(R.string.reservation_cancel_confirm, reservation.lessonTitle)) },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelReservation(reservation.id)
                        cancelTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.reservation_cancel_action)) }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text(stringResource(R.string.cancel_dismiss)) }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReservationsScreenPreview() {
    MaterialTheme {
        ReservationCard(
            reservation = Reservation(
                id = "res-1",
                lessonId = "lesson-1",
                lessonTitle = "Temel Binicilik",
                lessonDate = "Cumartesi, 15 Mart 10:00",
                instructorName = "Ahmet Yılmaz",
                status = ReservationStatus.CONFIRMED
            ),
            onCancel = {},
            onReview = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReservationsContentPreview() {
    MaterialTheme {
        MyReservationsContent(
            uiState = ScheduleUiState(
                loading = false,
                reservations = listOf(
                    Reservation(
                        id = "res-1",
                        lessonId = "lesson-1",
                        lessonTitle = "Temel Binicilik",
                        lessonDate = "Cumartesi, 15 Mart 10:00",
                        instructorName = "Ahmet Yılmaz",
                        status = ReservationStatus.CONFIRMED
                    ),
                    Reservation(
                        id = "res-2",
                        lessonId = "lesson-2",
                        lessonTitle = "İleri Atlama",
                        lessonDate = "Pazar, 16 Mart 14:00",
                        instructorName = "Zeynep Kaya",
                        status = ReservationStatus.COMPLETED
                    )
                )
            )
        )
    }
}

@Composable
private fun ReservationCard(reservation: Reservation, onCancel: () -> Unit, onReview: () -> Unit = {}) {
    val semantic = LocalSemanticColors.current
    val statusColor = when (reservation.status) {
        ReservationStatus.CONFIRMED -> semantic.success
        ReservationStatus.PENDING -> semantic.warning
        ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.error
        ReservationStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated),
        border = BorderStroke(1.dp, semantic.cardStroke)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reservation.lessonTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = reservation.status.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(reservation.lessonDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Text(reservation.instructorName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (reservation.status == ReservationStatus.COMPLETED) {
                OutlinedButton(
                    onClick = onReview,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reservation_review_action), style = MaterialTheme.typography.labelMedium)
                }
            }
            if (reservation.status == ReservationStatus.CONFIRMED || reservation.status == ReservationStatus.PENDING) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.reservation_cancel_action), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
