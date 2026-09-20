package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
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
import com.example.ui.components.MAXChip
import com.example.ui.components.MAXInput
import com.example.ui.components.MAXResultCard
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ProductivityScreen(
    settings: UserSettings,
    aiService: AIProviderService,
    libraryDao: LibraryDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTool by remember { mutableStateOf("Daily Plan") }
    var taskInput by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }
    var thinkingProcess by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    val tools = listOf("Daily Plan", "Task Breakdown", "Meeting Notes", "Decision Matrix", "Study Guide")

    fun triggerProductivityGen() {
        if (taskInput.isBlank()) {
            taskInput = "Plan my workday: ship app release, review PRs, team standup, 45m workout"
        }
        isGenerating = true
        generatedResult = ""
        thinkingProcess = ""
        isCopied = false
        isSaved = false

        scope.launch {
            val systemInstruction = "You are an executive productivity strategist. Structure actionable, timeblocked, prioritized schedules and plans."
            val fullPrompt = "Tool: $selectedTool\nInput: $taskInput"

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
                    generatedResult += chunk.text
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
                    text = "Productivity Hub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tools.forEach { tool ->
                    MAXChip(
                        text = tool,
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            Text(
                text = "Enter notes, goals, or meeting agenda...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            MAXInput(
                value = taskInput,
                onValueChange = { taskInput = it },
                placeholder = "e.g. Plan my workday: ship app release, review PRs, team standup, 45m workout...",
                maxChars = 800,
                minLines = 4,
                leadingIcon = Icons.Outlined.CalendarToday,
                testTag = "productivity_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            MAXButton(
                text = if (isGenerating) "Structuring Plan..." else "✨ Organize & Generate",
                onClick = { triggerProductivityGen() },
                isLoading = isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                testTag = "generate_productivity_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (generatedResult.isNotBlank() || isGenerating) {
            item {
                MAXResultCard(
                    title = "$selectedTool Output",
                    content = if (generatedResult.isBlank() && isGenerating) "Synthesizing schedule..." else generatedResult,
                    thinkingText = thinkingProcess.ifBlank { null },
                    isCopied = isCopied,
                    isSaved = isSaved,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MAX-N Plan", generatedResult))
                        isCopied = true
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSave = {
                        scope.launch {
                            libraryDao.insertItem(
                                LibraryItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    type = "productivity",
                                    title = if (taskInput.isNotBlank()) taskInput.take(35) else selectedTool,
                                    content = generatedResult
                                )
                            )
                            isSaved = true
                            Toast.makeText(context, "Saved to Library", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, generatedResult)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Plan"))
                    },
                    onRegenerate = { triggerProductivityGen() }
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
