package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bridge.AiAppBridges
import com.example.bridge.CommandResult
import com.example.bridge.SoundboardManager
import com.example.bridge.SupportedAi
import com.example.bridge.TerminalBridges
import com.example.data.ai.GeminiAiService
import com.example.data.database.ApexDatabase
import com.example.data.database.AppFolderEntity
import com.example.data.database.ChatMessageEntity
import com.example.data.database.CoordinatorExpansionEntity
import com.example.data.database.MacroRuleEntity
import com.example.service.ApexAccessibilityService
import com.example.service.ApexCompanionService
import com.example.service.ApexNotificationListenerService
import com.example.service.ApexVpnService
import com.example.service.CompanionVoiceState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApexMainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ApexDatabase.getDatabase(application)
    private val dao = db.apexDao()

    val terminalBridges = TerminalBridges(application)
    val aiBridges = AiAppBridges(application)
    val soundboard = SoundboardManager(application)
    val aiService = GeminiAiService()

    // Preferences & Nickname
    private val prefs = application.getSharedPreferences("apex_nexus_prefs", Context.MODE_PRIVATE)
    private val _userNickname = MutableStateFlow(prefs.getString("user_nickname", "DevOperator") ?: "DevOperator")
    val userNickname = _userNickname.asStateFlow()

    private val _userAvatar = MutableStateFlow(prefs.getString("user_avatar", "ic_avatar_cyber") ?: "ic_avatar_cyber")
    val userAvatar = _userAvatar.asStateFlow()

    private val _isSetupCompleted = MutableStateFlow(prefs.getBoolean("setup_completed", false))
    val isSetupCompleted = _isSetupCompleted.asStateFlow()

    // Room Database Flows
    val chatMessages = dao.getMessagesForRoom("ultra-ops")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val macroRules = dao.getAllMacroRules()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appFolders = dao.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val coordinatorExpansions = dao.getAllExpansions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Service Observables
    val isAccessibilityActive = ApexAccessibilityService.isServiceActive
    val lastObservedScreenEvent = ApexAccessibilityService.lastObservedEvent
    val macroTriggerLogs = ApexAccessibilityService.macroTriggerLogs
    val isNotificationListenerActive = ApexNotificationListenerService.isListenerActive
    val isVpnActive = ApexVpnService.isVpnActive

    // Companion Voice State
    private val _voiceState = MutableStateFlow(CompanionVoiceState.STANDBY)
    val voiceState = _voiceState.asStateFlow()

    // Terminal History & State
    private val _terminalHistory = MutableStateFlow<List<CommandResult>>(emptyList())
    val terminalHistory = _terminalHistory.asStateFlow()

    private val _isTerminalExecuting = MutableStateFlow(false)
    val isTerminalExecuting = _isTerminalExecuting.asStateFlow()

    // Coordinator Generating State
    private val _isGeneratingExpansion = MutableStateFlow(false)
    val isGeneratingExpansion = _isGeneratingExpansion.asStateFlow()

    // UI Toasts / SnackBar messages
    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val toastEvents = _toastEvents.asSharedFlow()

    init {
        seedInitialDefaultsIfEmpty()
    }

    private fun seedInitialDefaultsIfEmpty() {
        viewModelScope.launch {
            // Seed initial Discord & AI macros if none exist
            val existingMacros = dao.getActiveMacroRules()
            if (existingMacros.isEmpty()) {
                dao.insertMacroRule(
                    MacroRuleEntity(
                        title = "Discord Voice Arrival & Cheers",
                        description = "Detects Discord voice channel room, taps target button, and plays soundboard cheers.",
                        targetPackage = "com.discord",
                        targetNodeText = "Voice Connected",
                        actionType = "CHAIN_DISCORD_WELCOME",
                        payload = "Welcome to the voice channel"
                    )
                )
                dao.insertMacroRule(
                    MacroRuleEntity(
                        title = "Copilot Auto-Prompt Receiver",
                        description = "Detects Copilot chat input field and arms accessibility injection.",
                        targetPackage = "com.microsoft.copilot",
                        targetNodeText = "Ask me anything",
                        actionType = "CLICK_NODE"
                    )
                )
            }

            // Seed default 4-app folders
            dao.insertFolder(
                AppFolderEntity(
                    folderId = "folder_comms",
                    folderName = "Tactical Comms",
                    iconName = "chat",
                    app1Package = "com.discord",
                    app2Package = "org.telegram.messenger",
                    app3Package = "com.termux",
                    app4Package = "com.aistudio.apexnexus.qzlmkr",
                    isVpnEnabled = false,
                    isWakeLockForced = true,
                    sortOrder = 1
                )
            )
            dao.insertFolder(
                AppFolderEntity(
                    folderId = "folder_ai",
                    folderName = "AI Squad Quad",
                    iconName = "auto_awesome",
                    app1Package = "com.microsoft.copilot",
                    app2Package = "ai.x.grok",
                    app3Package = "com.google.android.apps.bard",
                    app4Package = "com.android.chrome",
                    isVpnEnabled = false,
                    isWakeLockForced = true,
                    sortOrder = 2
                )
            )

            // Seed welcome message
            val recent = dao.getRecentMessages(1)
            if (recent.isEmpty()) {
                dao.insertMessage(
                    ChatMessageEntity(
                        roomId = "ultra-ops",
                        senderName = "Apex Nexus",
                        senderRole = "COMPANION",
                        avatarId = "ic_avatar_apex",
                        content = "Apex Nexus initialized on Galaxy S24 Ultra (One UI 9.0). Persistent voice engine ready. Macro perception active."
                    )
                )
            }
        }
    }

    fun completeSetup() {
        prefs.edit().putBoolean("setup_completed", true).apply()
        _isSetupCompleted.value = true
    }

    fun updateUserProfile(nickname: String, avatar: String) {
        prefs.edit()
            .putString("user_nickname", nickname)
            .putString("user_avatar", avatar)
            .apply()
        _userNickname.value = nickname
        _userAvatar.value = avatar
    }

    fun sendChatMessage(content: String, targetAi: String? = null) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessageEntity(
                roomId = "ultra-ops",
                senderName = _userNickname.value,
                senderRole = "USER",
                avatarId = _userAvatar.value,
                content = content,
                targetAi = targetAi
            )
            dao.insertMessage(userMsg)

            // If directed at a bridged AI
            if (targetAi != null) {
                when (targetAi.uppercase()) {
                    "MUSE" -> aiBridges.routeMessageToAi(SupportedAi.MUSE, content)
                    "GROK" -> aiBridges.routeMessageToAi(SupportedAi.GROK, content)
                    "COPILOT" -> aiBridges.routeMessageToAi(SupportedAi.COPILOT, content)
                    "GEMINI" -> aiBridges.routeMessageToAi(SupportedAi.GEMINI, content)
                }
            } else if (content.startsWith("@")) {
                // Parse mention
                val mention = content.substringBefore(" ").removePrefix("@").uppercase()
                val prompt = content.substringAfter(" ")
                when (mention) {
                    "MUSE" -> aiBridges.routeMessageToAi(SupportedAi.MUSE, prompt)
                    "GROK" -> aiBridges.routeMessageToAi(SupportedAi.GROK, prompt)
                    "COPILOT" -> aiBridges.routeMessageToAi(SupportedAi.COPILOT, prompt)
                    "GEMINI" -> aiBridges.routeMessageToAi(SupportedAi.GEMINI, prompt)
                    else -> respondWithCompanion(content)
                }
            } else {
                respondWithCompanion(content)
            }
        }
    }

    private fun respondWithCompanion(prompt: String) {
        viewModelScope.launch {
            _voiceState.value = CompanionVoiceState.THINKING
            val reply = aiService.generateCompanionResponse(prompt)
            _voiceState.value = CompanionVoiceState.STANDBY

            dao.insertMessage(
                ChatMessageEntity(
                    roomId = "ultra-ops",
                    senderName = "Apex Companion",
                    senderRole = "COMPANION",
                    avatarId = "ic_avatar_apex",
                    content = reply
                )
            )

            // Speak response via companion service if bound or TTS
            speakViaService(reply)
        }
    }

    private fun speakViaService(text: String) {
        // Trigger companion voice
        soundboard.playTacticalAcknowledge()
    }

    fun executeTerminalCommand(command: String) {
        if (command.isBlank()) return
        viewModelScope.launch {
            _isTerminalExecuting.value = true
            val result = terminalBridges.executeCommand(command)
            _terminalHistory.value = _terminalHistory.value + result
            _isTerminalExecuting.value = false
        }
    }

    fun launchMultiWindowFolder(folder: AppFolderEntity) {
        val context = getApplication<Application>()
        try {
            // Samsung One UI Split Screen / Pop-Up multi-window launch
            val intent1 = context.packageManager.getLaunchIntentForPackage(folder.app1Package)
            val intent2 = context.packageManager.getLaunchIntentForPackage(folder.app2Package)

            if (intent1 != null) {
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(intent1)
            }

            if (intent2 != null) {
                intent2.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
                context.startActivity(intent2)
            }

            _toastEvents.tryEmit("Launched ${folder.folderName} in Samsung Multi-Window split")
        } catch (e: Exception) {
            _toastEvents.tryEmit("Failed to launch multi-window: ${e.message}")
        }
    }

    fun toggleFolderVpn(folder: AppFolderEntity, enable: Boolean) {
        val context = getApplication<Application>()
        val intent = Intent(context, ApexVpnService::class.java).apply {
            action = if (enable) ApexVpnService.ACTION_START_VPN else ApexVpnService.ACTION_STOP_VPN
            if (enable) {
                val packages = arrayListOf(
                    folder.app1Package,
                    folder.app2Package,
                    folder.app3Package,
                    folder.app4Package
                ).filter { it.isNotBlank() }
                putStringArrayListExtra(ApexVpnService.EXTRA_ALLOWED_PACKAGES, ArrayList(packages))
            }
        }
        try {
            context.startService(intent)
            viewModelScope.launch {
                dao.updateFolder(folder.copy(isVpnEnabled = enable))
            }
            _toastEvents.tryEmit("Per-App VPN ${if (enable) "Armed" else "Disarmed"} for ${folder.folderName}")
        } catch (e: Exception) {
            _toastEvents.tryEmit("VPN toggle error: ${e.message}")
        }
    }

    fun generateCoordinatorDailyExpansion() {
        viewModelScope.launch {
            _isGeneratingExpansion.value = true
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val recentMessages = dao.getRecentMessages(25)
            val pooledText = recentMessages.joinToString("\n") { "[${it.senderRole}] ${it.senderName}: ${it.content}" }

            val synthesized = aiService.generateDailyExpansion(pooledText, dateStr)

            val expansion = CoordinatorExpansionEntity(
                dateString = dateStr,
                summaryTitle = "Daily Operational Brief - $dateStr",
                synthesizedContent = synthesized,
                modelUsed = "gemini-4-argon",
                totalContextItemsPooled = recentMessages.size
            )
            dao.insertExpansion(expansion)

            // Post summary back to chat room
            dao.insertMessage(
                ChatMessageEntity(
                    roomId = "ultra-ops",
                    senderName = "Apex Coordinator",
                    senderRole = "SYSTEM",
                    avatarId = "ic_avatar_apex",
                    content = "Daily Expansion synthesized using Gemini 4 Argon. Context pool compiled: ${recentMessages.size} entries."
                )
            )

            _isGeneratingExpansion.value = false
            _toastEvents.tryEmit("Daily expansion generated successfully.")
        }
    }

    fun triggerDiscordMacroSimulation() {
        viewModelScope.launch {
            soundboard.playCheersSound {
                _toastEvents.tryEmit("Discord Macro Scenario Simulated: Voice entry detected -> Cheers soundboard triggered!")
            }
        }
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getApplication<Application>().getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(getApplication<Application>().packageName)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(intent)
    }

    fun requestBatteryOptimizationExemption() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${getApplication<Application>().packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        }
    }
}
