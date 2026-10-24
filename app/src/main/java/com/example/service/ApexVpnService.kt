package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ApexVpnService"
private const val VPN_CHANNEL_ID = "apex_vpn_channel"
private const val VPN_NOTIFICATION_ID = 5050

class ApexVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        const val ACTION_START_VPN = "com.example.apex.ACTION_START_VPN"
        const val ACTION_STOP_VPN = "com.example.apex.ACTION_STOP_VPN"
        const val EXTRA_ALLOWED_PACKAGES = "EXTRA_ALLOWED_PACKAGES"

        private val _isVpnActive = MutableStateFlow(false)
        val isVpnActive = _isVpnActive.asStateFlow()

        private val _activeAllowedPackages = MutableStateFlow<List<String>>(emptyList())
        val activeAllowedPackages = _activeAllowedPackages.asStateFlow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_VPN -> {
                stopVpn()
            }
            ACTION_START_VPN, null -> {
                val allowedPkgs = intent?.getStringArrayListExtra(EXTRA_ALLOWED_PACKAGES) ?: arrayListOf()
                startPerAppVpn(allowedPkgs)
            }
        }
        return START_NOT_STICKY
    }

    private fun startPerAppVpn(allowedPackages: List<String>) {
        try {
            stopVpn()

            createVpnNotificationChannel()
            startForeground(VPN_NOTIFICATION_ID, buildVpnNotification(allowedPackages.size))

            val builder = Builder()
                .setSession("Apex Per-App Isolated Folder Tunnel")
                .addAddress("10.128.0.1", 24)
                .addDnsServer("1.1.1.1")
                .setMtu(1500)

            // Configure Per-App routing rules for selected apps in the 4-app folder
            var addedCount = 0
            for (pkg in allowedPackages) {
                if (pkg.isNotBlank()) {
                    try {
                        builder.addAllowedApplication(pkg)
                        addedCount++
                    } catch (e: Exception) {
                        Log.w(TAG, "Cannot add package $pkg to VPN: ${e.message}")
                    }
                }
            }

            vpnInterface = builder.establish()
            _isVpnActive.value = true
            _activeAllowedPackages.value = allowedPackages
            Log.i(TAG, "Per-App VPN established for $addedCount apps.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish VPN interface", e)
            stopVpn()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN", e)
        }
        _isVpnActive.value = false
        _activeAllowedPackages.value = emptyList()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createVpnNotificationChannel() {
        val channel = NotificationChannel(
            VPN_CHANNEL_ID,
            "Apex Isolated Folder VPN",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Per-folder isolated network routing tunnel"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildVpnNotification(packageCount: Int): Notification {
        return NotificationCompat.Builder(this, VPN_CHANNEL_ID)
            .setContentTitle("Apex Folder VPN Active")
            .setContentText("Routing isolated traffic for $packageCount apps")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
