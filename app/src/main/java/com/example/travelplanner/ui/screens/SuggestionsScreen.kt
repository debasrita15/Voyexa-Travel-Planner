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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.domain.TripSuggestion
import com.example.travelplanner.domain.usecase.BudgetInfo
import com.example.travelplanner.util.Dates
import com.example.travelplanner.util.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown

// UI State
sealed class SuggestionsUiState {
    data object Loading : SuggestionsUiState()
    data class Success(
        val suggestions: List<TripSuggestion>,
        val budgetInfo: BudgetInfo,
        val isOnlineMode: Boolean
    ) : SuggestionsUiState()

    data class Error(
        val message: String,
        val budgetInfo: BudgetInfo? = null
    ) : SuggestionsUiState()
}


// ViewModel
class SuggestionsViewModel : ViewModel() {

    private companion object {
        const val SETTINGS_SUBSCRIPTION_TIMEOUT_MS = 5000L
    }

    private val calculateBudgetUseCase = AppGraph.calculateBudgetUseCase
    private val getTripSuggestionsUseCase = AppGraph.getTripSuggestionsUseCase

    val settingsFlow = AppGraph.settingsRepository.observeSettings()
        .stateIn(AppGraph.appScope, SharingStarted.WhileSubscribed(SETTINGS_SUBSCRIPTION_TIMEOUT_MS), null)

    private val _uiState = MutableStateFlow<SuggestionsUiState>(SuggestionsUiState.Loading)
    val uiState: StateFlow<SuggestionsUiState> = _uiState

    fun loadSuggestions(month: Int, useOnlineApi: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value = SuggestionsUiState.Loading

                val settings = AppGraph.settingsRepository.observeSettings().first()

                val budgetResult = calculateBudgetUseCase.calculateRemaining(
                    year = settings.year,
                    yearlyBudget = settings.yearlyBudget
                )

                val budgetInfo = budgetResult.getOrElse {
                    throw IllegalStateException("Failed to calculate budget: ${it.message}")
                }

                val suggestionsResult = getTripSuggestionsUseCase(
                    homeCountry = settings.homeCountry,
                    month = month,
                    budget = budgetInfo.remaining,
                    currency = settings.currency,
                    useOnlineApi = useOnlineApi
                )

                val suggestions = suggestionsResult.getOrElse {
                    throw IllegalStateException("Failed to fetch suggestions: ${it.message}")
                }

                _uiState.value = SuggestionsUiState.Success(
                    suggestions = suggestions,
                    budgetInfo = budgetInfo,
                    isOnlineMode = useOnlineApi
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(error: Exception) {
        error.printStackTrace()
        val errorMessage = when {
            error.message?.contains("Settings not available") == true ->
                "Settings not loaded. Please configure app settings first."
            error.message?.contains("calculate budget") == true ->
                "Budget calculation failed. Check your yearly budget in settings."
            error.message?.contains("fetch suggestions") == true ->
                "Could not load suggestions. Check internet connection or try offline mode."
            else -> error.message ?: "Unknown error occurred"
        }

        _uiState.value = SuggestionsUiState.Error(message = errorMessage)
    }
}

// UI Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    onBack: () -> Unit,
    onCreateFromSuggestion: (String, String, Long, Long, com.example.travelplanner.domain.TripType, Double) -> Unit = { _, _, _, _, _, _ -> }
) {
    val viewModel: SuggestionsViewModel = viewModel()
    val settings by viewModel.settingsFlow.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedMonth by remember { mutableStateOf(Dates.monthOf(System.currentTimeMillis())) }
    var isOnlineMode by remember { mutableStateOf(true) }

    LaunchedEffect(selectedMonth, isOnlineMode) {
        viewModel.loadSuggestions(selectedMonth, isOnlineMode)
    }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Trip Suggestions",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(onClick = onBack) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null
                        )

                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor =
                        MaterialTheme.colorScheme.background,

                    titleContentColor =
                        MaterialTheme.colorScheme.onBackground,

                    navigationIconContentColor =
                        MaterialTheme.colorScheme.onBackground

                )

            )

        }

    ) { paddingValues ->

        when {

            settings == null ->

                LoadingScreen(paddingValues)

            else ->

                SuggestionsContent(

                    uiState = uiState,

                    selectedMonth = selectedMonth,

                    isOnlineMode = isOnlineMode,

                    onMonthChange = { selectedMonth = it },

                    onModeToggle = { isOnlineMode = it },

                    onCreateFromSuggestion = onCreateFromSuggestion,

                    paddingValues = paddingValues

                )

        }

    }
}

