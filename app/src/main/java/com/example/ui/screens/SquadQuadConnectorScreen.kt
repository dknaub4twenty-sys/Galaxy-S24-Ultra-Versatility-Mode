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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Hub
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.bridge.AiPackageRouter
import com.example.data.database.AiPackageEntity
import com.example.data.database.ApexDatabase
import com.example.data.database.ConnectorNodeEntity
import com.example.ui.components.TacticalConfirmDialog
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.DeepCharcoalBg
import com.example.ui.theme.DeepCharcoalElevated
import com.example.ui.theme.DeepCharcoalSurface
import com.example.ui.theme.MonospaceTextPrimary
import com.example.ui.theme.MonospaceTextSecondary
import com.example.ui.theme.NeonAcidGreen
import com.example.ui.theme.NeonMatrixEmerald
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalSlateBorder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SquadQuadConnectorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { ApexDatabase.getDatabase(context) }
    val router = remember { AiPackageRouter(context) }

    val nodes by db.apexDao().getAllConnectorNodes().collectAsState(initial = emptyList())
    val packages by db.apexDao().getAllAiPackages().collectAsState(initial = emptyList())

    var showNewConnectorDialog by remember { mutableStateOf(false) }
    var showTransmitDialog by remember { mutableStateOf(false) }
    var inspectedContent by remember { mutableStateOf<String?>(null) }

    // Confirmation states
    var pendingNodeName by remember { mutableStateOf<String?>(null) }
    var showNodeConfirm by remember { mutableStateOf(false) }

    var pendingPackageConfig by remember { mutableStateOf<Triple<String, String, Pair<String, String>>?>(null) }
    var showTransmitConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepCharcoalBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI SQUAD QUAD // NODE ROUTER",
                        color = NeonAcidGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Automated Connector Nodes & Tagged File Bundles (.txt / .zip)",
                        color = MonospaceTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = { showNewConnectorDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonAcidGreen,
                        contentColor = DeepCharcoalBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("new_connector_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW CONNECTOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Card: Transmit File Package
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalSlateBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(NeonMatrixEmerald, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PACK & TRANSMIT AI FILE BUNDLE",
                                color = NeonAcidGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { showTransmitDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyanAccent, contentColor = DeepCharcoalBg),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SEND PACKAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automates packing messages into a tagged .txt or compressed .zip with manifest, dispatches to target AI node, inspects payload, and auto-replies.",
                        color = MonospaceTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Connector Nodes Section
            Text(
                text = "ACTIVE CONNECTOR NODES (${nodes.size})",
                color = NeonAcidGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (nodes.isEmpty()) {
                    item {
                        Text(
                            text = "No connector nodes provisioned. Tap 'NEW CONNECTOR' above to automate setup.",
                            color = MonospaceTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(nodes, key = { it.nodeId }) { node ->
                        ConnectorNodeCard(node = node)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Routed Packages Section
            Text(
                text = "ROUTED PACKAGES & REPLIES (${packages.size})",
                color = CyberCyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(0.55f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (packages.isEmpty()) {
                    item {
                        Text(
                            text = "No packages routed yet. Tap 'SEND PACKAGE' to bundle and transmit.",
                            color = MonospaceTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(packages, key = { it.packageId }) { pkg ->
                        AiPackageCard(
                            pkg = pkg,
                            onInspect = {
                                scope.launch {
                                    inspectedContent = router.inspectPackageContent(pkg)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Dialog: Automated New Connector
        if (showNewConnectorDialog) {
            NewConnectorDialog(
                onDismiss = { showNewConnectorDialog = false },
                onAutomate = { name ->
                    pendingNodeName = name
                    showNewConnectorDialog = false
                    showNodeConfirm = true
                }
            )
        }

        // Confirmation: New Connector Step
        if (showNodeConfirm && pendingNodeName != null) {
            val name = pendingNodeName!!
            TacticalConfirmDialog(
                title = "Confirm Node Provisioning",
                actionPrompt = "provision automated local connector node '$name'",
                stepDetails = "1. Allocate port 8765 and register local endpoint\n2. Bind target routing tags: @copilot, @grok, @gemini, @muse\n3. Initialize on-device file package router bridge",
                onConfirm = {
                    scope.launch {
                        router.automateNewConnectorNode(name)
                        pendingNodeName = null
                        showNodeConfirm = false
                    }
                },
                onCancel = {
                    pendingNodeName = null
                    showNodeConfirm = false
                }
            )
        }

        // Dialog: Pack & Send
        if (showTransmitDialog) {
            TransmitPackageDialog(
                onDismiss = { showTransmitDialog = false },
                onTransmit = { src, tgt, fmt, msg ->
                    pendingPackageConfig = Triple(src, tgt, Pair(fmt, msg))
                    showTransmitDialog = false
                    showTransmitConfirm = true
                }
            )
        }

        // Confirmation: Transmit Package Step
        if (showTransmitConfirm && pendingPackageConfig != null) {
            val (src, tgt, pair) = pendingPackageConfig!!
            val (fmt, msg) = pair
            TacticalConfirmDialog(
                title = "Confirm Package Transmission",
                actionPrompt = "bundle message into a $fmt package from $src and route to $tgt",
                stepDetails = "1. Generate $fmt container with target tag header\n2. Route payload through connector node\n3. Inspect packet at recipient node and trigger auto-response",
                onConfirm = {
                    scope.launch {
                        router.createAndRoutePackage(src, tgt, msg, fmt)
                        pendingPackageConfig = null
                        showTransmitConfirm = false
                    }
                },
                onCancel = {
                    pendingPackageConfig = null
                    showTransmitConfirm = false
                }
            )
        }

        // Inspect Package Content Modal
        if (inspectedContent != null) {
            AlertDialog(
                onDismissRequest = { inspectedContent = null },
                containerColor = DeepCharcoalSurface,
                title = {
                    Text("PACKAGE PAYLOAD INSPECTOR", color = NeonAcidGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                },
                text = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepCharcoalElevated),
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    ) {
                        Text(
                            text = inspectedContent ?: "",
                            color = MonospaceTextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { inspectedContent = null },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAcidGreen, contentColor = DeepCharcoalBg)
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }
}

@Composable
fun ConnectorNodeCard(node: ConnectorNodeEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCharcoalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalSlateBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = NeonAcidGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(node.nodeName, color = MonospaceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Text("STATUS: ${node.status}", color = NeonMatrixEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Endpoint: ${node.endpointUrl}", color = MonospaceTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Active Tags: ${node.activeAiTargets}", color = CyberCyanAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun AiPackageCard(pkg: AiPackageEntity, onInspect: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCharcoalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalSlateBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (pkg.format == "ZIP") Icons.Default.Archive else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (pkg.format == "ZIP") TacticalAmber else CyberCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${pkg.sourceAi} -> ${pkg.targetAi} [${pkg.format}]", color = MonospaceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                OutlinedButton(
                    onClick = onInspect,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalSlateBorder)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(12.dp), tint = MonospaceTextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("INSPECT", fontSize = 9.sp, color = MonospaceTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(pkg.payloadSummary, color = MonospaceTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(pkg.responsePayload, color = NeonMatrixEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun NewConnectorDialog(onDismiss: () -> Unit, onAutomate: (String) -> Unit) {
    var name by remember { mutableStateOf("Node-Squad-Alpha") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepCharcoalSurface,
        title = {
            Text("AUTOMATE NEW CONNECTOR NODE", color = NeonAcidGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        },
        text = {
            Column {
                Text("Configure connector node identifier to automatically link local AIs:", color = MonospaceTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MonospaceTextPrimary, unfocusedTextColor = MonospaceTextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAutomate(name) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonAcidGreen, contentColor = DeepCharcoalBg)
            ) {
                Text("PROVISION (CONFIRM)", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MonospaceTextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
fun TransmitPackageDialog(
    onDismiss: () -> Unit,
    onTransmit: (String, String, String, String) -> Unit
) {
    var sourceAi by remember { mutableStateOf("@copilot") }
    var targetAi by remember { mutableStateOf("@grok") }
    var format by remember { mutableStateOf("TXT") }
    var payloadMessage by remember { mutableStateOf("Coordination Directive: Share telemetry report for current cycle.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepCharcoalSurface,
        title = {
            Text("PACK & TRANSMIT AI FILE BUNDLE", color = NeonAcidGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SOURCE AI TAG:", color = MonospaceTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("@copilot", "@gemini").forEach { tag ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sourceAi == tag, onClick = { sourceAi = tag }, colors = RadioButtonDefaults.colors(selectedColor = NeonAcidGreen))
                            Text(tag, color = MonospaceTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Text("TARGET RECIPIENT TAG:", color = MonospaceTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("@grok", "@muse").forEach { tag ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = targetAi == tag, onClick = { targetAi = tag }, colors = RadioButtonDefaults.colors(selectedColor = NeonAcidGreen))
                            Text(tag, color = MonospaceTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Text("PACKAGE FORMAT:", color = MonospaceTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TXT", "ZIP").forEach { fmt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = format == fmt, onClick = { format = fmt }, colors = RadioButtonDefaults.colors(selectedColor = NeonAcidGreen))
                            Text(fmt, color = MonospaceTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                OutlinedTextField(
                    value = payloadMessage,
                    onValueChange = { payloadMessage = it },
                    label = { Text("Payload Message") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = MonospaceTextPrimary, unfocusedTextColor = MonospaceTextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (payloadMessage.isNotBlank()) onTransmit(sourceAi, targetAi, format, payloadMessage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonAcidGreen, contentColor = DeepCharcoalBg)
            ) {
                Text("TRANSMIT (CONFIRM)", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MonospaceTextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    )
}
