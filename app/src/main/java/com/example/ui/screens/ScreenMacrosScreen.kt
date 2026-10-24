package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MacroRuleEntity
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextMuted
import com.example.ui.viewmodel.ApexMainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScreenMacrosScreen(viewModel: ApexMainViewModel) {
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val lastObservedEvent by viewModel.lastObservedScreenEvent.collectAsState()
    val macroRules by viewModel.macroRules.collectAsState()

    var showAddMacroDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TacticalObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCREEN PERCEPTION // MACROS",
                        color = NeonCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MediaProjection + Accessibility Node Inspector",
                        color = TerminalTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                StatusBadge(
                    isActive = isAccessibilityActive,
                    label = if (isAccessibilityActive) "HOOK ARMED" else "OFFLINE"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Perception Feed Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isAccessibilityActive) MatrixEmerald else Color.Gray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE ACTIVE WINDOW OBSERVER",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val obs = lastObservedEvent
                    if (obs != null) {
                        Text(
                            text = "Package: ${obs.packageName}",
                            color = TerminalText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Class: ${obs.className.substringAfterLast('.')}",
                            color = TerminalTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (obs.detectedText.isNotEmpty()) {
                            Text(
                                text = "Detected UI Text: \"${obs.detectedText.joinToString(", ").take(80)}\"",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = "Awaiting window focus change or app launch to inspect nodes...",
                            color = TerminalTextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Scenario: Discord Channel Entry Card
            Card(
                colors = CardDefaults.cardColors(containerColor = TacticalSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCENARIO: DISCORD VOICE ENTRY",
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = ElectricViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "1. Detects user entry into Discord voice channel\n2. Taps target UI element configured in setup\n3. Triggers synthesized Soundboard 'Cheers' fanfare and companion voice welcome.",
                        color = TerminalText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.triggerDiscordMacroSimulation() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_discord_macro_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEST SIMULATE SEQUENCE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action / Reaction Macro Rules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONFIGURED MACRO RULES (${macroRules.size})",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedButton(
                    onClick = { showAddMacroDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Rule",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW RULE",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(macroRules, key = { it.id }) { rule ->
                    MacroRuleCard(rule = rule)
                }
            }
        }

        if (showAddMacroDialog) {
            AddMacroDialog(
                onDismiss = { showAddMacroDialog = false },
                onAdd = { title, pkg, text, action ->
                    showAddMacroDialog = false
                }
            )
        }
    }
}

@Composable
fun MacroRuleCard(rule: MacroRuleEntity) {
    val lastTrigger = if (rule.lastTriggeredAt > 0) {
        SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(rule.lastTriggeredAt))
    } else "Never"

    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rule.title,
                    color = TerminalText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (rule.isEnabled) "ACTIVE" else "DISABLED",
                    color = if (rule.isEnabled) MatrixEmerald else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = rule.description,
                color = TerminalTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target: ${rule.targetPackage.ifBlank { "Any" }}",
                    color = NeonAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Triggers: ${rule.triggerCount} | Last: $lastTrigger",
                    color = TerminalTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AddMacroDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetPkg by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("CHAIN_DISCORD_WELCOME") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CREATE SCREEN MACRO",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Macro Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetPkg,
                    onValueChange = { targetPkg = it },
                    label = { Text("Target Package (e.g. com.discord)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Node Text Substring") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, targetPkg, targetText, actionType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = TacticalObsidian)
            ) {
                Text("SAVE MACRO")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TerminalTextMuted)
            }
        },
        containerColor = TacticalSurface
    )
}
