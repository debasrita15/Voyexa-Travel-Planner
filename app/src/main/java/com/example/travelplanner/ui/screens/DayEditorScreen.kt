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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelplanner.app.AppGraph
import com.example.travelplanner.data.db.ActivityItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*

class DayEditorViewModel(private val dayId: String) : ViewModel() {

    val itemsFlow = AppGraph.tripRepository.observeItemsForDay(dayId)
        .stateIn(AppGraph.appScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun persistOrder(updated: List<ActivityItemEntity>) {
        viewModelScope.launch {
            updated.forEachIndexed { index, item ->
                AppGraph.tripRepository.updateActivity(item.copy(sortIndex = index))
            }
        }
    }

    fun addItem() {
        viewModelScope.launch {
            val current = itemsFlow.value
            val nextIndex = (current.maxOfOrNull { it.sortIndex } ?: 0) + 1
            val newItem = ActivityItemEntity(
                id = java.util.UUID.randomUUID().toString(),
                itineraryDayId = dayId,
                title = "New activity",
                category = "custom",
                timeSlot = "Custom",
                estimatedCost = 0.0,
                commuteMinutes = 15,
                notes = "Update details",
                sortIndex = nextIndex
            )
            AppGraph.tripRepository.upsertItems(listOf(newItem))
        }
    }
}

@Suppress("UNCHECKED_CAST")
private class DayEditorVMFactory(private val dayId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DayEditorViewModel(dayId) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditorScreen(dayId: String, onBack: () -> Unit) {
    val vm: DayEditorViewModel = viewModel(factory = DayEditorVMFactory(dayId))
    val items by vm.itemsFlow.collectAsState()

    val scope = rememberCoroutineScope()

    var listStateItems by remember { mutableStateOf(items.toMutableList()) }
    LaunchedEffect(items) {
        listStateItems = items.sortedBy { it.sortIndex }.toMutableList()
    }

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            listStateItems.add(to.index, listStateItems.removeAt(from.index))
        },
        onDragEnd = { _, _ ->
            vm.persistOrder(listStateItems)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Day Editor", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { vm.addItem() }) { Text("Add") }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            state = reorderState.listState,
            modifier = Modifier.padding(pad).padding(16.dp)
                .reorderable(reorderState)
                .detectReorderAfterLongPress(reorderState)
        ) {
            items(listStateItems, key = { it.id }) { item ->
                ReorderableItem(reorderState, key = item.id) { isDragging ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("${item.timeSlot} • ${item.commuteMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(item.notes, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
