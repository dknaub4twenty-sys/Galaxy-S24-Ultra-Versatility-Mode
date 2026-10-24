package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.data.database.ApexDatabase
import com.example.data.database.ChatMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ApexNotificationBridge"

data class CapturedAiNotification(
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ApexNotificationListenerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val _isListenerActive = MutableStateFlow(false)
        val isListenerActive = _isListenerActive.asStateFlow()

        private val _capturedNotifications = MutableSharedFlow<CapturedAiNotification>(extraBufferCapacity = 64)
        val capturedNotifications = _capturedNotifications.asSharedFlow()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListenerActive.value = true
        Log.i(TAG, "ApexNotificationListenerService connected.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListenerActive.value = false
        Log.w(TAG, "ApexNotificationListenerService disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val pkgName = sbn.packageName.orEmpty()
        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        if (text.isBlank()) return

        // Filter for target bridged assistants and chat platforms
        val isTargetAssistant = when {
            pkgName.contains("copilot", ignoreCase = true) -> true
            pkgName.contains("grok", ignoreCase = true) || pkgName.contains("twitter", ignoreCase = true) -> true
            pkgName.contains("bard", ignoreCase = true) || pkgName.contains("gemini", ignoreCase = true) -> true
            pkgName.contains("discord", ignoreCase = true) -> true
            else -> false
        }

        if (isTargetAssistant) {
            val captured = CapturedAiNotification(
                packageName = pkgName,
                title = title,
                text = text
            )
            scope.launch {
                _capturedNotifications.emit(captured)

                // Also inject into Room database as a bridged message for the coordinator to pool
                val roleName = when {
                    pkgName.contains("copilot") -> "COPILOT"
                    pkgName.contains("grok") || pkgName.contains("twitter") -> "GROK"
                    pkgName.contains("bard") || pkgName.contains("gemini") -> "GEMINI"
                    else -> "BRIDGED_APP"
                }

                val db = ApexDatabase.getDatabase(applicationContext)
                db.apexDao().insertMessage(
                    ChatMessageEntity(
                        roomId = "ultra-ops",
                        senderName = if (title.isNotBlank()) title else roleName,
                        senderRole = roleName,
                        avatarId = "avatar_${roleName.lowercase()}",
                        content = text,
                        isBridged = true,
                        targetAi = roleName
                    )
                )
            }
        }
    }
}
