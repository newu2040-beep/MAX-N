package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomToolDao
import com.example.data.local.CustomToolEntity
import com.example.ui.components.MAXStarMark
import kotlinx.coroutines.launch
import java.util.UUID

val INPUT_TYPES = listOf("Plain Text", "List / Items", "Document / Article", "Meeting Notes", "Code / Data")
val OUTPUT_FORMATS = listOf("Markdown", "Table", "Bullet Points", "Checklist", "Structured JSON", "Plain Text")
val MODEL_CHOICES = listOf("gemini-2.5-flash", "gpt-4o", "claude-3-5-sonnet-20241022", "sonar", "glm-4-flash")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomToolCreatorScreen(
    customToolDao: CustomToolDao,
    onBack: () -> Unit,
    onToolCreated: (toolId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var toolName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedInputType by remember { mutableStateOf(INPUT_TYPES[0]) }
    var selectedOutputFormat by remember { mutableStateOf(OUTPUT_FORMATS[0]) }
    var preferredModel by remember { mutableStateOf(MODEL_CHOICES[0]) }
    var saveToHome by remember { mutableStateOf(true) }

    var expandedInputType by remember { mutableStateOf(false) }
    var expandedOutputFormat by remember { mutableStateOf(false) }
    var expandedModel by remember { mutableStateOf(false) }

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("custom_tool_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                MAXStarMark(size = 20.dp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Create Custom AI Tool",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Build and save tailored AI tools for your daily workflows",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tool Name
                    OutlinedTextField(
                        value = toolName,
                        onValueChange = { toolName = it },
                        label = { Text("Tool Name") },
                        placeholder = { Text("e.g. YouTube Script Outliner, Rental Agreement Reviewer...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Explain what this tool does in 1-2 sentences...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Instructions (System Prompt)
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("AI Instructions (Prompt Template)") },
                        placeholder = {
                            Text("e.g. You are an expert lease auditor. Review the input text and identify 1) rent hike clauses, 2) subletting restrictions, 3) security deposit pitfalls...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    // Input Type Selector
                    ExposedDropdownMenuBox(
                        expanded = expandedInputType,
                        onExpandedChange = { expandedInputType = !expandedInputType }
                    ) {
                        OutlinedTextField(
                            value = selectedInputType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Input Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInputType) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedInputType,
                            onDismissRequest = { expandedInputType = false }
                        ) {
                            INPUT_TYPES.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedInputType = type
                                        expandedInputType = false
                                    }
                                )
                            }
                        }
                    }

                    // Output Format Selector
                    ExposedDropdownMenuBox(
                        expanded = expandedOutputFormat,
                        onExpandedChange = { expandedOutputFormat = !expandedOutputFormat }
                    ) {
                        OutlinedTextField(
                            value = selectedOutputFormat,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Output Format") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedOutputFormat) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedOutputFormat,
                            onDismissRequest = { expandedOutputFormat = false }
                        ) {
                            OUTPUT_FORMATS.forEach { format ->
                                DropdownMenuItem(
                                    text = { Text(format) },
                                    onClick = {
                                        selectedOutputFormat = format
                                        expandedOutputFormat = false
                                    }
                                )
                            }
                        }
                    }

                    // Preferred Model Selector
                    ExposedDropdownMenuBox(
                        expanded = expandedModel,
                        onExpandedChange = { expandedModel = !expandedModel }
                    ) {
                        OutlinedTextField(
                            value = preferredModel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferred AI Model") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedModel,
                            onDismissRequest = { expandedModel = false }
                        ) {
                            MODEL_CHOICES.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model) },
                                    onClick = {
                                        preferredModel = model
                                        expandedModel = false
                                    }
                                )
                            }
                        }
                    }

                    // Save to Home toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Save to Home Screen", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text("Pin as a 1-tap quick action on the Home dashboard", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = saveToHome, onCheckedChange = { saveToHome = it })
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (toolName.isNotBlank() && instructions.isNotBlank()) {
                                coroutineScope.launch {
                                    val id = "custom_" + UUID.randomUUID().toString().take(8)
                                    val customTool = CustomToolEntity(
                                        id = id,
                                        name = toolName.trim(),
                                        description = description.ifBlank { "Custom tool: $toolName" }.trim(),
                                        instructions = instructions.trim(),
                                        inputType = selectedInputType,
                                        outputFormat = selectedOutputFormat,
                                        preferredModel = preferredModel,
                                        isSavedToHome = saveToHome,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    customToolDao.insertCustomTool(customTool)
                                    Toast.makeText(context, "Tool '${toolName}' created!", Toast.LENGTH_SHORT).show()
                                    onToolCreated(id)
                                }
                            }
                        },
                        enabled = toolName.isNotBlank() && instructions.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create & Save Tool")
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
