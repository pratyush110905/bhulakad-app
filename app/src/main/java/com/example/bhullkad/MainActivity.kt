package com.example.bhullkad

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bhullkad.data.ThemeMode
import com.example.bhullkad.ui.navigation.MainScreen
import com.example.bhullkad.ui.screens.SettingsScreen
import com.example.bhullkad.ui.theme.BhullkadTheme
import com.example.bhullkad.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.supportedModes?.maxByOrNull { it.refreshRate }?.let { highResMode ->
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = highResMode.modeId
                }
            }
        }

        enableEdgeToEdge()
        
        setContent {
            val app = application as BhullkadApp
            val themeMode by app.preferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            
            BhullkadTheme(themeMode = themeMode) {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "main") {
                    composable("main") {
                        MainScreen(onOpenSettings = { nav.navigate("settings") })
                    }
                    composable("settings") {
                        val settingsVm: SettingsViewModel = viewModel(
                            factory = SettingsViewModel.Factory(app.preferences, app.resetManager)
                        )
                        SettingsScreen(viewModel = settingsVm, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
