package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessageEntity
import com.example.service.CompanionVoiceState
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatRoomScreen(viewModel: ApexMainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by viewModel.chatMessages.collectAsState()
    val userNickname by viewModel.userNickname.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()

    var inputMessage by remember { mutableStateOf("") }
    var selectedAiTarget by remember { mutableStateOf<String?>(null) }
    var showTerminalSheet by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var shareToastText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TacticalObsidian
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Room Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurface)
                    .border(1.dp, TacticalBorder)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MatrixEmerald, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "#ULTRA-OPS // PRIVATE ROOM",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Socket: :8765 | S24 Ultra Local Encrypted",
                            color = TerminalTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nickname Pill
                    Box(
                        modifier = Modifier
                            .background(TacticalSurfaceElevated, RoundedCornerShape(6.dp))
                            .border(1.dp, TacticalBorder, RoundedCornerShape(6.dp))
                            .clickable { showProfileDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = userNickname,
                            color = TerminalText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Share Link Button
                    IconButton(
                        onClick = {
                            val link = "apex://room/ultra-ops?token=apex99&port=8765"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Apex Room Link", link))
                            shareToastText = "Room link copied: $link"
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Link",
                            tint = NeonAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Terminal Side Drawer Toggle
                    IconButton(
                        onClick = { showTerminalSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("open_terminal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Terminal Console",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Share confirmation notice banner
            if (shareToastText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shareToastText ?: "",
                        color = NeonAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DISMISS",
                        color = NeonAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { shareToastText = null }
                    )
                }
            }

            // AI Target Routing Pills Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ROUTE TO:",
                    color = TerminalTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                AiRouteChip(
                    name = "ALL",
                    isSelected = selectedAiTarget == null,
                    onClick = { selectedAiTarget = null }
                )
                AiRouteChip(
                    name = "COPILOT",
                    isSelected = selectedAiTarget == "COPILOT",
                    onClick = { selectedAiTarget = "COPILOT" }
                )
                AiRouteChip(
                    name = "GROK",
                    isSelected = selectedAiTarget == "GROK",
                    onClick = { selectedAiTarget = "GROK" }
                )
                AiRouteChip(
                    name = "MUSE",
                    isSelected = selectedAiTarget == "MUSE",
                    onClick = { selectedAiTarget = "MUSE" }
                )
                AiRouteChip(
                    name = "GEMINI",
                    isSelected = selectedAiTarget == "GEMINI",
                    onClick = { selectedAiTarget = "GEMINI" }
                )
            }

            // Chat Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(msg = msg, currentUserName = userNickname)
                }
            }

            // Companion Thinking Bar if active
            if (voiceState == CompanionVoiceState.THINKING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonCyan.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = NeonCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apex Companion analyzing telemetry...",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurface)
                    .border(1.dp, TacticalBorder)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            text = if (selectedAiTarget != null) "Message @$selectedAiTarget..." else "Broadcast to #ultra-ops...",
                            color = TerminalTextMuted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TacticalSurfaceElevated,
                        unfocusedContainerColor = TacticalSurfaceElevated,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TacticalBorder,
                        focusedTextColor = TerminalText,
                        unfocusedTextColor = TerminalText
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            viewModel.sendChatMessage(inputMessage, selectedAiTarget)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(NeonCyan, RoundedCornerShape(10.dp))
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Send",
                        tint = TacticalObsidian,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Terminal Side Channel Bottom Sheet
        if (showTerminalSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTerminalSheet = false },
                containerColor = TacticalObsidian,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                TerminalConsoleSheet(viewModel = viewModel)
            }
        }

        // Profile / Nickname Editor Dialog
        if (showProfileDialog) {
            ProfileEditDialog(
                currentNickname = userNickname,
                onDismiss = { showProfileDialog = false },
                onSave = { newNick ->
                    viewModel.updateUserProfile(newNick, "ic_avatar_cyber")
                    showProfileDialog = false
                }
            )
        }
    }
}

@Composable
fun AiRouteChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) NeonCyan.copy(alpha = 0.2f) else TacticalSurface,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isSelected) NeonCyan else TacticalBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) NeonCyan else TerminalTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ChatMessageBubble(msg: ChatMessageEntity, currentUserName: String) {
    val isUser = msg.senderRole == "USER"
    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))

    val roleColor = when (msg.senderRole) {
        "USER" -> NeonCyan
        "COMPANION" -> MatrixEmerald
        "COPILOT" -> ElectricViolet
        "GROK" -> NeonAmber
        "MUSE" -> Color(0xFF1877F2)
        "GEMINI" -> Color(0xFF4285F4)
        else -> TerminalTextMuted
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Text(
                text = msg.senderName,
                color = roleColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            if (msg.isBridged) {
                Text(
                    text = "[BRIDGED]",
                    color = NeonAmber,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = timeFormatted,
                color = TerminalTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) TacticalSurfaceElevated else TacticalSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) NeonCyan.copy(alpha = 0.4f) else TacticalBorder
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(
                text = msg.content,
                color = TerminalText,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun TerminalConsoleSheet(viewModel: ApexMainViewModel) {
    val history by viewModel.terminalHistory.collectAsState()
    val isExecuting by viewModel.isTerminalExecuting.collectAsState()
    var commandInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TERMINAL SIDE CHANNEL // SHIZUKU & TERMUX",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "S24U KERNEL",
                color = MatrixEmerald,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickCmdChip(label = "diag") {
                viewModel.executeTerminalCommand("getprop ro.product.model")
            }
            QuickCmdChip(label = "shizuku perm") {
                viewModel.terminalBridges.requestShizukuPermission()
            }
            QuickCmdChip(label = "shizuku:id") {
                viewModel.executeTerminalCommand("shizuku:id")
            }
            QuickCmdChip(label = "termux bridge") {
                viewModel.executeTerminalCommand("shizuku-termux:whoami")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Output Window
        Card(
            colors = CardDefaults.cardColors(containerColor = TacticalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                if (history.isEmpty()) {
                    item {
                        Text(
                            text = viewModel.terminalBridges.getDeviceDiagnostics(),
                            color = TerminalTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(history) { res ->
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = "$ ${res.command} [${res.bridgeType}]",
                                color = NeonAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (res.stdout.isNotBlank()) {
                                Text(
                                    text = res.stdout,
                                    color = TerminalText,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (res.stderr.isNotBlank()) {
                                Text(
                                    text = res.stderr,
                                    color = Color(0xFFFF5252),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Terminal Input Field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = {
                    Text(
                        text = "Enter shell or termux: command...",
                        color = TerminalTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TacticalSurfaceElevated,
                    unfocusedContainerColor = TacticalSurfaceElevated,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = TacticalBorder,
                    focusedTextColor = TerminalText,
                    unfocusedTextColor = TerminalText
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        viewModel.executeTerminalCommand(commandInput)
                        commandInput = ""
                    }
                },
                enabled = !isExecuting,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = TacticalObsidian),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("RUN", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun QuickCmdChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(TacticalSurfaceElevated, RoundedCornerShape(4.dp))
            .border(1.dp, TacticalBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = NeonCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ProfileEditDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nick by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CUSTOMIZE CALLSIGN",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                Text(
                    text = "Configure your operator handle displayed across Private Room and bridged assistants:",
                    color = TerminalText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = nick,
                    onValueChange = { nick = it },
                    label = { Text("Callsign / Nickname") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TerminalText,
                        unfocusedTextColor = TerminalText
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nick.isNotBlank()) onSave(nick) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = TacticalObsidian)
            ) {
                Text("SAVE")
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
