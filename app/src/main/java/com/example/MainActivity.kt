package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.ai.AIProviderService
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MAXNTheme

class MainActivity : ComponentActivity() {

    private var sharedTextState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleShareIntent(intent)

        val prefsManager = UserPreferencesManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val aiService = AIProviderService()

        setContent {
            val settings by prefsManager.settings.collectAsState()

            MAXNTheme(themeSetting = settings.theme, isDarkMode = settings.isDarkMode) {
                AppNavigation(
                    prefsManager = prefsManager,
                    database = database,
                    aiService = aiService,
                    sharedTextFromIntent = sharedTextState
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                sharedTextState = text
            }
        }
    }
}
