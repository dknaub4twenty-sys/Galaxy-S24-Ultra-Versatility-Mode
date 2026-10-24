package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat
import com.example.ui.theme.AlertCrimson
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

@Composable
fun SetupWizardScreen(
    viewModel: ApexMainViewModel,
    onFinishSetup: () -> Unit
) {
    val context = LocalContext.current
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()
    val isNotifListenerActive by viewModel.isNotificationListenerActive.collectAsState()
    val isShizukuAvailable = viewModel.terminalBridges.isShizukuAvailable()
    val isTermuxAvailable = viewModel.terminalBridges.isTermuxAvailable()
    val isBatteryOptimized = viewModel.isBatteryOptimizationIgnored()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TacticalObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(TacticalSurfaceElevated, CircleShape)
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security HUD",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "APEX NEXUS // SETUP",
                        color = NeonCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Galaxy S24 Ultra (Android 17 / One UI 9.0)",
                        color = TerminalTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Automated permission pipeline and system service registration. Verify all nodes below before entering tactical command deck.",
                color = TerminalText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Post Notifications
                item {
                    SetupStepCard(
                        title = "1. Tactical Notification Channels",
                        description = "Enables foreground status, background companion state, and S24 Ultra persistent wake-lock notifications.",
                        isGranted = hasNotifPermission,
                        actionLabel = "Grant Notifications",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }

                // 2. Accessibility Service
                item {
                    SetupStepCard(
                        title = "2. Screen Perception & Accessibility",
                        description = "Mandatory for autonomous UI monitoring, Discord voice arrival detection, and tactile macro button replay.",
                        isGranted = isAccessibilityActive,
                        actionLabel = "Open Accessibility Settings",
                        onAction = { viewModel.openAccessibilitySettings() }
                    )
                }

                // 3. Notification Listener
                item {
                    SetupStepCard(
                        title = "3. AI Notification Bridge",
                        description = "Intercepts responses and telemetry from Copilot, Grok, Gemini, and Discord to synchronize with Private Chat Room.",
                        isGranted = isNotifListenerActive,
                        actionLabel = "Enable Listener Access",
                        onAction = { viewModel.openNotificationListenerSettings() }
                    )
                }

                // 4. Battery Optimization Exemption
                item {
                    SetupStepCard(
                        title = "4. One UI 9.0 Anti-Doze WakeLock",
                        description = "Exempts Apex Nexus from aggressive Samsung battery killing during screen-off and deep Doze intervals.",
                        isGranted = isBatteryOptimized,
                        actionLabel = "Exempt Battery",
                        onAction = { viewModel.requestBatteryOptimizationExemption() }
                    )
                }

                // 5. Terminal Environment
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorder),
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
                                    text = "5. Shell & Terminal Side Channels",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NeonCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                StatusBadge(
                                    isActive = isShizukuAvailable || isTermuxAvailable,
                                    label = if (isShizukuAvailable) "SHIZUKU READY" else if (isTermuxAvailable) "TERMUX LINKED" else "INTERNAL SHELL"
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Detects Shizuku Manager (ADB privileges) and Termux (RUN_COMMAND). If neither is installed, local sandboxed shell runner is automatically used.",
                                color = TerminalTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Launch Deck Button
            Button(
                onClick = {
                    viewModel.completeSetup()
                    onFinishSetup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("launch_command_deck_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = TacticalObsidian
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "ENTER COMMAND DECK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SetupStepCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) MatrixEmerald.copy(alpha = 0.5f) else TacticalBorder
        ),
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
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TerminalText,
                    fontFamily = FontFamily.Monospace
                )
                StatusBadge(isActive = isGranted, label = if (isGranted) "ARMED" else "PENDING")
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = TerminalTextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = actionLabel,
                        color = NeonAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean, label: String) {
    val color = if (isActive) MatrixEmerald else AlertCrimson
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
