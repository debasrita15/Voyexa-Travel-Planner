package com.example.travelplanner.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import com.example.travelplanner.data.prefs.AppSettings
import com.example.travelplanner.domain.TripType
import com.example.travelplanner.util.Dates
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateTripViewModel : ViewModel() {

    val settingsFlow = AppGraph.settingsRepository.observeSettings()
        .stateIn(AppGraph.appScope, SharingStarted.WhileSubscribed(5000), AppSettings(
            year = Calendar.getInstance().get(Calendar.YEAR),
            yearlyBudget = 200000.0,
            reservedBufferPercent = 15.0,
            homeCity = "",
            homeCountry = java.util.Locale.getDefault().displayCountry,
            currency = try { java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode } catch (e: Exception) { "USD" },
            adultsCount = 2,
            kidAgeYears = 2,
            kidNapRequired = true,
            kidFriendlyPreference = 80,
            travelPace = com.example.travelplanner.domain.TravelPace.MEDIUM,
            llmProvider = com.example.travelplanner.domain.LLMProviderType.LOCAL,
            openAIModel = "gpt-4o-mini",
            groqModel = "llama-3.3-70b-versatile",
            geminiModel = "gemini-2.0-flash-exp",
            anthropicModel = "claude-3-5-haiku-20241022",
            mcpProviders = listOf("GROQ", "GEMINI")
        ))

    suspend fun createTrip(
        title: String,
        destination: String,
        startMs: Long,
        endMs: Long,
        type: TripType,
        budgetCap: Double
    ): String {
        val repo = AppGraph.tripRepository
        val settings = settingsFlow.value

        val tripId = repo.createTrip(title, destination, startMs, endMs, type, budgetCap)

        // Budget breakdown
        val allocation = AppGraph.budgetService.allocateTripBudget(budgetCap, type, settings)
        val budgetEntities = allocation.map { (cat, planned) ->
            com.example.travelplanner.data.db.TripBudgetItemEntity(
                id = java.util.UUID.randomUUID().toString(),
                tripId = tripId,
                category = cat,
                planned = planned,
                spent = 0.0
            )
        }
        repo.replaceBudget(tripId, budgetEntities)

        // Packing checklist
        val packing = AppGraph.packingService.generateKidChecklist(tripId, settings)
        repo.replacePacking(tripId, packing)

        return tripId
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit
) {
    val vm: CreateTripViewModel = viewModel()
    val settings by vm.settingsFlow.collectAsState()

    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("Family Trip") }
    var destination by remember { mutableStateOf("Pondicherry") }
    var budget by remember { mutableStateOf(60000.0) }
    var type by remember { mutableStateOf(TripType.DOMESTIC) }

    val now = System.currentTimeMillis()
    var startMs by remember { mutableStateOf(Dates.startOfDay(now)) }
    var endMs by remember { mutableStateOf(Dates.addDays(startMs, 2)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startMs
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endMs
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let { selectedMs ->
                            startMs = Dates.startOfDay(selectedMs)
                            if (endMs < startMs) {
                                endMs = Dates.addDays(startMs, 2)
                            }
                        }
                        showStartDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { selectedMs ->
                            val newEndMs = Dates.startOfDay(selectedMs)
                            if (newEndMs >= startMs) {
                                endMs = newEndMs
                            }
                        }
                        showEndDatePicker = false
                    },colors = ButtonDefaults.textButtonColors(
                        contentColor =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Create Trip",
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

                    containerColor = MaterialTheme.colorScheme.background,

                    titleContentColor = MaterialTheme.colorScheme.onBackground,

                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground

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
            OutlinedTextField(

                modifier = Modifier.fillMaxWidth(),

                value = title,

                onValueChange = { title = it },

                label = {
                    Text("Title")
                },

                shape = RoundedCornerShape(18.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedBorderColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary,

                    focusedLabelColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary,

                    cursorColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary

                )

            )

            OutlinedTextField(

                modifier = Modifier.fillMaxWidth(),

                value = destination,

                onValueChange = { destination = it },

                label = {
                    Text("Destination")
                },

                shape = RoundedCornerShape(18.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedBorderColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary,

                    focusedLabelColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary,
                    cursorColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary

                )

            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(

                    modifier = Modifier.weight(1f),

                    value = budget.toString(),

                    onValueChange = {
                        budget = it.toDoubleOrNull() ?: budget
                    },

                    label = {
                        Text("Budget Cap")
                    },

                    shape = RoundedCornerShape(18.dp),

                    colors = OutlinedTextFieldDefaults.colors(

                        focusedBorderColor =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary,

                        focusedLabelColor =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary,
                        cursorColor =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary,

                    )

                )
                DropdownTripType(type = type, onChange = { type = it })
            }

            Spacer(Modifier.height(8.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showStartDatePicker = true },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Start Date",
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Dates.formatDate(startMs),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showEndDatePicker = true },
                colors = CardDefaults.outlinedCardColors(

                    containerColor = MaterialTheme.colorScheme.surface

                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "End Date",
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        Dates.formatDate(endMs),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(18.dp),

                colors = ButtonDefaults.buttonColors(

                    containerColor = MaterialTheme.colorScheme.tertiary,

                    contentColor = MaterialTheme.colorScheme.onTertiary

                ),

                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                ),

                onClick = {

                    scope.launch {

                        val id = vm.createTrip(
                            title,
                            destination,
                            startMs,
                            endMs,
                            type,
                            budget
                        )

                        onCreated(id)

                    }

                }

            ) {

                Text(

                    "Create Trip",

                    style = MaterialTheme.typography.titleMedium,

                    fontWeight = FontWeight.SemiBold

                )

            }
        }
    }
}

@Composable
private fun DropdownTripType(
    type: TripType,
    onChange: (TripType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {

        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            border = null
        ) {

            Text(
                text = type.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {

            TripType.entries.forEach { tripType ->

                DropdownMenuItem(

                    text = {

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {

                            Text(
                                text = tripType.name,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                        }

                    },

                    onClick = {
                        expanded = false
                        onChange(tripType)
                    }

                )

            }

        }

    }
}