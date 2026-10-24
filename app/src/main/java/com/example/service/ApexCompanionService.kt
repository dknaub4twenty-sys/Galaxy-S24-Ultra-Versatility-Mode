package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.ai.GeminiAiService
import com.example.data.network.LocalRoomServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "ApexCompanionService"
private const val NOTIFICATION_CHANNEL_ID = "apex_nexus_companion_channel"
private const val NOTIFICATION_ID = 4040

enum class CompanionVoiceState {
    STANDBY,
    LISTENING,
    THINKING,
    SPEAKING
}

class ApexCompanionService : Service(), TextToSpeech.OnInitListener {

    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var wakeLock: PowerManager.WakeLock? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(CompanionVoiceState.STANDBY)
    val voiceState = _voiceState.asStateFlow()

    private val aiService = GeminiAiService()
    var roomServer: LocalRoomServer? = null
        private set

    inner class LocalBinder : Binder() {
        fun getService(): ApexCompanionService = this@ApexCompanionService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "ApexCompanionService starting...")

        // 1. Acquire Partial WakeLock to prevent aggressive One UI 9 Doze sleep
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ApexNexus:PersistentCompanionWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 hours max holding
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock acquisition bypassed: ${e.message}")
        }

        // 2. Initialize Text-to-Speech Engine
        try {
            textToSpeech = TextToSpeech(applicationContext, this)
        } catch (e: Exception) {
            Log.w(TAG, "TTS init deferred: ${e.message}")
        }

        // 3. Start Foreground Notification safely
        try {
            createNotificationChannel()
            val notification = buildForegroundNotification("AI Companion Online", "Listening on Galaxy S24 Ultra")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground caught exception: ${e.message}")
        }

        // 4. Start Local Private Room Server
        roomServer = LocalRoomServer(applicationContext, 8765) { message ->
            Log.d(TAG, "Server received message: ${message.content}")
        }.apply {
            start()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                textToSpeech?.setPitch(0.95f)
                textToSpeech?.setSpeechRate(1.05f)

                // Select high quality neural voice if available
                val voices = textToSpeech?.voices
                val bestVoice = voices?.firstOrNull { voice ->
                    voice.locale == Locale.US && voice.quality >= 400 && !voice.isNetworkConnectionRequired
                } ?: voices?.firstOrNull { it.locale == Locale.US }

                if (bestVoice != null) {
                    textToSpeech?.voice = bestVoice
                }

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = CompanionVoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _voiceState.value = CompanionVoiceState.STANDBY
                    }

                    override fun onError(utteranceId: String?) {
                        _voiceState.value = CompanionVoiceState.STANDBY
                    }
                })

                Log.i(TAG, "TextToSpeech successfully initialized with neural voice configuration.")
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status: $status")
        }
    }

    /**
     * Speak response with high quality TTS.
     */
    fun speak(text: String, onDone: () -> Unit = {}) {
        if (!isTtsReady) {
            Log.w(TAG, "TextToSpeech not ready yet")
            return
        }
        _voiceState.value = CompanionVoiceState.SPEAKING
        val utteranceId = "apex_utt_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Autonomous conversation step: process query, think, speak answer.
     */
    fun processVoiceQuery(query: String, onReply: (String) -> Unit = {}) {
        scope.launch {
            _voiceState.value = CompanionVoiceState.THINKING
            updateNotification("Thinking...", "Processing: $query")
            val reply = aiService.generateCompanionResponse(query)
            _voiceState.value = CompanionVoiceState.SPEAKING
            updateNotification("Apex Speaking", reply.take(60))
            speak(reply) {
                _voiceState.value = CompanionVoiceState.STANDBY
            }
            onReply(reply)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(title: String, text: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildForegroundNotification(title, text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        roomServer?.stop()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        Log.i(TAG, "ApexCompanionService destroyed.")
    }
}
