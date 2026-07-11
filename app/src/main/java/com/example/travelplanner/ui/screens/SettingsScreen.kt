package com.example.travelplanner.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.domain.LLMProviderType
import com.example.travelplanner.domain.TravelPace
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SettingsViewModel : ViewModel() {
    val settingsFlow = AppGraph.settingsRepository.observeSettings()
        .stateIn(AppGraph.appScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveOpenAIKey(key: String) = AppGraph.settingsRepository.saveOpenAIKey(key)
    fun saveGroqKey(key: String) = AppGraph.settingsRepository.saveGroqKey(key)
    fun saveGeminiKey(key: String) = AppGraph.settingsRepository.saveGeminiKey(key)
    fun saveAnthropicKey(key: String) = AppGraph.settingsRepository.saveAnthropicKey(key)

    fun loadOpenAIKey(): String = AppGraph.settingsRepository.loadOpenAIKey().orEmpty()
    fun loadGroqKey(): String = AppGraph.settingsRepository.loadGroqKey().orEmpty()
    fun loadGeminiKey(): String = AppGraph.settingsRepository.loadGeminiKey().orEmpty()
    fun loadAnthropicKey(): String = AppGraph.settingsRepository.loadAnthropicKey().orEmpty()

    fun updateYearlyBudget(v: Double) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(yearlyBudget = v) }
        }
    }

    fun updateHomeCity(v: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(homeCity = v) }
        }
    }

    fun updateHomeCountry(v: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(homeCountry = v) }
        }
    }

    fun updateCurrency(v: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(currency = v) }
            com.example.travelplanner.util.Money.setUserCurrency(v)
        }
    }

    fun updateKidAge(v: Int) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(kidAgeYears = v) }
        }
    }

    fun updateLLMProvider(p: LLMProviderType) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(llmProvider = p) }
        }
    }

    fun updateOpenAIModel(m: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(openAIModel = m) }
        }
    }

    fun updateGroqModel(m: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(groqModel = m) }
        }
    }

    fun updateGeminiModel(m: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(geminiModel = m) }
        }
    }

    fun updateAnthropicModel(m: String) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(anthropicModel = m) }
        }
    }

    fun updateMCPProviders(providers: List<String>) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(mcpProviders = providers) }
        }
    }

    fun updatePace(p: TravelPace) {
        viewModelScope.launch {
            AppGraph.settingsRepository.updateSettings { it.copy(travelPace = p) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {

    val vm: SettingsViewModel = viewModel()
    val settings = vm.settingsFlow.collectAsState().value

    if (settings == null) {

        Scaffold(

            containerColor = MaterialTheme.colorScheme.background,

            topBar = {

                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
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

            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad),

                contentAlignment = Alignment.Center

            ) {

                CircularProgressIndicator(
                    color =
                        if (isSystemInDarkTheme())
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.primary
                )

            }

        }

        return
    }

    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
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
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp)

        ) {

            TravelPreferencesSection(

                settings = settings,

                onHomeCityChange = vm::updateHomeCity,

                onHomeCountryChange = vm::updateHomeCountry,

                onCurrencyChange = vm::updateCurrency,

                onBudgetChange = vm::updateYearlyBudget,

                onKidAgeChange = vm::updateKidAge,

                onPaceChange = vm::updatePace

            )

            LLMProviderSection(

                settings = settings,

                onProviderChange = vm::updateLLMProvider,

                onOpenAIModelChange = vm::updateOpenAIModel,

                onGroqModelChange = vm::updateGroqModel,

                onGeminiModelChange = vm::updateGeminiModel,

                onAnthropicModelChange = vm::updateAnthropicModel,

                onMCPProvidersChange = vm::updateMCPProviders,

                onSaveOpenAIKey = vm::saveOpenAIKey,

                onSaveGroqKey = vm::saveGroqKey,

                onSaveGeminiKey = vm::saveGeminiKey,

                onSaveAnthropicKey = vm::saveAnthropicKey,

                loadOpenAIKey = vm::loadOpenAIKey,

                loadGroqKey = vm::loadGroqKey,

                loadGeminiKey = vm::loadGeminiKey,

                loadAnthropicKey = vm::loadAnthropicKey

            )

            Spacer(
                Modifier.height(20.dp)
            )

        }

    }

}

