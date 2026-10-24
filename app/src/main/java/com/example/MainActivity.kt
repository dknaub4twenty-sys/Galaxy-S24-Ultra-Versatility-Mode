package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ApexCompanionService
import com.example.service.CompanionVoiceState
import com.example.ui.screens.CoordinatorScreen
import com.example.ui.screens.MultiWindowFolderScreen
import com.example.ui.screens.PrivateChatRoomScreen
import com.example.ui.screens.ScreenMacrosScreen
import com.example.ui.screens.SetupWizardScreen
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MatrixEmerald
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TerminalText
import com.example.ui.theme.TerminalTextMuted
import com.example.diagnostic.BootLifecycleObserver
import com.example.ui.theme.CyberpunkTheme
import com.example.ui.viewmodel.ApexMainViewModel
import com.example.util.configureAppCheck
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ApexMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Boot and Preview Diagnostic Observer
        lifecycle.addObserver(BootLifecycleObserver(this))

        // 1. App Check Configuration per skill specification
        configureAppCheck(this, intent)

        // 2. Start Persistent Companion Service
        startCompanionService()

        enableEdgeToEdge()

        setContent {
            CyberpunkTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    private fun startCompanionService() {
        try {
            val serviceIntent = Intent(this, ApexCompanionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "startCompanionService bypassed: ${e.message}")
        }
    }
}

@Composable
fun MainAppContent(viewModel: ApexMainViewModel) {
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showVoicePromptDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (!isSetupCompleted) {
        SetupWizardScreen(
            viewModel = viewModel,
            onFinishSetup = { /* Handled via ViewModel state update */ }
        )
    } else {
        BackHandler(enabled = selectedTab != 0) {
            selectedTab = 0
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = TacticalObsidian,
            topBar = {
                TacticalTopStatusBar(
                    onOpenSetup = { /* Re-check setup */ }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Floating Persistent Companion Voice Bar
                    CompanionFloatingVoiceBar(
                        viewModel = viewModel,
                        onTriggerVoice = { showVoicePromptDialog = true }
                    )

                    // Navigation Deck Bar
                    NavigationBar(
                        containerColor = TacticalSurface,
                        contentColor = TerminalText,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Comms")
                            },
                            label = {
                                Text("COMMS", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_tab_comms")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(Icons.Default.Visibility, contentDescription = "Macros")
                            },
                            label = {
                                Text("MACROS", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_tab_macros")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(Icons.Default.Folder, contentDescription = "Folders")
                            },
                            label = {
                                Text("FOLDERS", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_tab_folders")
                        )

                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Coordinator")
                            },
                            label = {
                                Text("COORDINATOR", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            },
                            colors = navBarItemColors(),
                            modifier = Modifier.testTag("nav_tab_coordinator")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> PrivateChatRoomScreen(viewModel = viewModel)
                    1 -> ScreenMacrosScreen(viewModel = viewModel)
                    2 -> MultiWindowFolderScreen(viewModel = viewModel)
                    3 -> CoordinatorScreen(viewModel = viewModel)
                }
            }
        }

        if (showVoicePromptDialog) {
            VoicePromptDialog(
                onDismiss = { showVoicePromptDialog = false },
                onSubmit = { prompt ->
                    viewModel.sendChatMessage(prompt)
                    showVoicePromptDialog = false
                }
            )
        }
    }
}

@Composable
fun TacticalTopStatusBar(onOpenSetup: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TacticalObsidian)
            .border(1.dp, TacticalBorder)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MatrixEmerald, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "APEX NEXUS // S24 ULTRA HUD",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ONE UI 9.0 // KNOX 3.14",
                color = TerminalTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CompanionFloatingVoiceBar(
    viewModel: ApexMainViewModel,
    onTriggerVoice: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsState()

    val stateText = when (voiceState) {
        CompanionVoiceState.STANDBY -> "VOICE: STANDBY"
        CompanionVoiceState.LISTENING -> "VOICE: LISTENING..."
        CompanionVoiceState.THINKING -> "VOICE: THINKING..."
        CompanionVoiceState.SPEAKING -> "VOICE: SPEAKING..."
    }

    val stateColor = when (voiceState) {
        CompanionVoiceState.STANDBY -> MatrixEmerald
        CompanionVoiceState.LISTENING -> NeonCyan
        CompanionVoiceState.THINKING -> NeonAmber
        CompanionVoiceState.SPEAKING -> ElectricViolet
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TacticalSurfaceElevated)
            .border(1.dp, TacticalBorder)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(stateColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stateText,
                color = stateColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.soundboard.playCheersSound() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Test Cheers Audio",
                    tint = NeonAmber,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = onTriggerVoice,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan.copy(alpha = 0.2f),
                    contentColor = NeonCyan
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Prompt Voice",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("VOICE PROMPT", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun VoicePromptDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var voiceInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "APEX COMPANION VOICE INPUT",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column {
                Text(
                    text = "Speak or enter query for continuous companion conversation mode:",
                    color = TerminalText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = voiceInput,
                    onValueChange = { voiceInput = it },
                    placeholder = { Text("What are our current tactical objectives?") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TerminalText,
                        unfocusedTextColor = TerminalText
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (voiceInput.isNotBlank()) onSubmit(voiceInput) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = TacticalObsidian)
            ) {
                Text("SPEAK / SEND")
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

@Composable
fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = TacticalObsidian,
    selectedTextColor = NeonCyan,
    indicatorColor = NeonCyan,
    unselectedIconColor = TerminalTextMuted,
    unselectedTextColor = TerminalTextMuted
)
