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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProviderService
import com.example.data.ai.AIRequest
import com.example.data.local.CustomToolEntity
import com.example.data.local.DailyMemoryDao
import com.example.data.local.DailyMemoryEntity
import com.example.data.local.LibraryDao
import com.example.data.local.LibraryItemEntity
import com.example.data.preferences.UserSettings
import com.example.data.tools.FormatTarget
import com.example.data.tools.MAXTool
import com.example.data.tools.MAXToolRegistry
import com.example.data.tools.QuickTransformAction
import com.example.ui.components.MAXStarMark
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolRunnerScreen(
    toolId: String,
    initialInput: String? = null,
    settings: UserSettings,
    aiService: AIProviderService,
    libraryDao: LibraryDao,
    dailyMemoryDao: DailyMemoryDao,
    customToolEntity: CustomToolEntity? = null,
    onBack: () -> Unit,
    onChainToTool: (targetToolId: String, textToPass: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Resolve Tool
    val tool: MAXTool = remember(toolId, customToolEntity) {
        if (customToolEntity != null) {
            MAXTool(
                id = customToolEntity.id,
                name = customToolEntity.name,
                category = com.example.data.tools.ToolCategory.CUSTOM,
                description = customToolEntity.description,
                inputLabel = "Input (${customToolEntity.inputType})",
                inputPlaceholder = "Enter content here...",
                systemInstruction = customToolEntity.instructions,
                badge = "Custom",
                isCustom = true,
                preferredModel = customToolEntity.preferredModel
            )
        } else {
            MAXToolRegistry.BUILT_IN_TOOLS.find { it.id == toolId } ?: MAXToolRegistry.BUILT_IN_TOOLS.first()
        }
    }

    var userInput by remember { mutableStateOf(initialInput ?: "") }
    var selectedFormatTarget by remember { mutableStateOf(FormatTarget.TABLE) }
    var selectedTransformAction by remember { mutableStateOf(QuickTransformAction.EXPLAIN) }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }
    var thinkingProcess by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showMemoryPicker by remember { mutableStateOf(false) }
    var showChainPicker by remember { mutableStateOf(false) }
    var showTransformMenu by remember { mutableStateOf(false) }

    val allMemories by dailyMemoryDao.getAllMemories().collectAsState(initial = emptyList())

    // Auto-run if opened with initialInput and not already generated
    LaunchedEffect(toolId, initialInput) {
        if (!initialInput.isNullOrBlank() && generatedResult.isEmpty() && !isGenerating) {
            userInput = initialInput
        }
    }

    fun runTool(overrideInput: String? = null, customInstruction: String? = null) {
        val inputToUse = overrideInput ?: userInput
        if (inputToUse.isBlank() || isGenerating) return

        isGenerating = true
        generatedResult = ""
        thinkingProcess = null
        errorMessage = null

        val finalInstruction = buildString {
            append(customInstruction ?: tool.systemInstruction)
            append("\n\n")
            if (tool.id == "format_converter") {
                append("TARGET FORMAT: ${selectedFormatTarget.displayName}.\n")
                append(selectedFormatTarget.instruction)
                append("\n\n")
            } else if (tool.id == "quick_transform") {
                append("ACTION: ${selectedTransformAction.displayName}.\n")
                append(selectedTransformAction.promptInstruction)
                append("\n\n")
            }
            // Auto inject pinned memories
            val pinned = allMemories.filter { it.isPinned }
            if (pinned.isNotEmpty()) {
                append("USER'S SAVED AI PREFERENCES & INSTRUCTIONS:\n")
                pinned.forEach { p ->
                    append("- [${p.category}] ${p.title}: ${p.content}\n")
                }
                append("\n")
            }
        }

        coroutineScope.launch {
            val request = AIRequest(
                prompt = inputToUse,
                systemInstruction = finalInstruction,
                model = tool.preferredModel ?: if (settings.customModel.isNotBlank()) settings.customModel else null,
                isThinkingEnabled = settings.isThinkingModeEnabled
            )

            aiService.streamContent(request, settings)
                .catch { e ->
                    errorMessage = e.message ?: "An unexpected error occurred."
                    isGenerating = false
                }
                .collect { chunk ->
                    if (!chunk.thinkingText.isNullOrBlank()) {
                        thinkingProcess = (thinkingProcess ?: "") + chunk.thinkingText
                    }
                    if (chunk.text.isNotBlank()) {
                        generatedResult += chunk.text
                    }
                    if (chunk.isDone) {
                        isGenerating = false
                        if (chunk.errorMessage != null) {
                            errorMessage = chunk.errorMessage
                        }
                    }
                }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("tool_runner_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                MAXStarMark(size = 20.dp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tool.category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            // Sub-Selectors for Format Converter & Quick Transform
            if (tool.id == "format_converter") {
                Text(
                    text = "Select Output Target Format:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FormatTarget.values().toList()) { target ->
                        val isSelected = selectedFormatTarget == target
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedFormatTarget = target }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = target.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (tool.id == "quick_transform") {
                Text(
                    text = "Select Transformation Action:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QuickTransformAction.values().toList()) { action ->
                        val isSelected = selectedTransformAction == action
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedTransformAction = action }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = action.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Input Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Actions row above textfield: Paste, Insert Memory, Use Sample
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = tool.inputLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // Paste from Clipboard
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clip.isNullOrBlank()) {
                                        userInput = clip
                                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Insert from Daily Memory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showMemoryPicker = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Memory",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Memory", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Sample prompt if available
                        if (tool.suggestedSample.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        userInput = tool.suggestedSample
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Sample",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sample", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        placeholder = { Text(tool.inputPlaceholder, style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { runTool() },
                        enabled = userInput.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_tool_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing with AI...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Run")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run ${tool.name}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thinking Process if enabled
            AnimatedVisibility(visible = !thinkingProcess.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Reasoning",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reasoning & Extraction Grounding",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = thinkingProcess ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Results Card
            if (generatedResult.isNotBlank() || isGenerating) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tool_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Output Result",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // 1. Universal "Transform" Menu Button (Feature #6)
                            Box {
                                IconButton(
                                    onClick = { showTransformMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Transform,
                                        contentDescription = "Transform",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showTransformMenu,
                                    onDismissRequest = { showTransformMenu = false }
                                ) {
                                    QuickTransformAction.values().forEach { action ->
                                        DropdownMenuItem(
                                            text = { Text(action.displayName) },
                                            onClick = {
                                                showTransformMenu = false
                                                val transformInstruction = "Transform this existing output: ${action.promptInstruction}"
                                                runTool(overrideInput = generatedResult, customInstruction = transformInstruction)
                                            }
                                        )
                                    }
                                }
                            }

                            // 2. Chain into Next Tool (Feature #10)
                            IconButton(
                                onClick = { showChainPicker = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Chain Tool",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 3. Save to Library
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val item = LibraryItemEntity(
                                            id = UUID.randomUUID().toString(),
                                            type = "tool_result",
                                            title = "${tool.name} Result",
                                            content = generatedResult,
                                            metaJson = tool.id,
                                            createdAt = System.currentTimeMillis()
                                        )
                                        libraryDao.insertItem(item)
                                        Toast.makeText(context, "Saved to Library!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 4. Copy to Clipboard
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText(tool.name, generatedResult))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 5. Share
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, generatedResult)
                                        putExtra(Intent.EXTRA_SUBJECT, "${tool.name} Output")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Output"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = generatedResult,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        if (isGenerating) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Streaming live response...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Error Message
            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Daily Memory Selection Dialog
    if (showMemoryPicker) {
        AlertDialog(
            onDismissRequest = { showMemoryPicker = false },
            title = { Text("Insert from Daily AI Memory") },
            text = {
                if (allMemories.isEmpty()) {
                    Text("No saved memories. Add reusable instructions or templates from Explore → Daily AI Memory.")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allMemories) { mem ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable {
                                        userInput = if (userInput.isBlank()) mem.content else "$userInput\n\n${mem.content}"
                                        showMemoryPicker = false
                                        Toast.makeText(context, "Inserted '${mem.title}'", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(mem.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(mem.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(mem.content, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showMemoryPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Chain Tool Picker Dialog (Feature #10)
    if (showChainPicker) {
        AlertDialog(
            onDismissRequest = { showChainPicker = false },
            title = { Text("Chain Output into Next Tool") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select which tool should process this output next:", style = MaterialTheme.typography.bodySmall)
                    MAXToolRegistry.BUILT_IN_TOOLS.filter { it.id != tool.id }.take(6).forEach { nextTool ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    showChainPicker = false
                                    onChainToTool(nextTool.id, generatedResult)
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nextTool.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(nextTool.category.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Icon(Icons.Default.Link, contentDescription = "Chain", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showChainPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
