package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppFolderEntity
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

@Composable
fun MultiWindowFolderScreen(viewModel: ApexMainViewModel) {
    val folders by viewModel.appFolders.collectAsState()
    val isVpnActive by viewModel.isVpnActive.collectAsState()

    var activeConsoleView by remember { mutableStateOf<String?>(null) }

    if (activeConsoleView == "folder_comms") {
        BackHandler { activeConsoleView = null }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeConsoleView = null }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BACK TO WORKSTATION FOLDERS",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            TacticalCommsWidgetConsole()
        }
    } else if (activeConsoleView == "folder_ai") {
        BackHandler { activeConsoleView = null }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeConsoleView = null }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BACK TO WORKSTATION FOLDERS",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            SquadQuadConnectorScreen()
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = TacticalObsidian
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "APP FOLDERS // WORKSTATION CONSOLE",
                            color = NeonCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tactical Comms Widgets & Squad Quad Node Routers",
                            color = TerminalTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    StatusBadge(
                        isActive = isVpnActive,
                        label = if (isVpnActive) "VPN ACTIVE" else "TUNNEL IDLE"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = TacticalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TACTICAL WORKSPACES & CONSOLES",
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Tactical Comms: Launch interactive movable web/app widgets (Desktop vs Mobile, isolated session storage).\n• AI Squad Quad: Launch local connector node and route tagged .txt/.zip packages between AIs.",
                            color = TerminalTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Workstation Folders List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(folders, key = { it.folderId }) { folder ->
                        AppFolderWorkstationCard(
                            folder = folder,
                            isVpnGloballyActive = isVpnActive,
                            onLaunchMultiWindow = {
                                if (folder.folderId == "folder_comms") {
                                    activeConsoleView = "folder_comms"
                                } else if (folder.folderId == "folder_ai") {
                                    activeConsoleView = "folder_ai"
                                } else {
                                    viewModel.launchMultiWindowFolder(folder)
                                }
                            },
                            onToggleVpn = { enable -> viewModel.toggleFolderVpn(folder, enable) },
                            onForceWakeLock = {
                                viewModel.executeTerminalCommand("dumpsys deviceidle whitelist +${folder.app1Package} && dumpsys deviceidle whitelist +${folder.app2Package}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppFolderWorkstationCard(
    folder: AppFolderEntity,
    isVpnGloballyActive: Boolean,
    onLaunchMultiWindow: () -> Unit,
    onToggleVpn: (Boolean) -> Unit,
    onForceWakeLock: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TacticalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Folder Title & App Slots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(TacticalSurfaceElevated, CircleShape)
                            .border(1.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = folder.folderName,
                            color = TerminalText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (folder.folderId == "folder_comms") "Movable Widget Deck" else if (folder.folderId == "folder_ai") "Node Package Router" else "Workstation: 4 App Slots",
                            color = TerminalTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = onLaunchMultiWindow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = TacticalObsidian
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("launch_split_console_${folder.folderId}")
                ) {
                    Text(
                        text = "LAUNCH CONSOLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Slots Grid (Up to 4 Apps)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppSlotBadge(slotIndex = 1, pkg = folder.app1Package, modifier = Modifier.weight(1f))
                AppSlotBadge(slotIndex = 2, pkg = folder.app2Package, modifier = Modifier.weight(1f))
                AppSlotBadge(slotIndex = 3, pkg = folder.app3Package, modifier = Modifier.weight(1f))
                AppSlotBadge(slotIndex = 4, pkg = folder.app4Package, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls: Per-Folder VPN & WakeLock Booster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TacticalSurfaceElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = if (folder.isVpnEnabled) MatrixEmerald else TerminalTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Per-Folder Isolated VPN",
                        color = TerminalText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = folder.isVpnEnabled,
                    onCheckedChange = onToggleVpn,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TacticalObsidian,
                        checkedTrackColor = MatrixEmerald,
                        uncheckedThumbColor = TerminalTextMuted,
                        uncheckedTrackColor = TacticalSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WakeLock Booster Button
            OutlinedButton(
                onClick = onForceWakeLock,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = NeonAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FORCE WAKELOCK & ANTI-DOZE (5 APPS)",
                    color = NeonAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AppSlotBadge(
    slotIndex: Int,
    pkg: String?,
    modifier: Modifier = Modifier
) {
    val isAssigned = !pkg.isNullOrBlank()
    val displayName = if (isAssigned) {
        pkg!!.substringAfterLast(".")
    } else {
        "SLOT $slotIndex"
    }

    Box(
        modifier = modifier
            .background(
                if (isAssigned) TacticalSurfaceElevated else TacticalSurface,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isAssigned) NeonCyan.copy(alpha = 0.5f) else TacticalBorder,
                RoundedCornerShape(6.dp)
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayName.uppercase(),
            color = if (isAssigned) NeonCyan else TerminalTextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
