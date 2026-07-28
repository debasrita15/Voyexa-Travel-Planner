package com.example.travelplanner.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.travelplanner.ui.viewmodel.guide.ChatViewModel
import com.example.travelplanner.ui.viewmodel.guide.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messagesMap by viewModel.messages.collectAsState()
    var selectedConversation by remember { mutableStateOf<String?>(null) }
    var textInput by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    if (selectedConversation != null) {
        val contactName = selectedConversation!!
        val thread = messagesMap[contactName] ?: emptyList()

        LaunchedEffect(thread.size) {
            if (thread.isNotEmpty()) {
                listState.animateScrollToItem(thread.size - 1)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contactName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(contactName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Online", style = MaterialTheme.typography.bodySmall, color = Color(0xFF10B981))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedConversation = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to list")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Phone, contentDescription = "Call") }
                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = "Options") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(thread) { message ->
                        val bubbleColor = if (message.isMe) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        val contentColor = if (message.isMe) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val alignment = if (message.isMe) Alignment.End else Alignment.Start
                        val shape = if (message.isMe) {
                            RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
                        } else {
                            RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignment
                        ) {
                            Surface(
                                shape = shape,
                                color = bubbleColor,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = message.text,
                                        color = contentColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = message.time,
                                        color = contentColor.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                // Input bar
                Surface(
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Attachments")
                        }
                        Spacer(Modifier.width(4.dp))
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendMessage(contactName, textInput)
                                    textInput = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Conversations", fontWeight = FontWeight.Bold) },
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
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            if (messagesMap.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No Active Traveller Chats",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "When you accept a traveller's trip request, their active chat will automatically appear here!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messagesMap.keys.toList()) { name ->
                        val lastMessage = messagesMap[name]?.lastOrNull()?.text ?: "No messages"
                        val lastTime = messagesMap[name]?.lastOrNull()?.time ?: ""

                        Card(
                            onClick = { selectedConversation = name },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(lastTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = lastMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
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
