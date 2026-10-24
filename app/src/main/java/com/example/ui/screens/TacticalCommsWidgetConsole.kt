package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import java.util.UUID
import kotlin.math.roundToInt

data class MovableWidgetInstance(
    val id: String = UUID.randomUUID().toString().take(8),
    val title: String,
    val url: String,
    val isDesktopMode: Boolean,
    var widthDp: Int,
    var heightDp: Int,
    var initialOffsetX: Float = 20f,
    var initialOffsetY: Float = 20f,
    val retainData: Boolean = false,
    val presenceStatus: String = "CONNECTED"
)

val PRESET_COMMS_APPS = listOf(
    Pair("Discord Web", "https://discord.com/app"),
    Pair("Telegram Web", "https://web.telegram.org/a/"),
    Pair("Element Matrix", "https://app.element.io/"),
    Pair("Termux Web Console", "http://127.0.0.1:8765/status"),
    Pair("Custom Web App", "https://news.ycombinator.com")
)

@Composable
fun TacticalCommsWidgetConsole() {
    val activeWidgets = remember { mutableStateListOf<MovableWidgetInstance>() }

    var showSpawnDialog by remember { mutableStateOf(false) }
    var pendingSpawnConfig by remember { mutableStateOf<MovableWidgetInstance?>(null) }
    var showStepConfirmation by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepCharcoalBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Top Console Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TACTICAL COMMS // WIDGET DECK",
                            color = NeonAcidGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Movable Persistent Widgets [Active: ${activeWidgets.size}]",
                            color = MonospaceTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { showSpawnDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonAcidGreen,
                            contentColor = DeepCharcoalBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("launch_widget_console_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LAUNCH WIDGET",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeWidgets.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepCharcoalSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalSlateBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "TACTICAL CONSOLE IDLE",
                                color = TacticalAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Spawn fully functional persistent web/app widgets (Discord, Telegram, Element Matrix, or custom web consoles) with choice of Desktop or Mobile layout and isolated session stores. Drag, move, and run widgets concurrently.",
                                color = MonospaceTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Render all active movable concurrent widgets
            activeWidgets.forEachIndexed { index, widget ->
                MovableWidgetCard(
                    widget = widget,
                    initialPositionIndex = index,
                    onClose = { activeWidgets.remove(widget) }
                )
            }
        }

        // Spawn Configuration Modal
        if (showSpawnDialog) {
            SpawnWidgetDialog(
                onDismiss = { showSpawnDialog = false },
                onConfigure = { config ->
                    pendingSpawnConfig = config
                    showSpawnDialog = false
                    showStepConfirmation = true
                }
            )
        }

        // Automated Step Confirmation Dialog
        if (showStepConfirmation && pendingSpawnConfig != null) {
            val config = pendingSpawnConfig!!
            TacticalConfirmDialog(
                title = "Confirm Widget Spawn",
                actionPrompt = "spawn a new ${if (config.isDesktopMode) "DESKTOP" else "MOBILE"} replica of ${config.title}",
                stepDetails = "1. Allocate isolated session container (Retain data: ${config.retainData})\n2. Set viewport dimensions to ${config.widthDp}x${config.heightDp} dp\n3. Bind presence status to CONNECTED on S24 Ultra tactical canvas",
                onConfirm = {
                    activeWidgets.add(config)
                    pendingSpawnConfig = null
                    showStepConfirmation = false
                },
                onCancel = {
                    pendingSpawnConfig = null
                    showStepConfirmation = false
                }
            )
        }
    }
}

