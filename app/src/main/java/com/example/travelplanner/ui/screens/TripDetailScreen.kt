package com.example.travelplanner.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.db.ActivityItemEntity
import com.example.travelplanner.data.db.PackingItemEntity
import com.example.travelplanner.data.db.TripBudgetItemEntity
import com.example.travelplanner.data.db.TripPlanEntity
import com.example.travelplanner.domain.LLMActivitySuggestion
import com.example.travelplanner.domain.LLMProviderType
import com.example.travelplanner.domain.TripStatus
import com.example.travelplanner.llm.PromptTemplates
import com.example.travelplanner.util.Dates
import com.example.travelplanner.util.Money
import com.example.travelplanner.workers.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration


data class TripDetailUiState(
    val trip: TripPlanEntity? = null,
    val days: List<com.example.travelplanner.data.db.ItineraryDayEntity> = emptyList(),
    val budget: List<TripBudgetItemEntity> = emptyList(),
    val packing: List<PackingItemEntity> = emptyList(),
    val transport: com.example.travelplanner.domain.TransportRecommendation? = null,
    val activitiesByDay: Map<String, List<ActivityItemEntity>> = emptyMap(),
    val loading: Boolean = false,
    val message: String? = null
)

class TripDetailViewModel(private val tripId: String) : ViewModel() {

    private val repo = AppGraph.tripRepository
    private val settingsRepo = AppGraph.settingsRepository

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(TripDetailUiState(loading = true))
    val state: kotlinx.coroutines.flow.StateFlow<TripDetailUiState> = _state

    init {
        observeTrip()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTrip() {
        viewModelScope.launch {
            repo.observeTrip(tripId).flatMapLatest { trip ->
                if (trip == null) {
                    flowOf(TripDetailUiState(message = "Trip not found", loading = false))
                } else {
                    combine(
                        repo.observeDays(tripId),
                        repo.observeBudget(tripId),
                        repo.observePacking(tripId)
                    ) { days, budget, packing ->
                        Triple(days, budget, packing)
                    }.flatMapLatest { (days, budget, packing) ->
                        if (days.isEmpty()) {
                            flowOf(TripDetailUiState(
                                trip = trip,
                                days = days,
                                budget = budget,
                                packing = packing,
                                transport = _state.value.transport,
                                activitiesByDay = emptyMap(),
                                loading = false
                            ))
                        } else {
                            val activityFlows = days.map { day ->
                                repo.observeItemsForDay(day.id).map { items -> day.id to items }
                            }

                            combine(activityFlows) { activitiesArray ->
                                val activitiesByDay = activitiesArray.toList().toMap()
                                TripDetailUiState(
                                    trip = trip,
                                    days = days,
                                    budget = budget,
                                    packing = packing,
                                    transport = _state.value.transport,
                                    activitiesByDay = activitiesByDay,
                                    loading = false
                                )
                            }
                        }
                    }
                }
            }.collect { ui ->
                _state.value = ui
            }
        }
    }

    fun refreshTransport() {
        val trip = _state.value.trip ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepo.observeSettings().first()
            val rec = AppGraph.transportAdvisor.recommend(settings.homeCity, trip.destination)
            _state.update { currentState ->
                currentState.copy(transport = rec)
            }
        }
    }

    fun generateItinerary() {
        val trip = _state.value.trip ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(loading = true, message = null) }

                val settings = settingsRepo.observeSettings().first()
                val (days, items) = AppGraph.itineraryService.generate(
                    tripId = trip.id,
                    destination = trip.destination,
                    startDateEpochMs = trip.startDateEpochMs,
                    endDateEpochMs = trip.endDateEpochMs,
                    type = trip.type,
                    settings = settings
                )

                repo.replaceItinerary(trip.id, days, items)
                repo.upsertTrip(
                    trip.copy(
                        status = TripStatus.PLANNED,
                        updatedAtEpochMs = System.currentTimeMillis()
                    )
                )

