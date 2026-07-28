package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.ui.viewmodel.guide.BookingDetailsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bookingId: String,
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAddReview: (String) -> Unit,
    viewModel: BookingDetailsViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val scrollState = rememberScrollState()

    val booking = remember(bookings, bookingId) {
        bookings.firstOrNull { it.id == bookingId }
    }

    var showRescheduleDialog by remember { mutableStateOf(false) }
    var rescheduleDate by remember { mutableStateOf("July 22, 2026") }
    var rescheduleTime by remember { mutableStateOf("09:30 AM") }

    var showConfirmCancel by remember { mutableStateOf(false) }
    var showConfirmReject by remember { mutableStateOf(false) }

    var showEditEmergencyContactDialog by remember { mutableStateOf(false) }
    var emergencyContactText by remember(booking?.emergencyContact) { mutableStateOf(booking?.emergencyContact ?: "+1 (555) 019-8877") }

    var showWriteReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Booking not found.")
        }
        return
    }

    if (showEditEmergencyContactDialog) {
        AlertDialog(
            onDismissRequest = { showEditEmergencyContactDialog = false },
            title = { Text("Edit Emergency Contact", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = emergencyContactText,
                    onValueChange = { emergencyContactText = it },
                    label = { Text("Emergency Contact Number / Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = { showEditEmergencyContactDialog = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEmergencyContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWriteReviewDialog) {
        AlertDialog(
            onDismissRequest = { showWriteReviewDialog = false },
            title = { Text("Write Guide Review", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Rate your experience:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { reviewRating = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= reviewRating) Color(0xFFFFB000) else Color.LightGray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Write your review for the guide...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newReview = com.example.travelplanner.data.repo.ReviewItem(
                            id = java.util.UUID.randomUUID().toString(),
                            travellerName = booking.travellerName.ifBlank { "Traveller" },
                            tourName = booking.tourPackage.ifBlank { "Guided Tour (${booking.destination})" },
                            rating = reviewRating,
                            reviewText = reviewText.ifBlank { "Excellent guide! Very informative and helpful." },
                            date = "Today"
                        )
                        scope.launch {
                            com.example.travelplanner.app.AppGraph.guideRepository.addReviewWritten(newReview)
                        }
                        showWriteReviewDialog = false
                    }
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWriteReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRescheduleDialog) {
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reschedule Booking", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = rescheduleDate,
                        onValueChange = { rescheduleDate = it },
                        label = { Text("New Date") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = rescheduleTime,
                        onValueChange = { rescheduleTime = it },
                        label = { Text("New Time") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rescheduleBooking(bookingId, rescheduleDate, rescheduleTime)
                        showRescheduleDialog = false
                    }
                ) {
                    Text("Confirm Reschedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmCancel) {
        AlertDialog(
            onDismissRequest = { showConfirmCancel = false },
            title = { Text("Cancel Booking?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel this booking? This action is permanent and may affect your guide rating.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelBooking(bookingId)
                        showConfirmCancel = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Cancel Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCancel = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    if (showConfirmReject) {
        AlertDialog(
            onDismissRequest = { showConfirmReject = false },
            title = { Text("Decline Booking Request?", fontWeight = FontWeight.Bold) },
            text = { Text("Declining this request will notify the traveler immediately.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectBooking(bookingId)
                        showConfirmReject = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Decline Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReject = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Traveller Profile card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = booking.travellerName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(booking.travellerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(booking.travellerContact, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    FilledIconButton(
                        onClick = onNavigateToChat,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Message Traveller", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            // Booking Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tour Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    BookingDetailRow("Booking ID", booking.id)
                    BookingDetailRow("Destination", booking.destination)
                    BookingDetailRow("Selected Package", booking.tourPackage)
                    BookingDetailRow("Scheduled Date", booking.date)
                    BookingDetailRow("Meeting Time", booking.meetingTime)
                    BookingDetailRow("Meeting Point", booking.meetingLocation)
                    BookingDetailRow("Group size", "${booking.groupSize} People")
                    BookingDetailRow("Payment", booking.paymentStatus)
                    BookingDetailRow("Status", booking.status)
                }
            }

            // Special requests & Notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Traveller Requests & Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text("Notes:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(booking.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(4.dp))
                    Text("Special Requests:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(booking.specialRequests, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Emergency Contact:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(emergencyContactText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { showEditEmergencyContactDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Emergency Contact", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Action triggers based on booking status
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (booking.status == "Pending") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showConfirmReject = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reject")
                        }
                        Button(
                            onClick = { viewModel.acceptBooking(bookingId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Accept Booking")
                        }
                    }
                } else if (booking.status == "Confirmed") {
                    Button(
                        onClick = { viewModel.completeBooking(bookingId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Mark as Completed", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showRescheduleDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reschedule")
                        }
                        OutlinedButton(
                            onClick = { showConfirmCancel = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Booking")
                        }
                    }
                } else if (booking.status == "Completed") {
                    Button(
                        onClick = { showWriteReviewDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB000))
                        Spacer(Modifier.width(8.dp))
                        Text("⭐ Write Traveller Review for Guide", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun BookingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
