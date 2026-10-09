package com.example.bhullkad.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhullkad.BhullkadApp
import com.example.bhullkad.data.entity.CalendarEvent
import com.example.bhullkad.ui.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as BhullkadApp
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(app.calendarEventRepository))

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val viewedMonth by viewModel.viewedMonth.collectAsStateWithLifecycle()
    val eventsForMonth by viewModel.eventsForViewedMonth.collectAsStateWithLifecycle()
    val eventsForDate by viewModel.eventsForSelectedDate.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        MonthHeader(
            viewedMonth = viewedMonth,
            onPrevMonth = { viewModel.setViewedMonth(viewedMonth.minusMonths(1)) },
            onNextMonth = { viewModel.setViewedMonth(viewedMonth.plusMonths(1)) }
        )
        
        CalendarGrid(
            viewedMonth = viewedMonth,
            selectedDate = selectedDate,
            eventsInMonth = eventsForMonth,
            onDateSelected = {
                viewModel.selectDate(it)
                if (it.month != viewedMonth.month) {
                    viewModel.setViewedMonth(it)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateText = remember(selectedDate) {
                "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.dayOfMonth}"
            }
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Event", tint = MaterialTheme.colorScheme.primary)
            }
        }

        if (eventsForDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No events scheduled.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(eventsForDate, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onDelete = { viewModel.deleteEvent(event) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            date = selectedDate,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc ->
                viewModel.addEvent(title, desc, selectedDate)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MonthHeader(
    viewedMonth: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }
        val monthYearText = remember(viewedMonth) {
            "${viewedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${viewedMonth.year}"
        }
        Text(
            text = monthYearText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

@Composable
private fun CalendarGrid(
    viewedMonth: LocalDate,
    selectedDate: LocalDate,
    eventsInMonth: List<CalendarEvent>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = remember { listOf("M", "T", "W", "T", "F", "S", "S") }
    val yearMonth = YearMonth.from(viewedMonth)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = viewedMonth.withDayOfMonth(1).dayOfWeek.value
    val prevMonthLength = yearMonth.minusMonths(1).lengthOfMonth()
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        var currentDay = 1
        var isNextMonth = false
        var prevMonthDay = prevMonthLength - firstDayOfWeek + 2
        
        for (row in 0..5) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                for (col in 0..6) {
                    val date: LocalDate
                    val isCurrentMonth: Boolean
                    
                    if (row == 0 && col < firstDayOfWeek - 1) {
                        date = viewedMonth.minusMonths(1).withDayOfMonth(prevMonthDay++)
                        isCurrentMonth = false
                    } else if (currentDay > daysInMonth) {
                        if (!isNextMonth) {
                            currentDay = 1
                            isNextMonth = true
                        }
                        date = viewedMonth.plusMonths(1).withDayOfMonth(currentDay++)
                        isCurrentMonth = false
                    } else {
                        date = viewedMonth.withDayOfMonth(currentDay++)
                        isCurrentMonth = true
                    }

                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val hasEvents = eventsInMonth.any { it.date == date.toString() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    !isCurrentMonth -> MaterialTheme.colorScheme.outline
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (hasEvents && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(event: CalendarEvent, onDelete: () -> Unit) {
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
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEventDialog(date: LocalDate, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Event on ${date.dayOfMonth}") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, desc) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
