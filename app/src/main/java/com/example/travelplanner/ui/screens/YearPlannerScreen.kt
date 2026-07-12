package com.example.travelplanner.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Year Planner",
                        maxLines = 1
                    )
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary,

                    titleContentColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.onTertiary
                        else
                            MaterialTheme.colorScheme.onPrimary,

                    navigationIconContentColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.onTertiary
                        else
                            MaterialTheme.colorScheme.onPrimary

                )

            )

        }

    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(

                    modifier = Modifier.width(150.dp),

                    value = selectedYear.toString(),

                    onValueChange = {
                        selectedYear = it.toIntOrNull() ?: selectedYear
                    },

                    label = {
                        Text("Year")
                    },

                    singleLine = true,

                    shape = RoundedCornerShape(18.dp),

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedBorderColor = MaterialTheme.colorScheme.primary,

                        focusedLabelColor = MaterialTheme.colorScheme.primary,

                        cursorColor = MaterialTheme.colorScheme.primary

                    )

                )
                Text(
                    "Trips: ${filtered.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Text(
                    "No trips for $selectedYear",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    groupedByMonth.forEach { (month, monthTrips) ->
                        item {
                            Text(
                                Dates.monthName(month),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                        items(monthTrips.sortedBy { it.startDateEpochMs }) { t ->
                            Card(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),

                                shape = RoundedCornerShape(24.dp),

                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),

                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )

                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        t.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${t.destination} • ${Dates.formatDate(t.startDateEpochMs)} to ${Dates.formatDate(t.endDateEpochMs)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = t.type.name,
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                ),
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color =
                                                if (isSystemInDarkTheme())
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                else
                                                    MaterialTheme.colorScheme.primaryContainer
                                        ) {

                                            Text(
                                                text = t.status.name,
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                ),
                                                color =
                                                    if (isSystemInDarkTheme())
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                    else
                                                        MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
