package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.data.repo.ActivityStop
import com.example.travelplanner.ui.viewmodel.guide.ScheduleEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorScreen(
    onBack: () -> Unit,
    viewModel: ScheduleEditorViewModel = viewModel()
) {
    val scheduleList by viewModel.schedule.collectAsState()
    val tour = scheduleList.firstOrNull { it.destination == "Goa Heritage Walk" }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activitiesState by remember { mutableStateOf<List<ActivityStop>>(emptyList()) }
    var meetingPointState by remember { mutableStateOf("") }
    var timeState by remember { mutableStateOf("") }

    LaunchedEffect(tour) {
        if (tour != null && activitiesState.isEmpty()) {
            activitiesState = tour.activities
            meetingPointState = tour.meetingPoint
            timeState = tour.time
        }
    }

    var selectedStopForEdit by remember { mutableStateOf<ActivityStop?>(null) }
    var editTime by remember { mutableStateOf("") }
    var editTitle by remember { mutableStateOf("") }
    var editDuration by remember { mutableStateOf("") }
    var editNotes by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }

    if (tour == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active schedule found.")
        }
        return
    }

    if (selectedStopForEdit != null) {
        AlertDialog(
            onDismissRequest = { selectedStopForEdit = null },
            title = { Text("Edit Timeline Stop", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTime,
                        onValueChange = { editTime = it },
                        label = { Text("Time (e.g. 10:30 AM)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Stop Title / Description") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("Duration (e.g. 45 mins)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes for travellers") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activitiesState = activitiesState.map {
                            if (it.id == selectedStopForEdit!!.id) {
                                it.copy(time = editTime, title = editTitle, duration = editDuration, notes = editNotes)
                            } else it
                        }
                        selectedStopForEdit = null
                    }
                ) {
                    Text("Save Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStopForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Activity Stop", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTime,
                        onValueChange = { editTime = it },
                        label = { Text("Time (e.g. 12:30 PM)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Stop Title") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("Duration (e.g. 30 mins)") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes for travellers") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newStop = ActivityStop(
                            id = "stop_${activitiesState.size + 1}",
                            time = editTime,
                            title = editTitle,
                            duration = editDuration,
                            notes = editNotes
                        )
                        activitiesState = activitiesState + newStop
                        showAddDialog = false
                    }
                ) {
                    Text("Add stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editTime = ""
                        editTitle = ""
                        editDuration = ""
                        editNotes = ""
                        showAddDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Stop")
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
                        viewModel.saveScheduleTimeline(
                            tourId = tour.id,
                            activities = activitiesState,
                            meetingPoint = meetingPointState,
                            time = timeState
                        ) {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Meeting point settings card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Global Tour Logistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = meetingPointState,
                        onValueChange = { meetingPointState = it },
                        label = { Text("Starting Meeting Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = timeState,
                        onValueChange = { timeState = it },
                        label = { Text("Starting Time") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Text(
                text = "Itinerary Timeline Stops",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Timeline stops list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(activitiesState) { stop ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Simple Dot & Line Indicator
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            ) {}

                            Spacer(Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stop.time, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(stop.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(stop.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                if (stop.notes.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(stop.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Row {
                                IconButton(
                                    onClick = {
                                        selectedStopForEdit = stop
                                        editTime = stop.time
                                        editTitle = stop.title
                                        editDuration = stop.duration
                                        editNotes = stop.notes
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit stop", modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = {
                                        activitiesState = activitiesState.filter { it.id != stop.id }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete stop", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