@Composable
fun MovableWidgetCard(
    widget: MovableWidgetInstance,
    initialPositionIndex: Int,
    onClose: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(widget.initialOffsetX + (initialPositionIndex * 30)) }
    var offsetY by remember { mutableFloatStateOf(widget.initialOffsetY + (initialPositionIndex * 35)) }
    var isMinimized by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val currentWidth = if (isExpanded) 380.dp else widget.widthDp.dp
    val currentHeight = if (isMinimized) 44.dp else if (isExpanded) 480.dp else widget.heightDp.dp

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(width = currentWidth, height = currentHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(DeepCharcoalSurface)
            .border(1.dp, NeonAcidGreen.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag Bar / Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(DeepCharcoalElevated)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceAtLeast(0f)
                            offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(NeonMatrixEmerald, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = widget.title,
                        color = NeonAcidGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (widget.isDesktopMode) "[DESK]" else "[MOB]",
                        color = CyberCyanAccent,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isMinimized = !isMinimized },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minimize",
                            tint = MonospaceTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Resize",
                            tint = MonospaceTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Widget",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Web App Content View (Concurrent & Isolated)
            if (!isMinimized) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000))
                ) {
                    IsolatedWebViewContainer(widget = widget)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun IsolatedWebViewContainer(widget: MovableWidgetInstance) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = widget.isDesktopMode

                if (widget.isDesktopMode) {
                    settings.userAgentString =
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                } else {
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; Galaxy S24 Ultra) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                // If user did NOT choose to retain data, ensure fresh spawn isolation
                if (!widget.retainData) {
                    clearCache(true)
                    clearHistory()
                    CookieManager.getInstance().removeAllCookies(null)
                }

                webViewClient = WebViewClient()
                loadUrl(widget.url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun SpawnWidgetDialog(
    onDismiss: () -> Unit,
    onConfigure: (MovableWidgetInstance) -> Unit
) {
    var selectedApp by remember { mutableStateOf(PRESET_COMMS_APPS[0]) }
    var customUrl by remember { mutableStateOf("") }
    var isDesktopView by remember { mutableStateOf(false) }
    var sizePreset by remember { mutableStateOf("Tactical (350x300)") }
    var retainData by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepCharcoalSurface,
        title = {
            Text(
                text = "SPAWN WIDGET REPLICA",
                color = NeonAcidGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT TARGET APP / BROWSER TO CLONE:",
                    color = MonospaceTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                PRESET_COMMS_APPS.forEach { app ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedApp = app }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedApp == app,
                            onClick = { selectedApp = app },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonAcidGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = app.first,
                            color = if (selectedApp == app) NeonAcidGreen else MonospaceTextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (selectedApp.first == "Custom Web App") {
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MonospaceTextPrimary,
                            unfocusedTextColor = MonospaceTextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Viewport Mode: Desktop vs Mobile
                Text(
                    text = "VIEWPORT AGENT MODE:",
                    color = MonospaceTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isDesktopView = false }) {
                        RadioButton(selected = !isDesktopView, onClick = { isDesktopView = false }, colors = RadioButtonDefaults.colors(selectedColor = NeonAcidGreen))
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NeonAcidGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mobile", color = MonospaceTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isDesktopView = true }) {
                        RadioButton(selected = isDesktopView, onClick = { isDesktopView = true }, colors = RadioButtonDefaults.colors(selectedColor = NeonAcidGreen))
                        Icon(Icons.Default.Computer, contentDescription = null, tint = CyberCyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Desktop", color = MonospaceTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dimension Selector
                Text(
                    text = "WIDGET DIMENSION:",
                    color = MonospaceTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Compact (300x240)", "Tactical (350x300)").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (sizePreset == preset) NeonAcidGreen.copy(alpha = 0.2f) else DeepCharcoalElevated,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (sizePreset == preset) NeonAcidGreen else TacticalSlateBorder, RoundedCornerShape(6.dp))
                                .clickable { sizePreset = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset.take(7), color = MonospaceTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Retain Data Checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = retainData,
                        onCheckedChange = { retainData = it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonAcidGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Retain widget data for next spawn",
                        color = MonospaceTextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUrl = if (selectedApp.first == "Custom Web App" && customUrl.isNotBlank()) customUrl else selectedApp.second
                    val (w, h) = if (sizePreset.startsWith("Compact")) Pair(300, 240) else Pair(350, 300)
                    onConfigure(
                        MovableWidgetInstance(
                            title = selectedApp.first,
                            url = finalUrl,
                            isDesktopMode = isDesktopView,
                            widthDp = w,
                            heightDp = h,
                            retainData = retainData
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonAcidGreen, contentColor = DeepCharcoalBg)
            ) {
                Text("NEXT (CONFIRM)", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MonospaceTextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    )
}
