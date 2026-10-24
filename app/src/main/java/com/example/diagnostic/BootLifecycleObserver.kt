package com.example.diagnostic

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.data.database.ApexDatabase
import com.example.data.database.BootDiagnosticLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BootLifecycleObserver"

class BootLifecycleObserver(private val context: Context) : DefaultLifecycleObserver {

    private val db = ApexDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun logEvent(event: String, details: String) {
        val runtime = Runtime.getRuntime()
        val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val threadName = Thread.currentThread().name

        Log.i(TAG, "[$event] $details (Mem: ${usedMemMb}MB, Thread: $threadName)")

        scope.launch {
            try {
                db.apexDao().insertDiagnosticLog(
                    BootDiagnosticLogEntity(
                        lifecycleEvent = event,
                        details = details,
                        memoryAllocatedMb = usedMemMb,
                        threadName = threadName
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist diagnostic log: ${e.message}")
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        logEvent("ACTIVITY_ON_CREATE", "MainActivity entering onCreate stage")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logEvent("ACTIVITY_ON_START", "MainActivity visible and started")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        logEvent("ACTIVITY_ON_RESUME", "MainActivity actively focused and running in foreground")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        logEvent("ACTIVITY_ON_PAUSE", "MainActivity paused")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logEvent("ACTIVITY_ON_STOP", "MainActivity stopped")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        logEvent("ACTIVITY_ON_DESTROY", "MainActivity destroyed")
    }
}
