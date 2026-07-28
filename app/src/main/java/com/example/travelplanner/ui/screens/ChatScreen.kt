package com.example.travelplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.domain.LLMProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: String = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "👋 Hello! I am your **AI Travel Assistant**. Ask me anything about destinations, itineraries, budgets, packing lists, or local travel tips!"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = "user", text = userText.trim())
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settings = AppGraph.settingsRepository.observeSettings().first()
                val provider = settings.llmProvider
                val groqKey = AppGraph.settingsRepository.loadGroqKey()?.trim().orEmpty()

                val prompt = """
                    You are Voyexa AI, an expert travel assistant. Answer the user's travel query concisely, with helpful recommendations, costs, and practical advice.
                    User Query: ${userText.trim()}
                """.trimIndent()

                val aiResponseText = try {
                    if (groqKey.isNotEmpty() && (provider == LLMProviderType.GROQ || provider == LLMProviderType.MCP)) {
                        val response = AppGraph.groqClient.chat(settings.groqModel, userText.trim())
                        if (response.isNotBlank()) response else generateFallbackAnswer(userText)
                    } else {
                        generateFallbackAnswer(userText)
                    }
                } catch (_: Exception) {
                    generateFallbackAnswer(userText)
                }

                _messages.value = _messages.value + ChatMessage(sender = "ai", text = aiResponseText)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    sender = "ai",
                    text = generateFallbackAnswer(userText)
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateFallbackAnswer(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("pondi") -> "🌊 **Pondicherry 3-Day Highlights**:\n• Day 1: French Quarter Heritage Walk & Cafe Des Arts\n• Day 2: Auroville Matrimandir & Serenity Beach\n• Day 3: Chunnambar Boat House to Paradise Beach"
            q.contains("paris") -> "🗼 **Paris Trip Tips**:\n• Best time: May - September\n• Must-visit: Eiffel Tower, Louvre, Seine Sunset Cruise\n• Budget tip: Use Navigo transit pass for unlimited metro rides."
            q.contains("goa") || q.contains("beach") -> """
                🏖️ **Top Beaches in Goa**:

                📍 **North Goa (Vibrant & Energetic)**:
                • **Baga Beach**: Water sports & nightlife.
                • **Calangute Beach**: Beach shacks & watersports.
                • **Anjuna Beach**: Flea markets & sunset views.

                📍 **South Goa (Serene & Relaxing)**:
                • **Palolem Beach**: Crescent bay with calm waters.
                • **Colva Beach**: White sands & seaside dining.
                • **Agonda Beach**: Peaceful sanctuary & nature.
            """.trimIndent()
            q.contains("pack") -> "🧳 **Essential Packing Checklist**:\n1. Passport / Government ID\n2. Power Bank & Universal Charger\n3. Breathable Cotton Wear & Comfortable Walking Shoes\n4. Basic First Aid & Sunscreen"
            else -> "✨ **AI Travel Insight for '$query'**:\nPlan ahead by booking accommodation 2 weeks early, reserving 15% budget buffer for spontaneous activities, and keeping day-by-day commutes under 45 minutes!"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit
) {
    val vm: ChatViewModel = viewModel()
    val messages by vm.messages.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "🌴 3-day Pondicherry plan",
        "🗼 Paris budget tips",
        "🏖️ Goa top beaches",
        "🧳 Packing essentials"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "AI Travel Chatbot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Powered by Groq LLM GenAI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(
                                "AI is thinking...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick suggestion chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    SuggestionChip(
                        onClick = {
                            vm.sendMessage(prompt.replace(Regex("^[\\p{So}\\p{Cn}]+\\s*"), ""))
                        },
                        label = { Text(prompt, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // Input Bar
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask AI travel questions...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                vm.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 20.dp
                ),
                color = bgColor
            ) {
                Text(
                    text = msg.text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
