package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.bridge.SoundboardManager
import com.example.data.database.ApexDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ApexAccessibility"

data class ObservedScreenEvent(
    val packageName: String,
    val className: String,
    val detectedText: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

class ApexAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var soundboard: SoundboardManager? = null

    companion object {
        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        private val _lastObservedEvent = MutableStateFlow<ObservedScreenEvent?>(null)
        val lastObservedEvent = _lastObservedEvent.asStateFlow()

        private val _macroTriggerLogs = MutableSharedFlow<String>(extraBufferCapacity = 64)
        val macroTriggerLogs = _macroTriggerLogs.asSharedFlow()

        @Volatile
        var instance: ApexAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        soundboard = SoundboardManager(applicationContext)
        Log.i(TAG, "ApexAccessibilityService connected and armed.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString().orEmpty()
        val clsName = event.className?.toString().orEmpty()
        val textList = event.text.map { it.toString() }

        if (pkgName.isNotBlank() && (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)) {
            val observed = ObservedScreenEvent(
                packageName = pkgName,
                className = clsName,
                detectedText = textList
            )
            _lastObservedEvent.value = observed

            // Check against active macros in database
            checkAndExecuteMacros(pkgName)
        }
    }

    private fun checkAndExecuteMacros(currentPackage: String) {
        val rootNode = rootInActiveWindow ?: return
        scope.launch {
            try {
                val db = ApexDatabase.getDatabase(applicationContext)
                val activeMacros = db.apexDao().getActiveMacroRules()

                for (macro in activeMacros) {
                    if (macro.targetPackage.isNotBlank() && !currentPackage.contains(macro.targetPackage, ignoreCase = true)) {
                        continue
                    }

                    // Locate matching node
                    val matchingNodes = if (macro.targetViewId.isNotBlank()) {
                        rootNode.findAccessibilityNodeInfosByViewId(macro.targetViewId)
                    } else if (macro.targetNodeText.isNotBlank()) {
                        rootNode.findAccessibilityNodeInfosByText(macro.targetNodeText)
                    } else {
                        emptyList()
                    }

                    if (!matchingNodes.isNullOrEmpty()) {
                        val targetNode = matchingNodes.firstOrNull { it.isClickable } ?: matchingNodes.firstOrNull()
                        if (targetNode != null) {
                            executeMacroAction(macro.id, macro.title, macro.actionType, macro.payload, targetNode)
                            db.apexDao().recordMacroTrigger(macro.id)
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing macro pipeline", e)
            }
        }
    }

    private fun executeMacroAction(
        macroId: Long,
        title: String,
        actionType: String,
        payload: String,
        targetNode: AccessibilityNodeInfo
    ) {
        scope.launch {
            _macroTriggerLogs.emit("Triggered Macro: '$title' on node [${targetNode.text ?: targetNode.viewIdResourceName}]")

            when (actionType) {
                "CLICK_NODE" -> {
                    targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    _macroTriggerLogs.emit("Action performed: CLICK_NODE")
                }
                "SOUNDBOARD_CHEERS" -> {
                    soundboard?.playCheersSound()
                    _macroTriggerLogs.emit("Action performed: SOUNDBOARD_CHEERS played")
                }
                "CHAIN_DISCORD_WELCOME" -> {
                    // Scenario: Discord voice room entry -> tap target button -> soundboard cheers
                    targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    _macroTriggerLogs.emit("Step 1: Discord target button tapped")
                    delay(350)
                    soundboard?.playCheersSound()
                    _macroTriggerLogs.emit("Step 2: Cheers soundboard triggered")
                }
                else -> {
                    targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "ApexAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
        soundboard?.release()
        soundboard = null
        Log.i(TAG, "ApexAccessibilityService destroyed.")
    }
}
