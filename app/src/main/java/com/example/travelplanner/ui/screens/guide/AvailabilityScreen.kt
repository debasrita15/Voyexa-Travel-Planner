package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.data.repo.AvailabilityDay
import com.example.travelplanner.ui.viewmodel.guide.AvailabilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    onBack: () -> Unit,
    viewModel: AvailabilityViewModel = viewModel()
) {
    val currentAvailability by viewModel.availability.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var availabilityList by remember { mutableStateOf<List<AvailabilityDay>>(emptyList()) }

    LaunchedEffect(currentAvailability) {
        if (currentAvailability.isNotEmpty() && availabilityList.isEmpty()) {
            availabilityList = currentAvailability
        }
    }

    var selectedDayForEdit by remember { mutableStateOf<AvailabilityDay?>(null) }
    var editStatus by remember { mutableStateOf("Available") }
    var editStart by remember { mutableStateOf("09:00") }
    var editEnd by remember { mutableStateOf("17:00") }
    var editRepeat by remember { mutableStateOf(true) }

    if (selectedDayForEdit != null) {
        AlertDialog(
            onDismissRequest = { selectedDayForEdit = null },
            title = { Text("Edit Hours - ${selectedDayForEdit!!.date}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select status for this date:")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Available", "Unavailable", "Vacation").forEach { status ->
                            val isSelected = editStatus == status
                            val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = color,
                                onClick = { editStatus = status },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(status, style = MaterialTheme.typography.labelSmall, color = textColor)
                                }
                            }
                        }
                    }

                    if (editStatus == "Available") {
                        OutlinedTextField(
                            value = editStart,
                            onValueChange = { editStart = it },
                            label = { Text("Start Time (e.g. 09:00)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = editEnd,
                            onValueChange = { editEnd = it },
                            label = { Text("End Time (e.g. 17:00)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = editRepeat, onCheckedChange = { editRepeat = it })
                            Text("Repeat weekly on this weekday")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val day = selectedDayForEdit!!
                        val updated = availabilityList.map {
                            if (it.date == day.date) {
                                it.copy(
                                    status = editStatus,
                                    startHour = editStart,
                                    endHour = editEnd,
                                    repeatWeekly = editRepeat
                                )
                            } else it
                        }
                        availabilityList = updated
                        selectedDayForEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDayForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Availability", fontWeight = FontWeight.Bold) },
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
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        viewModel.saveAvailability(availabilityList) {
                            // Saved snackbar action could go here
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Availability Settings", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanatory Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Set your weekly working hours and mark vacations to prevent unwanted client bookings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Availability list headers
            item {
                Text(
                    text = "Weekly Schedule Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Custom schedule days
            items(availabilityList) { day ->
                Card(
                    onClick = {
                        selectedDayForEdit = day
                        editStatus = day.status
                        editStart = day.startHour
                        editEnd = day.endHour
                        editRepeat = day.repeatWeekly
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(day.date, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            if (day.status == "Available") {
                                Text(
                                    text = "Hours: ${day.startHour} - ${day.endHour}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = day.status,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (day.status == "Vacation") Color(0xFFFFB000) else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}
