package com.example.bhullkad.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bhullkad_settings")

class UserPreferences(private val context: Context) {

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val RESET_HOUR = intPreferencesKey("reset_hour")
        val RESET_MINUTE = intPreferencesKey("reset_minute")
        val LAST_RESET_AT = longPreferencesKey("last_reset_at")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        try {
            ThemeMode.valueOf(prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    val resetHour: Flow<Int> = context.dataStore.data.map { it[RESET_HOUR] ?: 0 }
    val resetMinute: Flow<Int> = context.dataStore.data.map { it[RESET_MINUTE] ?: 0 }

    suspend fun setResetTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[RESET_HOUR] = hour
            it[RESET_MINUTE] = minute
        }
    }

    val lastResetAt: Flow<Long> = context.dataStore.data.map { it[LAST_RESET_AT] ?: 0L }

    suspend fun setLastResetAt(millis: Long) {
        context.dataStore.edit { it[LAST_RESET_AT] = millis }
    }
}