                _state.update { it.copy(loading = false, message = "Itinerary generated") }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, message = e.message ?: "Failed to generate itinerary") }
            }
        }
    }

    fun togglePacking(item: PackingItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updatePacking(item.copy(isChecked = !item.isChecked))
        }
    }

    fun updateBudgetPlanned(item: TripBudgetItemEntity, planned: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateBudget(item.copy(planned = planned))
        }
    }

    fun scheduleLeaveReminder(context: Context) {
        val trip = _state.value.trip ?: return

        val trigger = Dates.addDays(Dates.startOfDay(trip.startDateEpochMs), -14) + (9 * 60 * 60 * 1000L)

        WorkScheduler.scheduleLeaveReminder(
            context = context.applicationContext,
            tripId = trip.id,
            tripTitle = trip.title,
            destination = trip.destination,
            dateRange = "${Dates.formatDate(trip.startDateEpochMs)} to ${Dates.formatDate(trip.endDateEpochMs)}",
            triggerAtEpochMs = trigger
        )

        _state.update { it.copy(message = "Reminder scheduled") }
    }

    fun exportTripJson(context: Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = _state.value.trip ?: return@launch
                val payload = repo.exportTripPayload(trip)

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    AppGraph.jsonShareService.writeTripPayload(payload, os)
                }

                _state.update { it.copy(message = "JSON exported") }
            } catch (e: Exception) {
                _state.update { it.copy(message = e.message ?: "JSON export failed") }
            }
        }
    }

    fun exportTripPdf(context: Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = _state.value.trip ?: return@launch
                val days = _state.value.days

                val itemsByDay = mutableMapOf<String, List<ActivityItemEntity>>()
                for (d in days) {
                    itemsByDay[d.id] = repo.observeItemsForDay(d.id).first()
                }

                context.contentResolver.openOutputStream(uri)?.use { os ->
                    AppGraph.pdfExportService.exportTripPdf(trip, days, itemsByDay, os)
                }

                _state.update { it.copy(message = "PDF exported") }
            } catch (e: Exception) {
                _state.update { it.copy(message = e.message ?: "PDF export failed") }
            }
        }
    }

    fun addLLMEnrichment() {
        viewModelScope.launch(Dispatchers.IO) {
            val trip = _state.value.trip ?: return@launch
            val days = _state.value.days

            if (days.isEmpty()) {
                _state.update { it.copy(message = "Generate itinerary first") }
                return@launch
            }

            try {
                _state.update { it.copy(loading = true, message = "Enriching with AI...") }

                val settings = settingsRepo.observeSettings().first()

                when (settings.llmProvider) {
                    LLMProviderType.OPENAI -> {
                        val key = settingsRepo.loadOpenAIKey()?.trim().orEmpty()
                        if (key.isEmpty()) {
                            _state.update { it.copy(loading = false, message = "OpenAI key missing. Add it from Settings") }
                            return@launch
                        }
                    }
                    LLMProviderType.GROQ -> {
                        val key = settingsRepo.loadGroqKey()?.trim().orEmpty()
                        if (key.isEmpty()) {
                            _state.update { it.copy(loading = false, message = "Groq key missing. Add it from Settings") }
                            return@launch
                        }
                    }
                    LLMProviderType.GEMINI -> {
                        val key = settingsRepo.loadGeminiKey()?.trim().orEmpty()
                        if (key.isEmpty()) {
                            _state.update { it.copy(loading = false, message = "Gemini key missing. Add it from Settings") }
                            return@launch
                        }
                    }
                    LLMProviderType.ANTHROPIC -> {
                        val key = settingsRepo.loadAnthropicKey()?.trim().orEmpty()
                        if (key.isEmpty()) {
                            _state.update { it.copy(loading = false, message = "Anthropic key missing. Add it from Settings") }
                            return@launch
                        }
                    }
                    LLMProviderType.MCP -> {
                        val missingKeys = mutableListOf<String>()
                        settings.mcpProviders.forEach { provider ->
                            val hasKey = when (provider) {
                                "OPENAI" -> settingsRepo.loadOpenAIKey()?.isNotEmpty() == true
                                "GROQ" -> settingsRepo.loadGroqKey()?.isNotEmpty() == true
                                "GEMINI" -> settingsRepo.loadGeminiKey()?.isNotEmpty() == true
                                "ANTHROPIC" -> settingsRepo.loadAnthropicKey()?.isNotEmpty() == true
                                else -> false
                            }
                            if (!hasKey) missingKeys.add(provider)
                        }
                        if (missingKeys.isNotEmpty()) {
                            _state.update { it.copy(loading = false, message = "MCP missing keys: ${missingKeys.joinToString(", ")}") }
                            return@launch
                        }
                    }
                    LLMProviderType.LOCAL -> {}
                }

                val itemsByDay = mutableMapOf<String, List<ActivityItemEntity>>()
                for (d in days) {
                    itemsByDay[d.id] = repo.observeItemsForDay(d.id).first()
                }

                val prompt = PromptTemplates.enrichPrompt(trip, days, itemsByDay, settings)

                val suggestions: List<LLMActivitySuggestion> = when (settings.llmProvider) {
                    LLMProviderType.LOCAL -> {
                        listOf(
                            LLMActivitySuggestion(null, "Morning", "Kid-friendly park visit", "Low commute, start early. Great for children with plenty of safe play areas."),
                            LLMActivitySuggestion(null, "Afternoon", "Interactive museum", "Educational and fun. Many hands-on exhibits suitable for young kids."),
                            LLMActivitySuggestion(null, "Evening", "Early dinner at family restaurant", "Kid-friendly menu, quick service. Return to accommodation early for rest.")
                        )
                    }
                    LLMProviderType.OPENAI -> {
                        AppGraph.openAIClient.enrichItinerary(settings.openAIModel, prompt)
                    }
                    LLMProviderType.GROQ -> {
                        AppGraph.groqClient.enrichItinerary(settings.groqModel, prompt)
                    }
                    LLMProviderType.GEMINI -> {
                        AppGraph.geminiClient.enrichItinerary(settings.geminiModel, prompt)
                    }
                    LLMProviderType.ANTHROPIC -> {
                        AppGraph.anthropicClient.enrichItinerary(settings.anthropicModel, prompt)
                    }
                    LLMProviderType.MCP -> {
                        val models = mapOf(
                            "OPENAI" to settings.openAIModel,
                            "GROQ" to settings.groqModel,
                            "GEMINI" to settings.geminiModel,
                            "ANTHROPIC" to settings.anthropicModel
                        )
                        AppGraph.mcpAggregatorClient.enrichItinerary(
                            providers = settings.mcpProviders,
                            models = models,
                            prompt = prompt
                        )
                    }
                }

                android.util.Log.d("TripDetail", "LLM returned ${suggestions.size} suggestions")
                suggestions.forEachIndexed { idx, s ->
                    android.util.Log.d("TripDetail", "  [$idx] date=${s.dayDateEpochMs}, slot=${s.timeSlot}, title=${s.title}")
                }

                var addedCount = 0

                val suggestionsWithDates = suggestions.filter { it.dayDateEpochMs != null }
                val suggestionsWithoutDates = suggestions.filter { it.dayDateEpochMs == null }

                android.util.Log.d("TripDetail", "Split: ${suggestionsWithDates.size} with dates, ${suggestionsWithoutDates.size} without dates")
                android.util.Log.d("TripDetail", "Available days: ${days.size}")

                for (s in suggestionsWithDates) {
                    val day = days.firstOrNull {
                        Dates.startOfDay(it.dateEpochMs) == Dates.startOfDay(s.dayDateEpochMs!!)
                    }

                    if (day == null) {
                        android.util.Log.w("TripDetail", "No matching day found for suggestion with date ${s.dayDateEpochMs}")
                        continue
                    }

                    val current = repo.observeItemsForDay(day.id).first()
                    val nextIndex = (current.maxOfOrNull { it.sortIndex } ?: 0) + 1

                    val newItem = ActivityItemEntity(
                        id = UUID.randomUUID().toString(),
                        itineraryDayId = day.id,
                        title = s.title,
                        category = "llm",
                        timeSlot = s.timeSlot,
                        estimatedCost = 0.0,
                        commuteMinutes = 20,
                        notes = s.notes,
                        sortIndex = nextIndex
                    )

                    repo.upsertItems(listOf(newItem))
                    addedCount++
                    android.util.Log.d("TripDetail", "Added dated suggestion: ${s.title} to day ${day.id}")
                }

                if (suggestionsWithoutDates.isNotEmpty() && days.isNotEmpty()) {
                    var dayIndex = 0
                    for (s in suggestionsWithoutDates) {
                        val day = days[dayIndex % days.size]

                        val current = repo.observeItemsForDay(day.id).first()
                        val nextIndex = (current.maxOfOrNull { it.sortIndex } ?: 0) + 1

                        val newItem = ActivityItemEntity(
                            id = UUID.randomUUID().toString(),
                            itineraryDayId = day.id,
                            title = s.title,
                            category = "llm",
                            timeSlot = s.timeSlot,
                            estimatedCost = 0.0,
                            commuteMinutes = 20,
                            notes = s.notes,
                            sortIndex = nextIndex
                        )

                        repo.upsertItems(listOf(newItem))
                        addedCount++
                        dayIndex++
                        android.util.Log.d("TripDetail", "Added undated suggestion: ${s.title} to day ${day.id} (index $dayIndex)")
                    }
                } else if (suggestionsWithoutDates.isNotEmpty()) {
                    android.util.Log.w("TripDetail", "Have ${suggestionsWithoutDates.size} suggestions but no days to add them to!")
                }

                android.util.Log.d("TripDetail", "Total added: $addedCount suggestions")

                _state.update { it.copy(loading = false, message = "Added $addedCount AI suggestions ✨") }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(loading = false, message = e.message ?: "LLM enrichment failed") }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

@Suppress("UNCHECKED_CAST")
private class TripDetailVMFactory(private val tripId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TripDetailViewModel(tripId) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    onBack: () -> Unit,
    onEditDay: (String) -> Unit
) {

    val ctx = LocalContext.current

    val vm: TripDetailViewModel =
        viewModel(factory = TripDetailVMFactory(tripId))

    val state by vm.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val exportJsonLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            uri?.let {
                vm.exportTripJson(ctx, it)
            }
        }

    val exportPdfLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/pdf")
        ) { uri ->
            uri?.let {
                vm.exportTripPdf(ctx, it)
            }
        }

    LaunchedEffect(Unit) {
        vm.refreshTransport()
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(

        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },

        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = state.trip?.title ?: "Trip Details",
                        maxLines = 1
                    )

                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                },

                actions = {

                    Text(
                        text = "Generate",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(enabled = !state.loading) {
                                vm.generateItinerary()
                            },
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "AI",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(enabled = !state.loading) {
                                vm.addLLMEnrichment()
                            },
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = {
                            exportPdfLauncher.launch("${state.trip?.title ?: "trip"}.pdf")
                        }
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF"
                        )
                    }

                    IconButton(
                        onClick = {
                            exportJsonLauncher.launch("${state.trip?.title ?: "trip"}.json")
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export JSON"
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
                            MaterialTheme.colorScheme.onPrimary,

                    actionIconContentColor =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.onTertiary
                        else
                            MaterialTheme.colorScheme.onPrimary
                )

            )

        }

    ) { pad ->

        val trip = state.trip

        if (trip == null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {

                Text("Trip not found")

            }

            return@Scaffold
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            LazyColumn(

                modifier = Modifier
                    .padding(pad),

                contentPadding = PaddingValues(20.dp),

                verticalArrangement = Arrangement.spacedBy(18.dp)

            ) {

                item {

                    Card(

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(28.dp),

                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surfaceContainer
                        ),

                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        )

                    ) {

                        Column(

                            modifier = Modifier.padding(24.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(16.dp)

                        ) {

                            Text(
                                text = trip.destination,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                trip.type.name.replace("_", " "),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider()

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                Surface(

                                    shape = RoundedCornerShape(50),

                                    color =
                                        if (isSystemInDarkTheme())
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        else
                                            MaterialTheme.colorScheme.primaryContainer

                                ) {

                                    Text(
                                        Dates.formatDate(
                                            trip.startDateEpochMs
                                        ),
                                        modifier = Modifier.padding(
                                            horizontal = 18.dp,
                                            vertical = 8.dp
                                        ),
                                        color =
                                            if (isSystemInDarkTheme())
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            else
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                }

                                Surface(

                                    shape = RoundedCornerShape(50),

                                    color =
                                        if (isSystemInDarkTheme())
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        else
                                            MaterialTheme.colorScheme.primaryContainer

                                ) {

                                    Text(
                                        Dates.formatDate(
                                            trip.endDateEpochMs
                                        ),
                                        modifier = Modifier.padding(
                                            horizontal = 18.dp,
                                            vertical = 8.dp
                                        ),
                                        color =
                                            if (isSystemInDarkTheme())
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            else
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                }

                            }

                            Text(
                                text = "Budget",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = Money.format(trip.budgetCap),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(

                                shape = RoundedCornerShape(50),

                                color =
                                    if (isSystemInDarkTheme())
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.primaryContainer

                            ) {

                                Text(

                                    text = trip.status.name.replace("_", " "),

                                    modifier = Modifier.padding(
                                        horizontal = 18.dp,
                                        vertical = 10.dp
                                    ),

                                    color =
                                        if (isSystemInDarkTheme())
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else
                                            MaterialTheme.colorScheme.onPrimaryContainer,

                                    fontWeight = FontWeight.Bold

                                )

                            }

                        }

                    }

                }

                item {

                    ElevatedCard(

                        shape = RoundedCornerShape(24.dp),

                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )

                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        "Transport Advisor",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "Best travel recommendation",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                }

                                FilledTonalButton(
                                    enabled = !state.loading,
                                    onClick = { vm.refreshTransport() }
                                ) {
                                    Text("Refresh")
                                }

                            }

                            HorizontalDivider()

                            val rec = state.transport

                            if (rec == null) {

                                Text(
                                    "No recommendation available.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            } else {

                                Text(
                                    "From",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    rec.from,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    "To",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    rec.to,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                HorizontalDivider()

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {

                                        Text(
                                            "${rec.distanceKm} km",
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )

                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {

                                        Text(
                                            rec.recommendedMode,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            ),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )

                                    }

                                }

                                Text(
                                    rec.reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            }

                        }

                    }

                }

                item {

                    ElevatedCard(

                        shape = RoundedCornerShape(24.dp),

                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )

                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        "Leave Reminder",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "14 days before departure",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                }

                                FilledTonalButton(
                                    onClick = {
                                        vm.scheduleLeaveReminder(ctx)
                                    }
                                ) {
                                    Text("Schedule")
                                }

                            }

                        }

                    }

                }

                item {

                    ElevatedCard(

                        shape = RoundedCornerShape(24.dp),

                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )

                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Text(
                                "Budget Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (state.budget.isEmpty()) {

                                Text(
                                    "No budget added.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            } else {

                                state.budget
                                    .sortedBy { it.category.name }
                                    .forEach { b ->

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {

                                            Text(
                                                b.category.name.replace("_", " "),
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            OutlinedTextField(
                                                modifier = Modifier.fillMaxWidth(),
                                                value = b.planned.toString(),
                                                onValueChange = {
                                                    it.toDoubleOrNull()?.let { value ->
                                                        vm.updateBudgetPlanned(b, value)
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                singleLine = true
                                            )

                                        }

                                    }

                            }

                        }

                    }

                }

                item {

                    ElevatedCard(

                        shape = RoundedCornerShape(24.dp),

                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )

                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Text(
                                "Packing Checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (state.packing.isEmpty()) {

                                Text(
                                    "No packing items.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            } else {

                                state.packing
                                    .sortedBy { it.category }
                                    .forEach { item ->

                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {

                                            Row(

                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        horizontal = 16.dp,
                                                        vertical = 12.dp
                                                    ),

                                                horizontalArrangement = Arrangement.SpaceBetween,

                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically

                                            ) {

                                                Column {

                                                    Text(
                                                        item.title,
                                                        fontWeight = FontWeight.SemiBold
                                                    )

                                                    Text(
                                                        text = item.category.name.replace("_", " "),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )

                                                }

                                                Checkbox(
                                                    checked = item.isChecked,
                                                    onCheckedChange = {
                                                        vm.togglePacking(item)
                                                    }
                                                )

                                            }

                                        }

                                    }

                            }

                        }

                    }

                }

                item {

                    Text(
                        "Trip Itinerary",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                }

                items(
                    state.days.sortedBy { it.dateEpochMs }
                ) { day ->

                    ElevatedCard(

                        shape = RoundedCornerShape(24.dp),

                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )

                    ) {

                        Column(

                            modifier = Modifier.padding(20.dp),

                            verticalArrangement = Arrangement.spacedBy(14.dp)

                        ) {

                            Row(

                                modifier = Modifier.fillMaxWidth(),

                                horizontalArrangement = Arrangement.SpaceBetween,

                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically

                            ) {

                                Column {

                                    Text(
                                        Dates.formatFullDate(day.dateEpochMs),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        day.cityArea,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                }

                                FilledTonalButton(
                                    onClick = {
                                        onEditDay(day.id)
                                    }
                                ) {
                                    Text("Edit")
                                }

                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {

                                Text(
                                    "Kid Friendly Score : ${day.kidFriendlinessScore}/10",
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                            }

                            val activities =
                                state.activitiesByDay[day.id] ?: emptyList()

                            if (activities.isNotEmpty()) {

                                activities
                                    .sortedBy { it.sortIndex }
                                    .forEach { activity ->

                                        val isLLMEnriched = activity.category == "llm"

                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor =
                                                    if (isLLMEnriched)
                                                        MaterialTheme.colorScheme.tertiaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.surface
                                            )
                                        ) {

                                            Column(
                                                Modifier.padding(18.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {

                                                Row(
                                                    Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                                ) {

                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Text(
                                                            activity.timeSlot,
                                                            modifier = Modifier.padding(
                                                                horizontal = 12.dp,
                                                                vertical = 6.dp
                                                            ),
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }

                                                    if (isLLMEnriched) {

                                                        Surface(
                                                            shape = RoundedCornerShape(50),
                                                            color = MaterialTheme.colorScheme.tertiary
                                                        ) {

                                                            Text(
                                                                "AI Suggested",
                                                                modifier = Modifier.padding(
                                                                    horizontal = 12.dp,
                                                                    vertical = 6.dp
                                                                ),
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.onTertiary
                                                            )

                                                        }

                                                    }

                                                }

                                                Text(
                                                    activity.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                if (activity.notes.isNotBlank()) {

                                                    Text(
                                                        activity.notes,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )

                                                }

                                                HorizontalDivider()

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {

                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = MaterialTheme.colorScheme.secondaryContainer
                                                    ) {
                                                        Text(
                                                            "₹ ${activity.estimatedCost.toInt()}",
                                                            modifier = Modifier.padding(
                                                                horizontal = 12.dp,
                                                                vertical = 6.dp
                                                            ),
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = MaterialTheme.colorScheme.secondaryContainer
                                                    ) {
                                                        Text(
                                                            "${activity.commuteMinutes} min",
                                                            modifier = Modifier.padding(
                                                                horizontal = 12.dp,
                                                                vertical = 6.dp
                                                            ),
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }

                                                }

                                            }

                                        }

                                    }

                            } else {

                                Text(

                                    "No activities available.",

                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant

                                )

                            }
                        }

                    }

                }

            }

            if (state.loading) {

                    Box(

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pad)
                            .clickable(enabled = false) {},

                        contentAlignment = androidx.compose.ui.Alignment.Center

                    ) {

                        Surface(

                            modifier = Modifier.fillMaxSize(),

                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)

                        ) {}

                        Card(

                            shape = RoundedCornerShape(28.dp),

                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 12.dp
                            ),

                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )

                        ) {

                            Column(

                                modifier = Modifier.padding(
                                    horizontal = 36.dp,
                                    vertical = 32.dp
                                ),

                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,

                                verticalArrangement = Arrangement.spacedBy(18.dp)

                            ) {

                                CircularProgressIndicator()

                                Text(

                                    "Enriching itinerary with AI...",

                                    style = MaterialTheme.typography.titleLarge,

                                    fontWeight = FontWeight.Bold

                                )

                                Text(

                                    "This may take a few seconds",

                                    style = MaterialTheme.typography.bodyMedium,

                                    color = MaterialTheme.colorScheme.onSurfaceVariant

                                )

                            }

                        }

                    }

                }

            }

        }

    }

