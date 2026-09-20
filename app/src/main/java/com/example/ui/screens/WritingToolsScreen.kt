package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProviderService
import com.example.data.ai.AIRequest
import com.example.data.local.LibraryDao
import com.example.data.local.LibraryItemEntity
import com.example.data.preferences.UserSettings
import com.example.ui.components.MAXButton
import com.example.ui.components.MAXCard
import com.example.ui.components.MAXChip
import com.example.ui.components.MAXInput
import com.example.ui.components.MAXResultCard
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Screen 3: Writing Tools (Faithfully recreating Mockup Screen 3)
 */
@Composable
fun WritingToolsScreen(
    settings: UserSettings,
    aiService: AIProviderService,
    libraryDao: LibraryDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf("Email") }
    var promptInput by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("Professional") }
    var selectedLanguage by remember { mutableStateOf("English") }

    var isToneMenuOpen by remember { mutableStateOf(false) }
    var isLangMenuOpen by remember { mutableStateOf(false) }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }
    var thinkingProcess by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    val tones = listOf("Professional", "Casual", "Urgent", "Friendly", "Executive", "Persuasive")
    val languages = listOf("English", "Spanish", "French", "German", "Japanese", "Chinese")
    val categories = listOf("All", "Email", "Blog", "Social", "Humanize", "Grammar", "Other")

    fun triggerGeneration() {
        if (promptInput.isBlank()) {
            promptInput = "Write a professional email to my boss for a leave request"
        }
        isGenerating = true
        generatedResult = ""
        thinkingProcess = ""
        isCopied = false
        isSaved = false

        scope.launch {
            val systemInstruction = "You are an elite corporate writing and communications AI. Format output with Subject, Body, and Alternative Version."
            val fullPrompt = "Category: $selectedCategory\nTone: $selectedTone\nLanguage: $selectedLanguage\nTask: $promptInput"

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
        // Header Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Writing Tools",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { /* Menu */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Horizontal Category Filter Pills (Mockup: All, Email, Blog, Social, Other)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    MAXChip(
                        text = cat,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Selected Tool Banner Card (Email Writer)
        item {
            MAXCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedCategory == "All") "Email Writer" else "$selectedCategory Assistant",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Create polished, professional drafts in seconds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Input Card (Describe what your email is about...)
        item {
            Text(
                text = "Describe what your ${selectedCategory.lowercase()} is about...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            MAXInput(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = "e.g. Write a professional email to my boss for a leave request...",
                maxChars = 500,
                minLines = 4,
                leadingIcon = Icons.Outlined.Edit,
                testTag = "writing_input"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tone & Language Selectors
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tone Selector Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Tone",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        MAXCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            onClick = { isToneMenuOpen = true }
                        ) {
                            Text(
                                text = selectedTone,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isToneMenuOpen,
                        onDismissRequest = { isToneMenuOpen = false }
                    ) {
                        tones.forEach { tone ->
                            DropdownMenuItem(
                                text = { Text(tone) },
                                onClick = {
                                    selectedTone = tone
                                    isToneMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Language Selector Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Language",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        MAXCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            onClick = { isLangMenuOpen = true }
                        ) {
                            Text(
                                text = selectedLanguage,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isLangMenuOpen,
                        onDismissRequest = { isLangMenuOpen = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    selectedLanguage = lang
                                    isLangMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Primary Action: ✨ Generate
        item {
            MAXButton(
                text = if (isGenerating) "Generating..." else "✨ Generate",
                onClick = { triggerGeneration() },
                isLoading = isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                testTag = "generate_writing_button"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Generated Result Card
        if (generatedResult.isNotBlank() || isGenerating) {
            item {
                MAXResultCard(
                    title = "$selectedCategory Output",
                    content = if (generatedResult.isBlank() && isGenerating) "Synthesizing draft..." else generatedResult,
                    thinkingText = thinkingProcess.ifBlank { null },
                    isCopied = isCopied,
                    isSaved = isSaved,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MAX-N Draft", generatedResult))
                        isCopied = true
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSave = {
                        scope.launch {
                            libraryDao.insertItem(
                                LibraryItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    type = "writing",
                                    title = if (promptInput.isNotBlank()) promptInput.take(40) else "$selectedCategory draft",
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
                        context.startActivity(Intent.createChooser(sendIntent, "Share Draft"))
                    },
                    onRegenerate = { triggerGeneration() }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Recent Section (matching Mockup Screen 3)
        item {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            MAXCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = {
                    promptInput = "Write a professional leave request email"
                    triggerGeneration()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Leave request email",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "2 hours ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MAXCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = {
                    promptInput = "Write a courteous follow up email regarding previous meeting"
                    triggerGeneration()
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Follow up email",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "5 hours ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
