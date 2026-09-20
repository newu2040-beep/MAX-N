package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProviderService
import com.example.data.ai.AIRequest
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.preferences.UserSettings
import com.example.ui.components.MAXCard
import com.example.ui.components.MAXStarMark
import com.example.ui.theme.CodeBlockBg
import com.example.ui.theme.CodeBlockText
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@Composable
fun ChatScreen(
    settings: UserSettings,
    aiService: AIProviderService,
    database: AppDatabase,
    initialPrompt: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var activeConversationId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var conversationTitle by remember { mutableStateOf("New Chat") }
    var isPinned by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var currentStreamingText by remember { mutableStateOf("") }
    var currentThinkingText by remember { mutableStateOf("") }

    var isModelMenuOpen by remember { mutableStateOf(false) }
    var isChatMenuOpen by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(settings.provider.defaultModel) }

    // Uploaded files attachments state
    var attachedFiles by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // Pair(Name, summary/type)

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newFiles = uris.mapNotNull { uri ->
                var name = "Document"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        name = cursor.getString(nameIndex)
                    }
                }
                name to "File"
            }
            attachedFiles = attachedFiles + newFiles
            Toast.makeText(context, "${uris.size} file(s) attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo/Gallery picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newImages = uris.mapIndexed { index, _ ->
                "Image_${System.currentTimeMillis().toString().takeLast(4)}_$index.jpg" to "Image"
            }
            attachedFiles = attachedFiles + newImages
            Toast.makeText(context, "${uris.size} image(s) attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for notifications & storage
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    // TextToSpeech initialization
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Load active conversation pinned status
    LaunchedEffect(activeConversationId) {
        val conv = database.conversationDao().getConversationById(activeConversationId)
        if (conv != null) {
            isPinned = conv.isPinned
            conversationTitle = conv.title
        } else {
            isPinned = false
        }
    }

    // Collect messages from Room DB for active conversation
    val messagesFlow = remember(activeConversationId) {
        database.chatMessageDao().getMessagesForConversation(activeConversationId)
    }
    val messages by messagesFlow.collectAsState(initial = emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank() || isStreaming) return
        
        // Append attachment summary if any
        val attachmentsPrompt = if (attachedFiles.isNotEmpty()) {
            val fileNames = attachedFiles.joinToString(", ") { it.first }
            "\n\n[Attached Files: $fileNames]"
        } else ""

        val userMsgText = (text + attachmentsPrompt).trim()
        inputText = ""
        attachedFiles = emptyList()

        val userMessage = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = activeConversationId,
            role = "user",
            content = userMsgText,
            timestamp = System.currentTimeMillis()
        )

        scope.launch {
            // Ensure conversation exists in DB
            if (messages.isEmpty()) {
                val title = if (userMsgText.length > 28) userMsgText.take(28) + "..." else userMsgText
                conversationTitle = title
                database.conversationDao().insertConversation(
                    ConversationEntity(
                        id = activeConversationId,
                        title = title,
                        lastMessage = userMsgText,
                        model = selectedModel,
                        isPinned = isPinned
                    )
                )
            }

            database.chatMessageDao().insertMessage(userMessage)

            // Prepare AI Request with context
            val history = messages.map { it.role to it.content }
            val request = AIRequest(
                prompt = userMsgText,
                systemInstruction = settings.systemInstruction + 
                    (if (settings.userPersonality.isNotBlank()) "\nUser Preferred Tone/Personality: ${settings.userPersonality}" else ""),
                model = selectedModel,
                isThinkingEnabled = settings.isThinkingModeEnabled,
                contextHistory = history
            )

            isStreaming = true
            currentStreamingText = ""
            currentThinkingText = ""

            val assistantMsgId = UUID.randomUUID().toString()

            aiService.streamContent(request, settings).collect { chunk ->
                if (chunk.thinkingText != null) {
                    currentThinkingText += chunk.thinkingText
                }
                if (chunk.text.isNotBlank()) {
                    currentStreamingText += chunk.text
                }
                if (chunk.isDone) {
                    isStreaming = false
                    val assistantMessage = ChatMessageEntity(
                        id = assistantMsgId,
                        conversationId = activeConversationId,
                        role = "assistant",
                        content = currentStreamingText,
                        model = selectedModel,
                        isThinking = settings.isThinkingModeEnabled,
                        thinkingProcess = currentThinkingText.ifBlank { null }
                    )
                    database.chatMessageDao().insertMessage(assistantMessage)
                    database.conversationDao().updateConversation(
                        ConversationEntity(
                            id = activeConversationId,
                            title = conversationTitle,
                            updatedAt = System.currentTimeMillis(),
                            lastMessage = currentStreamingText.take(40),
                            model = selectedModel,
                            isPinned = isPinned
                        )
                    )
                    currentStreamingText = ""
                    currentThinkingText = ""
                }
            }
        }
    }

    // Auto-scroll when messages update
    LaunchedEffect(messages.size, currentStreamingText) {
        if (messages.isNotEmpty() || currentStreamingText.isNotEmpty()) {
            listState.animateScrollToItem((messages.size + if (isStreaming) 1 else 0).coerceAtLeast(0))
        }
    }

    // Handle initial prompt if passed from home screen
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank() && messages.isEmpty() && !isStreaming) {
            sendMessage(initialPrompt)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // Model Switcher Chip
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { isModelMenuOpen = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MAXStarMark(size = 14.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedModel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DropdownMenu(
                    expanded = isModelMenuOpen,
                    onDismissRequest = { isModelMenuOpen = false }
                ) {
                    listOf("gemini-3.5-flash", "gemini-3.1-pro", "gpt-4o", "claude-3-5-sonnet", "deepseek-chat").forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                selectedModel = model
                                isModelMenuOpen = false
                            }
                        )
                    }
                }
            }

            // Actions group: Pin, Share/Link, and New Chat `+`
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pin Chat Action
                IconButton(
                    onClick = {
                        isPinned = !isPinned
                        scope.launch {
                            val existing = database.conversationDao().getConversationById(activeConversationId)
                            if (existing != null) {
                                database.conversationDao().updateConversation(existing.copy(isPinned = isPinned))
                            }
                        }
                        Toast.makeText(
                            context,
                            if (isPinned) "Chat pinned to top" else "Chat unpinned",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin Chat",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Chat Menu (Copy Link, Share)
                Box {
                    IconButton(
                        onClick = { isChatMenuOpen = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isChatMenuOpen,
                        onDismissRequest = { isChatMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Link to Chat") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                isChatMenuOpen = false
                                val shareLink = "https://maxn.ai/chat/$activeConversationId"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Chat Link", shareLink))
                                Toast.makeText(context, "Chat link copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share to Any Platform") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                isChatMenuOpen = false
                                val allConversationText = messages.joinToString("\n\n") {
                                    "${if (it.role == "user") "You" else "MAX-N"}: ${it.content}"
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "MAX-N AI Workspace Conversation:\n\n$allConversationText\n\nLink: https://maxn.ai/chat/$activeConversationId")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Chat via"))
                            }
                        )
                    }
                }

                // New Chat `+` button
                IconButton(
                    onClick = {
                        activeConversationId = UUID.randomUUID().toString()
                        conversationTitle = "New Chat"
                        currentStreamingText = ""
                        currentThinkingText = ""
                        isPinned = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (messages.isEmpty() && !isStreaming) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MAXStarMark(size = 36.dp, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "How can MAX-N help you today?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ask questions, write documents, generate code, or solve complex tasks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MAX-N Message", msg.content))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, msg.content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Message"))
                    },
                    onSpeak = {
                        tts?.speak(msg.content, TextToSpeech.QUEUE_FLUSH, null, null)
                    },
                    onRegenerate = {
                        sendMessage(msg.content)
                    },
                    onEdit = {
                        inputText = msg.content
                        Toast.makeText(context, "Loaded message into input for editing", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Live streaming bubble
            if (isStreaming) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentThinkingText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentThinkingText.takeLast(120),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (currentStreamingText.isNotBlank()) {
                            MAXCard(
                                modifier = Modifier.fillMaxWidth(0.95f),
                                cornerRadius = 20.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = currentStreamingText,
                                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // Bottom Input Card with File Attachment Tray
        MAXCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                // Attached files chips
                if (attachedFiles.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(attachedFiles) { (fileName, fileType) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (fileType == "Image") Icons.Outlined.Image else Icons.Outlined.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = fileName.take(16),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable {
                                                attachedFiles = attachedFiles.filterNot { it.first == fileName }
                                            },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment Action with Dropdown
                    var isAttachMenuOpen by remember { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { isAttachMenuOpen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AttachFile,
                                contentDescription = "Attach File",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = isAttachMenuOpen,
                            onDismissRequest = { isAttachMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Upload Documents / Files") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    isAttachMenuOpen = false
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Choose from Gallery") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    isAttachMenuOpen = false
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Request System Permissions") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    isAttachMenuOpen = false
                                    permissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.POST_NOTIFICATIONS,
                                            android.Manifest.permission.READ_MEDIA_IMAGES
                                        )
                                    )
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask MAX-N...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        maxLines = 4
                    )

                    // Send Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() || attachedFiles.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { sendMessage(inputText) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(16.dp),
                            tint = if (inputText.isNotBlank() || attachedFiles.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSpeak: () -> Unit,
    onRegenerate: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit message before sending",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy message",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            MAXCard(
                modifier = Modifier.fillMaxWidth(0.95f),
                cornerRadius = 22.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Thinking section if present
                    if (!message.thinkingProcess.isNullOrBlank()) {
                        var showThinking by remember { mutableStateOf(false) }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showThinking = !showThinking }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showThinking) "Hide Reasoning" else "Thought Process",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = showThinking) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = message.thinkingProcess,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Render markdown & formatted code blocks if code is present
                    FormattedTextContent(content = message.content)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Message Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onSpeak, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.VolumeUp,
                                contentDescription = "Listen",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Regenerate",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parses markdown code blocks (```code```) and renders formatted code cards with one-tap copy
 */
@Composable
fun FormattedTextContent(content: String) {
    val context = LocalContext.current
    val parts = content.split("```")

    Column {
        for (i in parts.indices) {
            val part = parts[i]
            if (i % 2 == 1) {
                // Code block
                val lines = part.trim().lines()
                val lang = lines.firstOrNull()?.trim() ?: "code"
                val code = lines.drop(1).joinToString("\n")

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CodeBlockBg,
                    contentColor = CodeBlockText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFA2A098)
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy code",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFA2A098)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = code.ifBlank { part.trim() },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            color = CodeBlockText
                        )
                    }
                }
            } else {
                // Regular prose
                if (part.isNotBlank()) {
                    Text(
                        text = part.trim(),
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
