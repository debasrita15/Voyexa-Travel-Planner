package com.example.travelplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.util.Dates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class YearPlannerViewModel : ViewModel() {
    val trips = AppGraph.tripRepository.observeTrips()
        .stateIn(AppGraph.appScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearPlannerScreen(onBack: () -> Unit) {
    val vm: YearPlannerViewModel = viewModel()
    val trips by vm.trips.collectAsState()

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    var selectedYear by remember { mutableStateOf(currentYear) }

    val filtered = trips.filter { Dates.yearOf(it.startDateEpochMs) == selectedYear }
    val groupedByMonth = filtered.groupBy { Dates.monthOf(it.startDateEpochMs) }.toSortedMap()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Year Planner", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(16.dp)) {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.width(140.dp),
                    value = selectedYear.toString(),
                    onValueChange = { selectedYear = it.toIntOrNull() ?: selectedYear },
                    label = { Text("Year") }
                )
                Text("Trips: ${filtered.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Text("No trips for $selectedYear", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {
                    groupedByMonth.forEach { (month, monthTrips) ->
                        item {
                            Text(
                                Dates.monthName(month),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(monthTrips.sortedBy { it.startDateEpochMs }) { t ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(t.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${t.destination} • ${Dates.formatDate(t.startDateEpochMs)} to ${Dates.formatDate(t.endDateEpochMs)}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("${t.type.name} • ${t.status.name}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
