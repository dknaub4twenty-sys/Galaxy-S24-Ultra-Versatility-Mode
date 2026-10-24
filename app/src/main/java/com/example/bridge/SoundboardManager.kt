package com.example.bridge

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "SoundboardManager"

class SoundboardManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator init failed", e)
        }
    }

    fun playCheersSound(onComplete: () -> Unit = {}) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Tactical celebratory soundboard pattern (Cheers fanfare)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                delay(140)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                delay(140)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                delay(260)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing cheers sound", e)
            }
            onComplete()
        }
    }

    fun playAlertCue() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 180)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alert cue", e)
        }
    }

    fun playTacticalAcknowledge() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing ack tone", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