@Composable
private fun LoadingScreen(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuggestionsContent(
    uiState: SuggestionsUiState,
    selectedMonth: Int,
    isOnlineMode: Boolean,
    onMonthChange: (Int) -> Unit,
    onModeToggle: (Boolean) -> Unit,
    onCreateFromSuggestion: (String, String, Long, Long, com.example.travelplanner.domain.TripType, Double) -> Unit,
    paddingValues: PaddingValues
) {
    Column(

        modifier = Modifier

            .fillMaxSize()

            .padding(paddingValues)

            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(20.dp)

    ) {

        when (uiState) {

            is SuggestionsUiState.Loading ->

                LoadingContent()

            is SuggestionsUiState.Success ->

                SuccessContent(

                    state = uiState,

                    selectedMonth = selectedMonth,

                    isOnlineMode = isOnlineMode,

                    onMonthChange = onMonthChange,

                    onModeToggle = onModeToggle,

                    onCreateFromSuggestion = onCreateFromSuggestion

                )

            is SuggestionsUiState.Error ->

                ErrorContent(uiState)

        }

    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SuccessContent(
    state: SuggestionsUiState.Success,
    selectedMonth: Int,
    isOnlineMode: Boolean,
    onMonthChange: (Int) -> Unit,
    onModeToggle: (Boolean) -> Unit,
    onCreateFromSuggestion: (String, String, Long, Long, com.example.travelplanner.domain.TripType, Double) -> Unit
) {
    BudgetOverviewCard(
        budgetInfo = state.budgetInfo
    )

    FilterControls(
        selectedMonth = selectedMonth,
        isOnlineMode = isOnlineMode,
        onMonthChange = onMonthChange,
        onModeToggle = onModeToggle
    )

    SuggestionsList(
        suggestions = state.suggestions,
        selectedMonth = selectedMonth,
        onCreateFromSuggestion = onCreateFromSuggestion
    )
}

@Composable
private fun ErrorContent(
    error: SuggestionsUiState.Error
) {
    error.budgetInfo?.let { budgetInfo ->
        BudgetOverviewCard(budgetInfo)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun BudgetOverviewCard(
    budgetInfo: BudgetInfo
) {

    val dark = isSystemInDarkTheme()

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(30.dp),

        elevation = CardDefaults.cardElevation(8.dp),

        colors = CardDefaults.cardColors(

            containerColor =
                if (dark)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.primary

        )

    ) {

        Column(

            modifier = Modifier.padding(24.dp)

        ) {

            val textColor =
                if (dark)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onPrimary

            Text(

                "Budget Overview",

                color = textColor,

                style = MaterialTheme.typography.headlineSmall,

                fontWeight = FontWeight.Bold

            )

            Spacer(Modifier.height(20.dp))

            BudgetHeader(budgetInfo)

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(

                progress = { budgetInfo.spentPercentage },

                modifier = Modifier.fillMaxWidth(),

                color = MaterialTheme.colorScheme.tertiary,

                trackColor = textColor.copy(.15f)

            )

            Spacer(Modifier.height(16.dp))

            RemainingBudgetRow(budgetInfo)

        }

    }

}

@Composable
private fun TripTypeBadge(
    type: com.example.travelplanner.domain.TripType
) {

    val background =
        MaterialTheme.colorScheme.tertiaryContainer

    val textColor =
        if (type == com.example.travelplanner.domain.TripType.DOMESTIC)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onTertiaryContainer

    Surface(
        shape = RoundedCornerShape(50),
        color = background
    ) {

        Text(
            text = type.name,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )

    }

}
@Composable
private fun BudgetHeader(budgetInfo: BudgetInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BudgetItem(
            label = "Yearly Budget",
            amount = budgetInfo.yearly,
            style = MaterialTheme.typography.titleLarge
        )
        BudgetItem(
            label = "Spent",
            amount = budgetInfo.spent,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            alignment = Alignment.End
        )
    }
}

@Composable
private fun BudgetItem(
    label: String,
    amount: Double,
    style: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            Money.format(amount),
            style = style,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RemainingBudgetRow(budgetInfo: BudgetInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Remaining for trips:", style = MaterialTheme.typography.bodyMedium)
        Text(
            Money.format(budgetInfo.remaining),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun FilterControls(
    selectedMonth: Int,
    isOnlineMode: Boolean,
    onMonthChange: (Int) -> Unit,
    onModeToggle: (Boolean) -> Unit
) {
    Row(

        modifier = Modifier.fillMaxWidth(),

        horizontalArrangement = Arrangement.spacedBy(16.dp),

        verticalAlignment = Alignment.CenterVertically

    ) {

        MonthSelector(

            selectedMonth,

            onMonthChange,

            Modifier.weight(1f)

        )

        ApiModeToggle(

            isOnlineMode,

            onModeToggle

        )

    }
}

@Composable
private fun MonthSelector(
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val currentMonthName = monthNames.getOrElse(selectedMonth - 1) { "January" }

    OutlinedTextField(
        modifier = modifier,
        value = currentMonthName,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        label = {
            Text("Month")
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor =
                if (dark)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary,
            focusedLabelColor =
                if (dark)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary
        ),
        trailingIcon = {
            Column {
                IconButton(
                    onClick = {
                        if (selectedMonth < 12)
                            onMonthChange(selectedMonth + 1)
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase Month"
                    )
                }

                IconButton(
                    onClick = {
                        if (selectedMonth > 1)
                            onMonthChange(selectedMonth - 1)
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease Month"
                    )
                }
            }
        }
    )
}

@Composable
private fun BestMonthsText(bestMonths: List<Int>) {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthNamesStr = bestMonths.take(6).mapNotNull { m -> monthNames.getOrNull(m - 1) }.joinToString(", ")

    Text(
        "Best months: $monthNamesStr",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ApiModeToggle(
    isOnlineMode: Boolean,
    onModeToggle: (Boolean) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Switch(

            checked = isOnlineMode,

            onCheckedChange = onModeToggle,

            colors = SwitchDefaults.colors(

                checkedThumbColor = MaterialTheme.colorScheme.tertiary,

                checkedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.40f),

                checkedBorderColor = MaterialTheme.colorScheme.tertiary,

                uncheckedThumbColor = MaterialTheme.colorScheme.outline,

                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,

                uncheckedBorderColor = MaterialTheme.colorScheme.outline

            )

        )

        Spacer(Modifier.width(8.dp))

        Text(

            text = if (isOnlineMode) "Online" else "Offline",

            style = MaterialTheme.typography.bodyMedium,

            fontWeight = FontWeight.SemiBold,

            color = MaterialTheme.colorScheme.onSurface

        )

    }

}

@Composable
private fun SuggestionsList(
    suggestions: List<TripSuggestion>,
    selectedMonth: Int,
    onCreateFromSuggestion: (String, String, Long, Long, com.example.travelplanner.domain.TripType, Double) -> Unit
) {
    if (suggestions.isEmpty()) {
        EmptySuggestionsCard()
    } else {
        SuggestionsHeader(count = suggestions.size)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(suggestions) { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    selectedMonth = selectedMonth,
                    onCreateClick = onCreateFromSuggestion
                )
            }
        }
    }
}

@Composable
private fun EmptySuggestionsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "No suggestions found?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Try a different month or increase budget",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
    }
}

@Composable
private fun SuggestionsHeader(count: Int) {
    Text(
        "$count destinations found",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SuggestionCard(
    suggestion: TripSuggestion,
    selectedMonth: Int,
    onCreateClick: (String, String, Long, Long, com.example.travelplanner.domain.TripType, Double) -> Unit
) {
    val dark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Consistent rounded corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuggestionHeader(suggestion)
            BudgetRangeText(suggestion)

            if (suggestion.tags.isNotEmpty()) {
                TagsRow(suggestion.tags)
            }
            BestMonthsText(suggestion.bestMonths)

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(java.util.Calendar.YEAR, currentYear)
                    calendar.set(java.util.Calendar.MONTH, selectedMonth - 1)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val startMs = Dates.startOfDay(calendar.timeInMillis)
                    val endMs = Dates.addDays(startMs, 5)
                    onCreateClick( suggestion.destination, suggestion.destination, startMs, endMs, suggestion.type, suggestion.approxBudgetMin )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),

                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp
                )
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
private fun BudgetRangeText(suggestion: TripSuggestion) {
    val dark = isSystemInDarkTheme()
    Text(
        "Budget: ${Money.format(suggestion.approxBudgetMin)} - ${Money.format(suggestion.approxBudgetMax)}",
        style = MaterialTheme.typography.bodyMedium,
        // Use onSurfaceVariant in Dark Mode to avoid blue tint
        color = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun SuggestionHeader(suggestion: TripSuggestion) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            suggestion.destination,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        TripTypeBadge(suggestion.type)
    }
}


@Composable
private fun TagsRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.take(3).forEach { tag ->
            TagChip(tag)
        }
    }
}

@Composable
private fun TagChip(tag: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            tag,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
