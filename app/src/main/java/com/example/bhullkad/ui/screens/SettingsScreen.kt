package com.example.bhullkad.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bhullkad.data.ThemeMode
import com.example.bhullkad.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val resetHour by viewModel.resetHour.collectAsStateWithLifecycle()
    val resetMinute by viewModel.resetMinute.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ThemeSettingRow(themeMode = themeMode, onThemeSelected = viewModel::setThemeMode)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            ResetTimeRow(hour = resetHour, minute = resetMinute, onTimeSet = viewModel::setResetTime)
        }
    }
}

@Composable
private fun ThemeSettingRow(themeMode: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (themeMode) {
            ThemeMode.LIGHT -> Icons.Default.LightMode
            ThemeMode.DARK -> Icons.Default.DarkMode
            ThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text("App Theme", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (themeMode) {
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.SYSTEM -> "System default"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("System default") },
                onClick = { onThemeSelected(ThemeMode.SYSTEM); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Light") },
                onClick = { onThemeSelected(ThemeMode.LIGHT); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Dark") },
                onClick = { onThemeSelected(ThemeMode.DARK); expanded = false }
            )
        }
    }
}

@Composable
private fun ResetTimeRow(hour: Int, minute: Int, onTimeSet: (Int, Int) -> Unit) {
    val context = LocalContext.current
    val formattedTime = remember(hour, minute) {
        val amPm = if (hour >= 12) "PM" else "AM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        val m = minute.toString().padStart(2, '0')
        "$h:$m $amPm"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(
                    context,
                    { _, h, m -> onTimeSet(h, m) },
                    hour,
                    minute,
                    false
                ).show()
            }
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text("Daily Reset Time", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Clears completed tasks at $formattedTime",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
