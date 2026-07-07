package com.example.travelplanner.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val settings = vm.settingsFlow.collectAsState().value

    if (settings == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { pad ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(pad),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    Scaffold(
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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

            Spacer(Modifier.height(16.dp))
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
    var homeCityText by remember(settings.homeCity) { mutableStateOf(settings.homeCity) }
    var homeCountryText by remember(settings.homeCountry) { mutableStateOf(settings.homeCountry) }
    var budgetText by remember(settings.yearlyBudget) { mutableStateOf(settings.yearlyBudget.toInt().toString()) }
    var kidAgeText by remember(settings.kidAgeYears) { mutableStateOf(settings.kidAgeYears.toString()) }

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
            label = { Text("Home City") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = homeCountryText,
            onValueChange = {
                homeCountryText = it
                onHomeCountryChange(it)
            },
            label = { Text("Home Country") },
            leadingIcon = { Icon(Icons.Default.Place, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        DropdownSetting(
            label = "Currency",
            icon = Icons.Default.Star,
            current = "${settings.currency} - ${com.example.travelplanner.util.Money.getSupportedCurrencies().find { it.first == settings.currency }?.second ?: settings.currency}",
            values = com.example.travelplanner.util.Money.getSupportedCurrencies().map { "${it.first} - ${it.second}" },
            onSelect = {
                val currencyCode = it.split(" - ")[0]
                onCurrencyChange(currencyCode)
            }
        )

        OutlinedTextField(
            value = budgetText,
            onValueChange = {
                budgetText = it
                it.toDoubleOrNull()?.let { value -> onBudgetChange(value) }
            },
            label = { Text("Yearly Travel Budget") },
            leadingIcon = { Icon(Icons.Default.Star, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        OutlinedTextField(
            value = kidAgeText,
            onValueChange = {
                kidAgeText = it
                it.toIntOrNull()?.let { value -> onKidAgeChange(value) }
            },
            label = { Text("Kid Age (years)") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        DropdownSetting(
            label = "Travel Pace",
            icon = Icons.Default.Info,
            current = settings.travelPace.name,
            values = TravelPace.values().map { it.name },
            onSelect = { onPaceChange(TravelPace.valueOf(it)) }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✓ Best of Multiple AIs",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                "Select which AI providers to combine (needs their API keys):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val availableProviders = listOf("GROQ", "GEMINI", "ANTHROPIC", "OPENAI")

            availableProviders.forEach { provider ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = selectedProviders.contains(provider),
                            onCheckedChange = { checked ->
                                val newList = if (checked) {
                                    selectedProviders + provider
                                } else {
                                    selectedProviders - provider
                                }
                                onProvidersChange(newList)
                            }
                        )
                        Text(provider)
                    }

                    val hasKey = when (provider) {
                        "OPENAI" -> loadOpenAIKey().isNotEmpty()
                        "GROQ" -> loadGroqKey().isNotEmpty()
                        "GEMINI" -> loadGeminiKey().isNotEmpty()
                        "ANTHROPIC" -> loadAnthropicKey().isNotEmpty()
                        else -> false
                    }

                    if (hasKey) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            "No key",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (selectedProviders.size < 2) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "⚠️ Select at least 2 providers for MCP to work",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "✓ MCP will merge results from ${selectedProviders.size} providers",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
    var apiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stored = loadKey()
        if (stored.isNotEmpty()) {
            apiKey = "•".repeat(20)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = infoText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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
                label = { Text("API Key") },
                placeholder = { Text(keyPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            if (showKey) Icons.Default.Close else Icons.Default.Info,
                            "Toggle visibility"
                        )
                    }
                },
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onSaveKey(apiKey)
                        showSuccess = true
                        apiKey = "•".repeat(20)
                        showKey = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = apiKey.isNotEmpty() && !apiKey.startsWith("•")
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save Key")
                }

                AnimatedVisibility(
                    visible = showSuccess,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Saved",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Divider()
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(current, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                values.forEach { v ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (v == current) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(v)
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelect(v)
                        }
                    )
                }
            }
        }
    }
}
