package com.example.bhullkad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bhullkad.data.UserPreferences
import com.example.bhullkad.data.ThemeMode
import com.example.bhullkad.reset.DailyResetManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: UserPreferences,
    private val resetManager: DailyResetManager,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val resetHour: StateFlow<Int> = preferences.resetHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val resetMinute: StateFlow<Int> = preferences.resetMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setResetTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferences.setResetTime(hour, minute)
            resetManager.syncNow()
        }
    }

    class Factory(
        private val preferences: UserPreferences,
        private val resetManager: DailyResetManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferences, resetManager) as T
        }
    }
}