@Composable
private fun TravelPreferencesSection(
    settings: com.example.travelplanner.data.prefs.AppSettings,
    onHomeCityChange: (String) -> Unit,
    onHomeCountryChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onBudgetChange: (Double) -> Unit,
    onKidAgeChange: (Int) -> Unit,
    onPaceChange: (TravelPace) -> Unit
) {

    val focusManager = LocalFocusManager.current
    val dark = isSystemInDarkTheme()

    var homeCityText by remember(settings.homeCity) {
        mutableStateOf(settings.homeCity)
    }

    var homeCountryText by remember(settings.homeCountry) {
        mutableStateOf(settings.homeCountry)
    }

    var budgetText by remember(settings.yearlyBudget) {
        mutableStateOf(settings.yearlyBudget.toInt().toString())
    }

    var kidAgeText by remember(settings.kidAgeYears) {
        mutableStateOf(settings.kidAgeYears.toString())
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(

        focusedBorderColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        focusedLabelColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        cursorColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        focusedLeadingIconColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        unfocusedBorderColor = MaterialTheme.colorScheme.outline,

        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    SettingsSection(
        title = "Travel Preferences",
        icon = Icons.Default.Home
    ) {

        OutlinedTextField(
            value = homeCityText,
            onValueChange = {
                homeCityText = it
                onHomeCityChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Home City") },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        androidx.compose.ui.focus.FocusDirection.Down
                    )
                }
            )
        )

        OutlinedTextField(
            value = homeCountryText,
            onValueChange = {
                homeCountryText = it
                onHomeCountryChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Home Country") },
            leadingIcon = {
                Icon(Icons.Default.Place, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        androidx.compose.ui.focus.FocusDirection.Down
                    )
                }
            )
        )

        DropdownSetting(
            label = "Currency",
            icon = Icons.Default.Star,
            current = "${settings.currency} - ${
                com.example.travelplanner.util.Money
                    .getSupportedCurrencies()
                    .find { it.first == settings.currency }
                    ?.second ?: settings.currency
            }",
            values = com.example.travelplanner.util.Money
                .getSupportedCurrencies()
                .map { "${it.first} - ${it.second}" },
            onSelect = {
                onCurrencyChange(it.split(" - ")[0])
            }
        )

        OutlinedTextField(
            value = budgetText,
            onValueChange = {
                budgetText = it
                it.toDoubleOrNull()?.let(onBudgetChange)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Yearly Travel Budget") },
            leadingIcon = {
                Icon(Icons.Default.Star, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(
                        androidx.compose.ui.focus.FocusDirection.Down
                    )
                }
            )
        )

        OutlinedTextField(
            value = kidAgeText,
            onValueChange = {
                kidAgeText = it
                it.toIntOrNull()?.let(onKidAgeChange)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Kid Age (years)") },
            leadingIcon = {
                Icon(Icons.Default.Person, null)
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = textFieldColors,
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )

        DropdownSetting(
            label = "Travel Pace",
            icon = Icons.Default.Info,
            current = settings.travelPace.name,
            values = TravelPace.values().map { it.name },
            onSelect = {
                onPaceChange(
                    TravelPace.valueOf(it)
                )
            }
        )
    }
}

@Composable
private fun LLMProviderSection(
    settings: com.example.travelplanner.data.prefs.AppSettings,
    onProviderChange: (LLMProviderType) -> Unit,
    onOpenAIModelChange: (String) -> Unit,
    onGroqModelChange: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onAnthropicModelChange: (String) -> Unit,
    onMCPProvidersChange: (List<String>) -> Unit,
    onSaveOpenAIKey: (String) -> Unit,
    onSaveGroqKey: (String) -> Unit,
    onSaveGeminiKey: (String) -> Unit,
    onSaveAnthropicKey: (String) -> Unit,
    loadOpenAIKey: () -> String,
    loadGroqKey: () -> String,
    loadGeminiKey: () -> String,
    loadAnthropicKey: () -> String
) {
    SettingsSection(
        title = "AI Assistant",
        icon = Icons.Default.Build
    ) {
        DropdownSetting(
            label = "AI Provider",
            icon = Icons.Default.Settings,
            current = settings.llmProvider.name,
            values = LLMProviderType.values().map { it.name },
            onSelect = { onProviderChange(LLMProviderType.valueOf(it)) }
        )

        AnimatedVisibility(
            visible = settings.llmProvider != LLMProviderType.LOCAL,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (settings.llmProvider) {
                    LLMProviderType.OPENAI -> {
                        ProviderConfigCard(
                            providerName = "OpenAI",
                            model = settings.openAIModel,
                            onModelChange = onOpenAIModelChange,
                            onSaveKey = onSaveOpenAIKey,
                            loadKey = loadOpenAIKey,
                            modelOptions = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo"),
                            infoText = "Free tier: No free credits. Paid only.",
                            keyPlaceholder = "sk-..."
                        )
                    }
                    LLMProviderType.GROQ -> {
                        ProviderConfigCard(
                            providerName = "Groq",
                            model = settings.groqModel,
                            onModelChange = onGroqModelChange,
                            onSaveKey = onSaveGroqKey,
                            loadKey = loadGroqKey,
                            modelOptions = listOf(
                                "llama-3.3-70b-versatile",
                                "llama-3.1-70b-versatile",
                                "mixtral-8x7b-32768"
                            ),
                            infoText = "✓ Free tier: 14,400 requests/day",
                            keyPlaceholder = "gsk_..."
                        )
                    }
                    LLMProviderType.GEMINI -> {
                        ProviderConfigCard(
                            providerName = "Google Gemini",
                            model = settings.geminiModel,
                            onModelChange = onGeminiModelChange,
                            onSaveKey = onSaveGeminiKey,
                            loadKey = loadGeminiKey,
                            modelOptions = listOf(
                                "gemini-2.0-flash-exp",
                                "gemini-1.5-flash",
                                "gemini-1.5-pro"
                            ),
                            infoText = "✓ Free tier: 15 requests/min, 1500/day",
                            keyPlaceholder = "AIza..."
                        )
                    }
                    LLMProviderType.ANTHROPIC -> {
                        ProviderConfigCard(
                            providerName = "Anthropic Claude",
                            model = settings.anthropicModel,
                            onModelChange = onAnthropicModelChange,
                            onSaveKey = onSaveAnthropicKey,
                            loadKey = loadAnthropicKey,
                            modelOptions = listOf(
                                "claude-3-5-haiku-20241022",
                                "claude-3-5-sonnet-20241022",
                                "claude-3-opus-20240229"
                            ),
                            infoText = "\$5 free credits for new accounts",
                            keyPlaceholder = "sk-ant-..."
                        )
                    }
                    LLMProviderType.MCP -> {
                        MCPConfigCard(
                            selectedProviders = settings.mcpProviders,
                            onProvidersChange = onMCPProvidersChange,
                            onSaveOpenAIKey = onSaveOpenAIKey,
                            onSaveGroqKey = onSaveGroqKey,
                            onSaveGeminiKey = onSaveGeminiKey,
                            onSaveAnthropicKey = onSaveAnthropicKey,
                            loadOpenAIKey = loadOpenAIKey,
                            loadGroqKey = loadGroqKey,
                            loadGeminiKey = loadGeminiKey,
                            loadAnthropicKey = loadAnthropicKey
                        )
                    }
                    LLMProviderType.LOCAL -> {}
                }
            }
        }
    }
}

@Composable
private fun MCPConfigCard(
    selectedProviders: List<String>,
    onProvidersChange: (List<String>) -> Unit,
    onSaveOpenAIKey: (String) -> Unit,
    onSaveGroqKey: (String) -> Unit,
    onSaveGeminiKey: (String) -> Unit,
    onSaveAnthropicKey: (String) -> Unit,
    loadOpenAIKey: () -> String,
    loadGroqKey: () -> String,
    loadGeminiKey: () -> String,
    loadAnthropicKey: () -> String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "MCP Aggregator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {

                    Text(
                        text = "Best of Multiple AIs",
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Select which AI providers to combine:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val providers = listOf(
                "GROQ",
                "GEMINI",
                "ANTHROPIC",
                "OPENAI"
            )

            providers.forEach { provider ->

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 12.dp,
                                vertical = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = selectedProviders.contains(provider),
                            onCheckedChange = { checked ->

                                val updated =
                                    if (checked)
                                        selectedProviders + provider
                                    else
                                        selectedProviders - provider

                                onProvidersChange(updated)
                            },

                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.tertiary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Text(
                            provider,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        val hasKey =
                            when (provider) {

                                "OPENAI" ->
                                    loadOpenAIKey().isNotEmpty()

                                "GROQ" ->
                                    loadGroqKey().isNotEmpty()

                                "GEMINI" ->
                                    loadGeminiKey().isNotEmpty()

                                "ANTHROPIC" ->
                                    loadAnthropicKey().isNotEmpty()

                                else -> false
                            }

                        if (hasKey) {

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {

                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    Text(
                                        "Ready",
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                        } else {

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {

                                Text(
                                    text = "No Key",
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    ),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            if (selectedProviders.size < 2) {

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {

                    Text(
                        text = "Select at least 2 providers for MCP.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

            } else {

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {

                    Text(
                        text = "MCP will combine ${selectedProviders.size} providers.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderConfigCard(
    providerName: String,
    model: String,
    onModelChange: (String) -> Unit,
    onSaveKey: (String) -> Unit,
    loadKey: () -> String,
    modelOptions: List<String>,
    infoText: String,
    keyPlaceholder: String
) {

    val dark = isSystemInDarkTheme()

    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stored = loadKey()
        if (stored.isNotEmpty()) {
            apiKey = "•".repeat(20)
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(

        focusedBorderColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        focusedLabelColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        cursorColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        focusedLeadingIconColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        focusedTrailingIconColor =
            if (dark)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary,

        unfocusedBorderColor = MaterialTheme.colorScheme.outline,

        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {

                    Text(
                        text = infoText,
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            DropdownSetting(
                label = "Model",
                icon = Icons.Default.Build,
                current = model,
                values = modelOptions,
                onSelect = onModelChange
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = {
                    apiKey = it
                    showSuccess = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = textFieldColors,
                label = {
                    Text("API Key")
                },
                placeholder = {
                    Text(keyPlaceholder)
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null)
                },
                trailingIcon = {

                    IconButton(
                        onClick = {
                            showKey = !showKey
                        }
                    ) {

                        Icon(
                            if (showKey)
                                Icons.Default.Close
                            else
                                Icons.Default.Info,
                            contentDescription = null
                        )
                    }

                },
                singleLine = true,
                visualTransformation =
                    if (showKey)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    onClick = {

                        onSaveKey(apiKey)

                        showSuccess = true

                        apiKey = "•".repeat(20)

                        showKey = false
                    },
                    enabled = apiKey.isNotEmpty() &&
                            !apiKey.startsWith("•")
                ) {

                    Icon(
                        Icons.Default.Check,
                        null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text("Save Key")
                }

                AnimatedVisibility(
                    visible = showSuccess
                ) {

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {

                        Row(
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                "Saved",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {

                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            content()
        }
    }
}

@Composable
private fun DropdownSetting(
    label: String,
    icon: ImageVector,
    current: String,
    values: List<String>,
    onSelect: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (androidx.compose.foundation.isSystemInDarkTheme())
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.primary
                )
            )
        ) {

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = current,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {

            values.forEach { value ->

                DropdownMenuItem(

                    text = {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            if (value == current) {

                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(Modifier.width(8.dp))
                            }

                            Text(
                                value,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },

                    onClick = {

                        expanded = false
                        onSelect(value)

                    }

                )
            }
        }
    }
}
