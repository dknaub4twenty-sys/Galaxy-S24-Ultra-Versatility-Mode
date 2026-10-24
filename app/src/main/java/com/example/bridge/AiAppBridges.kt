package com.example.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

private const val TAG = "AiAppBridges"

enum class SupportedAi(
    val displayName: String,
    val packageName: String,
    val deepLinkUrl: String,
    val priority: Int
) {
    MUSE(
        displayName = "Muse AI (Meta)",
        packageName = "com.facebook.orca",
        deepLinkUrl = "https://www.meta.ai",
        priority = 1
    ),
    GROK(
        displayName = "Grok",
        packageName = "ai.x.grok",
        deepLinkUrl = "https://x.com/i/grok",
        priority = 2
    ),
    COPILOT(
        displayName = "Copilot",
        packageName = "com.microsoft.copilot",
        deepLinkUrl = "https://copilot.microsoft.com",
        priority = 3
    ),
    GEMINI(
        displayName = "Gemini",
        packageName = "com.google.android.apps.bard",
        deepLinkUrl = "https://gemini.google.com",
        priority = 4
    )
}

class AiAppBridges(private val context: Context) {

    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Route message to target assistant using Intent or deep-link with clipboard handoff.
     */
    fun routeMessageToAi(ai: SupportedAi, message: String): Boolean {
        // Copy message to clipboard for instant paste convenience
        val clip = ClipData.newPlainText("Apex Bridged Prompt", message)
        clipboard.setPrimaryClip(clip)

        // Try direct launcher intent first if installed
        val launchIntent = context.packageManager.getLaunchIntentForPackage(ai.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.putExtra(Intent.EXTRA_TEXT, message)
            try {
                context.startActivity(launchIntent)
                Log.d(TAG, "Routed message to installed app: ${ai.displayName}")
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to launch package ${ai.packageName}", e)
            }
        }

        // Fallback: Web / App Deep-link Intent
        return try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ai.deepLinkUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            Log.d(TAG, "Routed message via deep link to: ${ai.displayName}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed deep link for ${ai.displayName}", e)
            false
        }
    }
}
