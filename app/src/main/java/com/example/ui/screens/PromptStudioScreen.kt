package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Image
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
 * Screen 4: Image & Prompt Tools / Scene-by-Scene Builder (Faithfully recreating Mockup Screen 4)
 */
@Composable
fun PromptStudioScreen(
    settings: UserSettings,
    aiService: AIProviderService,
    libraryDao: LibraryDao,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedMode by remember { mutableStateOf("Scene to Scene") }
    var sceneInput by remember { mutableStateOf("") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf("Realistic") }
    var selectedQuality by remember { mutableStateOf("High") }

    var isStyleMenuOpen by remember { mutableStateOf(false) }
    var isQualityMenuOpen by remember { mutableStateOf(false) }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedResult by remember { mutableStateOf("") }
    var thinkingProcess by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    val modes = listOf("Text to Image", "Scene to Scene", "Image Prompt", "Character", "Cinematic")
    val aspectRatios = listOf("1:1", "9:16", "16:9", "4:3")
    val styles = listOf("Realistic", "Cinematic 35mm", "Anime/Manga", "Cyberpunk", "Minimal 3D", "Editorial Photography")
    val qualities = listOf("Standard", "High", "Ultra 4K")

    fun triggerPromptGeneration() {
        if (sceneInput.isBlank()) {
            sceneInput = "A serene mountain village at sunrise, with a small wooden house, misty valley, and a person holding a cup of tea"
        }
        isGenerating = true
        generatedResult = ""
        thinkingProcess = ""
        isCopied = false
        isSaved = false

        scope.launch {
            val systemInstruction = "You are a master cinematic director and prompt engineer. Create an exhaustive scene-by-scene prompt including: Location, Characters, Action, Camera, Lens, Lighting, Composition, Motion, Environment, Dialogue, Negative prompt, Continuity notes."
            val fullPrompt = "Mode: $selectedMode\nAspect Ratio: $selectedAspectRatio\nStyle: $selectedStyle\nQuality: $selectedQuality\nIdea/Story: $sceneInput"

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
        // Header
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
                    text = "Image Generation",
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

        // Horizontal Category Pills (Mockup Screen 4: Text to Image, Scene to Scene, Image Prompt)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEach { mode ->
                    MAXChip(
                        text = mode,
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode }
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Banner Card (Scene to Scene Prompt)
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
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedMode == "Scene to Scene") "Scene to Scene Prompt" else selectedMode,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Turn your ideas into cinematic sequences.",
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

        // Input Card (Describe your scene or story...)
        item {
            Text(
                text = "Describe your scene or story...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            MAXInput(
                value = sceneInput,
                onValueChange = { sceneInput = it },
                placeholder = "(e.g. A serene mountain village at sunrise, with a small wooden house, misty valley, and a person holding a cup of tea....)",
                maxChars = 1000,
                minLines = 4,
                leadingIcon = Icons.Outlined.Image,
                testTag = "scene_prompt_input"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Aspect Ratio Pills (1:1, 9:16, 16:9, 4:3)
        item {
            Text(
                text = "Aspect Ratio",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                aspectRatios.forEach { ratio ->
                    val isSelected = selectedAspectRatio == ratio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedAspectRatio = ratio }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Mini aspect rectangle outline
                            Box(
                                modifier = Modifier
                                    .size(
                                        width = when (ratio) {
                                            "1:1" -> 16.dp
                                            "9:16" -> 11.dp
                                            "16:9" -> 20.dp
                                            else -> 18.dp
                                        },
                                        height = when (ratio) {
                                            "1:1" -> 16.dp
                                            "9:16" -> 18.dp
                                            "16:9" -> 11.dp
                                            else -> 14.dp
                                        }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ratio,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Style & Quality Selectors
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Style Selector
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Style",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        MAXCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            onClick = { isStyleMenuOpen = true }
                        ) {
                            Text(
                                text = selectedStyle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                maxLines = 1
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isStyleMenuOpen,
                        onDismissRequest = { isStyleMenuOpen = false }
                    ) {
                        styles.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    selectedStyle = st
                                    isStyleMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Quality Selector
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Quality",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                        MAXCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 20.dp,
                            onClick = { isQualityMenuOpen = true }
                        ) {
                            Text(
                                text = selectedQuality,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isQualityMenuOpen,
                        onDismissRequest = { isQualityMenuOpen = false }
                    ) {
                        qualities.forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q) },
                                onClick = {
                                    selectedQuality = q
                                    isQualityMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Primary Action: ✨ Generate Image / Prompt
        item {
            MAXButton(
                text = if (isGenerating) "Constructing Scene..." else "✨ Generate Prompt",
                onClick = { triggerPromptGeneration() },
                isLoading = isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                testTag = "generate_prompt_button"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Generated Result Card
        if (generatedResult.isNotBlank() || isGenerating) {
            item {
                MAXResultCard(
                    title = "Cinematic Scene Specification",
                    content = if (generatedResult.isBlank() && isGenerating) "Rendering storyboard parameters..." else generatedResult,
                    thinkingText = thinkingProcess.ifBlank { null },
                    isCopied = isCopied,
                    isSaved = isSaved,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MAX-N Scene Prompt", generatedResult))
                        isCopied = true
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSave = {
                        scope.launch {
                            libraryDao.insertItem(
                                LibraryItemEntity(
                                    id = UUID.randomUUID().toString(),
                                    type = "prompt",
                                    title = if (sceneInput.isNotBlank()) sceneInput.take(40) else "Scene Prompt",
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
                        context.startActivity(Intent.createChooser(sendIntent, "Share Scene Prompt"))
                    },
                    onRegenerate = { triggerPromptGeneration() }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
