package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ai.AIProviderService
import com.example.data.ai.AIRequest
import com.example.data.local.LibraryDao
import com.example.data.local.LibraryItemEntity
import com.example.data.preferences.UserSettings
import com.example.ui.components.MAXButton
import com.example.ui.components.MAXInput
import com.example.ui.components.MAXResultCard
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun PrdGeneratorScreen(
    settings: UserSettings,
    aiService: AIProviderService,
    libraryDao: LibraryDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var appIdea by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedPrd by remember { mutableStateOf("") }
    var thinkingProcess by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    fun generatePrd() {
        if (appIdea.isBlank()) {
            appIdea = "Autonomous AI-powered personal productivity workspace"
        }
        isGenerating = true
        generatedPrd = ""
        thinkingProcess = ""
        isCopied = false
        isSaved = false

        scope.launch {
            val systemInstruction = "You are a Principal Product Architect at Apple/Google. Generate an exhaustive, production-grade 16-section PRD covering: 1. Overview, 2. Problem, 3. Target Users, 4. Core Features, 5. User Flows, 6. Screen Architecture, 7. UI/UX Specs, 8. Technical Stack, 9. Database Schema, 10. API Endpoints, 11. Auth, 12. Security, 13. Permissions, 14. Performance, 15. Testing, 16. Launch Checklist."
            val fullPrompt = "App Idea: $appIdea\nTarget Audience: ${targetAudience.ifBlank { "Knowledge workers & engineers" }}\nTech Preferences: ${techStack.ifBlank { "Modern Kotlin, Jetpack Compose, Room" }}"

            val request = AIRequest(
                prompt = fullPrompt,
                systemInstruction = systemInstruction,
                isThinkingEnabled = settings.isThinkingModeEnabled
            )

            aiService.streamContent(request, settings).collect { chunk ->
                if (chunk.thinkingText != null) {
                    thinkingProcess += chunk.thinkingText
                }
                if (chunk.text.isNotBlank()) {
                    generatedPrd += chunk.text
                }
                if (chunk.isDone) {
                    isGenerating = false
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "PRD Generator",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { /* Menu */ }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "Product Idea & Core Vision",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            MAXInput(
                value = appIdea,
                onValueChange = { appIdea = it },
                placeholder = "Describe the product idea, key user problem, and core value proposition...",
                maxChars = 1000,
                minLines = 4,
                leadingIcon = Icons.Outlined.Description,
                testTag = "prd_idea_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Target Audience (Optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            MAXInput(
                value = targetAudience,
                onValueChange = { targetAudience = it },
                placeholder = "e.g. Remote engineers, product managers, students...",
                maxChars = 200,
                minLines = 2,
                testTag = "prd_audience_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            MAXButton(
                text = if (isGenerating) "Synthesizing PRD Architecture..." else "✨ Generate Complete PRD",
                onClick = { generatePrd() },
                isLoading = isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                testTag = "generate_prd_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (generatedPrd.isNotBlank() || isGenerating) {
            item {
                MAXResultCard(
                    title = "Complete Product Requirements Document",
                    content = if (generatedPrd.isBlank() && isGenerating) "Architecting 16 specification modules..." else generatedPrd,
                    thinkingText = thinkingProcess.ifBlank { null },
                    isCopied = isCopied,
                    isSaved = isSaved,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MAX-N PRD", generatedPrd))
                        isCopied = true
                        Toast.makeText(context, "PRD copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSave = {
                        scope.launch {
                            libraryDao.insertItem(
                                LibraryItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    type = "prd",
                                    title = if (appIdea.isNotBlank()) "PRD: " + appIdea.take(35) else "Product Requirements Document",
                                    content = generatedPrd
                                )
                            )
                            isSaved = true
                            Toast.makeText(context, "Saved to Library", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, generatedPrd)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share PRD"))
                    },
                    onRegenerate = { generatePrd() }
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
