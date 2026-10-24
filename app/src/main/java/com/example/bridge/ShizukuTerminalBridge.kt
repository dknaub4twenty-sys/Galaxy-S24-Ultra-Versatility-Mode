package com.example.bridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

private const val TAG = "ShizukuTerminalBridge"

class ShizukuTerminalBridge(private val context: Context) {

    private val _isServerConnected = MutableStateFlow(false)
    val isServerConnected = _isServerConnected.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.i(TAG, "Shizuku binder received successfully.")
        _isServerConnected.value = true
        checkPermissionStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.w(TAG, "Shizuku binder dead / disconnected.")
        _isServerConnected.value = false
        _hasPermission.value = false
    }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            Log.i(TAG, "Shizuku permission result for request $requestCode: $granted")
            _hasPermission.value = granted
        }

    init {
        initShizukuConnection()
    }

    /**
     * Initializes a connection to the Shizuku server and registers listeners.
     */
    fun initShizukuConnection() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

            _isServerConnected.value = Shizuku.pingBinder()
            if (_isServerConnected.value) {
                checkPermissionStatus()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku initialization exception: ${e.message}")
            _isServerConnected.value = false
        }
    }

    /**
     * Checks if Shizuku permission is granted.
     */
    fun checkPermissionStatus(): Boolean {
        return try {
            if (!_isServerConnected.value && !Shizuku.pingBinder()) {
                _hasPermission.value = false
                return false
            }

            val granted = if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
            _hasPermission.value = granted
            granted
        } catch (e: Throwable) {
            Log.w(TAG, "Error checking Shizuku permission: ${e.message}")
            _hasPermission.value = false
            false
        }
    }

    /**
     * Requests Shizuku permission from user.
     */
    fun requestPermission(requestCode: Int = 1002) {
        try {
            if (Shizuku.pingBinder() && !hasPermission.value) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to prompt Shizuku permission dialog", e)
        }
    }

    /**
     * Executes a shell command via broadcast intent or RunCommandService intent to Termux,
     * following strict non-root security guidelines.
     */
    fun executeShellViaTermuxIntent(
        command: String,
        workDir: String = "/data/data/com.termux/files/home",
        inBackground: Boolean = true
    ): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName("com.termux", "com.termux.app.RunCommandService")
                action = "com.termux.RUN_COMMAND"
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
                putExtra("com.termux.RUN_COMMAND_WORKDIR", workDir)
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", inBackground)
            }
            context.startService(intent)
            Log.i(TAG, "Dispatched shell command to Termux service intent: $command")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch command to Termux", e)
            false
        }
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        } catch (e: Throwable) {
            Log.w(TAG, "Error unregistering Shizuku listeners: ${e.message}")
        }
    }
}
