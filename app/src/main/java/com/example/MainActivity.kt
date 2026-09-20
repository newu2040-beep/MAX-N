package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.ai.AIProviderService
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MAXNTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsManager = UserPreferencesManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val aiService = AIProviderService()

        setContent {
            val settings by prefsManager.settings.collectAsState()

            MAXNTheme(themeSetting = settings.theme) {
                AppNavigation(
                    prefsManager = prefsManager,
                    database = database,
                    aiService = aiService
                )
            }
        }
    }
}

