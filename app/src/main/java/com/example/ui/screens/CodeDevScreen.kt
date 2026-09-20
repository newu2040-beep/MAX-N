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
import androidx.compose.material.icons.outlined.Code
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
import com.example.ui.components.MAXCard
import com.example.ui.components.MAXChip
import com.example.ui.components.MAXInput
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CodeDevScreen(
    settings: UserSettings,
    aiService: AIProviderService,
    libraryDao: LibraryDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTool by remember { mutableStateOf("Generate Code") }
    var codePrompt by remember { mutableStateOf("") }
    var languageChoice by remember { mutableStateOf("Kotlin") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedCode by remember { mutableStateOf("") }
    var thinkingProcess by remember { mutableStateOf("") }

    val tools = listOf("Generate Code", "Debug & Fix", "Refactor", "Explain", "SQL Generator", "Regex")
    val languages = listOf("Kotlin", "Swift", "Python", "TypeScript", "Rust", "Go", "SQL")

    fun triggerCodeGeneration() {
        if (codePrompt.isBlank()) {
            codePrompt = "Implement an async queue worker with exponential backoff and cancellation in $languageChoice"
        }
        isGenerating = true
        generatedCode = ""
        thinkingProcess = ""

        scope.launch {
            val systemInstruction = "You are a Principal Software Engineer. Write clean, idiomatic, production-ready code with complete type annotations, markdown code blocks, and clear architectural explanations."
            val fullPrompt = "Tool: $selectedTool\nLanguage: $languageChoice\nTask: $codePrompt"

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
                    generatedCode += chunk.text
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
                    text = "Code & Dev Studio",
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
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { lang ->
                    MAXChip(
                        text = lang,
                        selected = languageChoice == lang,
                        onClick = { languageChoice = lang }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Describe function, paste code, or specify requirements...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            MAXInput(
                value = codePrompt,
                onValueChange = { codePrompt = it },
                placeholder = "e.g. Implement an async queue worker with exponential backoff and cancellation...",
                maxChars = 1500,
                minLines = 4,
                leadingIcon = Icons.Outlined.Code,
                testTag = "code_prompt_input"
            )

            Spacer(modifier = Modifier.height(18.dp))

            MAXButton(
                text = if (isGenerating) "Compiling Solution..." else "✨ Generate Code Solution",
                onClick = { triggerCodeGeneration() },
                isLoading = isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                testTag = "generate_code_button"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (generatedCode.isNotBlank() || isGenerating) {
            item {
                MAXCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "$selectedTool ($languageChoice)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FormattedTextContent(
                            content = if (generatedCode.isBlank() && isGenerating) "Writing clean code..." else generatedCode
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
