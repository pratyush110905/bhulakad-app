package com.example.bhullkad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhullkad.BhullkadApp
import com.example.bhullkad.data.entity.Task
import com.example.bhullkad.ui.viewmodel.TodoViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TodoScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as BhullkadApp
    val viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory(app.taskRepository))

    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val archivedTasks by viewModel.archivedTasks.collectAsStateWithLifecycle()

    var showHistory by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showHistory = true }) {
                    Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (activeTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active tasks.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    items(activeTasks, key = { it.id }) { task ->
                        TaskRowWithSwipe(
                            task = task,
                            onToggle = { completed -> viewModel.toggleTaskCompletion(task, completed) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        // Using proper layout weights earlier means this bottom bar respects the IME padding
        // that is configured at the MainScreen scaffold level.
        AddTaskInput(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAdd = viewModel::addTask
        )

        if (showHistory) {
            HistoryBottomSheet(
                archivedTasks = archivedTasks,
                onDismiss = { showHistory = false },
                onClearHistory = viewModel::clearHistory
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskRowWithSwipe(task: Task, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                MaterialTheme.colorScheme.errorContainer else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    ) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (!task.isCompleted) {
                        val dateString = remember(task.createdAt) {
                            Instant.ofEpochMilli(task.createdAt)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                        }
                        Text(
                            text = "Created $dateString",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskInput(modifier: Modifier = Modifier, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("New task...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (text.isNotBlank()) {
                            onAdd(text)
                            text = ""
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onAdd(text)
                        text = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryBottomSheet(
    archivedTasks: List<Task>,
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Archive History", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (archivedTasks.isNotEmpty()) {
                    IconButton(onClick = onClearHistory) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (archivedTasks.isEmpty()) {
                Text(
                    "No completed tasks archived yet. Completed tasks are moved here during the daily reset.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val grouped = remember(archivedTasks) {
                    archivedTasks.groupBy { task ->
                        if (task.completedAt == null) "Unknown" else {
                            Instant.ofEpochMilli(task.completedAt)
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                        }
                    }
                }
                
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    grouped.forEach { (dateStr, tasks) ->
                        item {
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(tasks, key = { it.id }) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
