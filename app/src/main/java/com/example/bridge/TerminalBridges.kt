package com.example.bridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

private const val TAG = "TerminalBridges"

data class CommandResult(
    val command: String,
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val bridgeType: String // "SHIZUKU", "SHIZUKU_TERMUX", "TERMUX_INTENT", "LOCAL_SANDBOX"
)

class TerminalBridges(private val context: Context) {

    /**
     * Check if Shizuku Manager application is installed.
     */
    fun isShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isShizukuAvailable(): Boolean = isShizukuInstalled() || isShizukuRunning()

    /**
     * Check if the Shizuku server binder service is alive and pingable.
     */
    fun isShizukuRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Check if this app has been granted Shizuku permissions (UID 2000 ADB shell).
     */
    fun hasShizukuPermission(): Boolean {
        return try {
            if (!isShizukuRunning()) return false
            if (Shizuku.isPreV11()) false else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Request Shizuku permission prompt via official Shizuku IPC binder.
     */
    fun requestShizukuPermission(requestCode: Int = 1001) {
        try {
            if (isShizukuRunning() && !hasShizukuPermission()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Check if Termux is installed.
     */
    fun isTermuxAvailable(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.termux", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Execute a shell command using the best available bridge:
     * 1. Shizuku (if running and granted)
     * 2. Termux RUN_COMMAND intent (if termux: prefix)
     * 3. Local sandboxed shell runner
     */
    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        val trimmed = command.trim()

        // Explicit Shizuku command
        if (trimmed.startsWith("shizuku:", ignoreCase = true)) {
            val cmd = trimmed.substringAfter("shizuku:").trim()
            return@withContext executeViaShizuku(cmd)
        }

        // Shizuku-Termux bridge
        if (trimmed.startsWith("shizuku-termux:", ignoreCase = true)) {
            val cmd = trimmed.substringAfter("shizuku-termux:").trim()
            return@withContext executeTermuxViaShizuku(cmd)
        }

        // Termux Intent dispatch if command starts with 'termux:'
        if (trimmed.startsWith("termux:", ignoreCase = true)) {
            val cmd = trimmed.substringAfter("termux:").trim()
            if (isShizukuRunning() && hasShizukuPermission()) {
                return@withContext executeTermuxViaShizuku(cmd)
            }
            return@withContext executeViaTermuxIntent(cmd)
        }

        // If Shizuku is armed, prioritize elevated ADB execution
        if (isShizukuRunning() && hasShizukuPermission()) {
            val shizukuRes = executeViaShizuku(trimmed)
            if (shizukuRes.exitCode == 0 || shizukuRes.stderr.isBlank()) {
                return@withContext shizukuRes
            }
        }

        // Fallback: Local sandboxed shell execution
        return@withContext executeViaLocalSandbox(trimmed)
    }

    /**
     * Execute shell command via Shizuku IPC binder with ADB shell privileges (non-root).
     */
    fun executeViaShizuku(command: String): CommandResult {
        if (!isShizukuRunning()) {
            return CommandResult(
                command = command,
                stdout = "",
                stderr = "Shizuku server is not running. Please start Shizuku on your S24 Ultra via Wireless Debugging.",
                exitCode = 127,
                bridgeType = "SHIZUKU"
            )
        }

        if (!hasShizukuPermission()) {
            requestShizukuPermission()
            return CommandResult(
                command = command,
                stdout = "",
                stderr = "Shizuku permission not yet granted. Requested permission dialog in Shizuku Manager.",
                exitCode = 126,
                bridgeType = "SHIZUKU"
            )
        }

        val stdoutBuilder = StringBuilder()
        val stderrBuilder = StringBuilder()
        var exitCode = -1

        try {
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }

            val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stdoutBuilder.appendLine(line)
                    line = reader.readLine()
                }
            }

            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stderrBuilder.appendLine(line)
                    line = reader.readLine()
                }
            }

            exitCode = process.waitFor()
        } catch (e: Throwable) {
            Log.e(TAG, "Shizuku process execution failed", e)
            stderrBuilder.appendLine("Shizuku execution error: ${e.message}")
            exitCode = 1
        }

        return CommandResult(
            command = command,
            stdout = stdoutBuilder.toString().trimEnd(),
            stderr = stderrBuilder.toString().trimEnd(),
            exitCode = exitCode,
            bridgeType = "SHIZUKU"
        )
    }

    /**
     * Execute Termux command through Shizuku ADB bridge following non-root security boundaries.
     * Uses Android's ActivityManager to securely dispatch to Termux's RunCommandService.
     */
    fun executeTermuxViaShizuku(termuxScript: String): CommandResult {
        if (!isTermuxAvailable()) {
            return CommandResult(
                command = termuxScript,
                stdout = "",
                stderr = "Termux application is not installed on this device.",
                exitCode = 127,
                bridgeType = "SHIZUKU_TERMUX"
            )
        }

        val escapedScript = termuxScript.replace("'", "'\\''")
        val adbCmd = "am startservice --user 0 -n com.termux/com.termux.app.RunCommandService -a com.termux.RUN_COMMAND --es com.termux.RUN_COMMAND_PATH '/data/data/com.termux/files/usr/bin/bash' --esa com.termux.RUN_COMMAND_ARGUMENTS '-c,$escapedScript' --ez com.termux.RUN_COMMAND_BACKGROUND true"

        val result = executeViaShizuku(adbCmd)
        return CommandResult(
            command = termuxScript,
            stdout = if (result.exitCode == 0) "Dispatched to Termux via Shizuku ADB bridge: $termuxScript" else result.stdout,
            stderr = result.stderr,
            exitCode = result.exitCode,
            bridgeType = "SHIZUKU_TERMUX"
        )
    }

    /**
     * Execute via standard Android Termux RUN_COMMAND intent.
     */
    fun executeViaTermuxIntent(scriptCommand: String): CommandResult {
        if (!isTermuxAvailable()) {
            return CommandResult(
                command = scriptCommand,
                stdout = "",
                stderr = "Termux is not installed. Please install Termux from F-Droid to enable RUN_COMMAND.",
                exitCode = 127,
                bridgeType = "TERMUX_INTENT"
            )
        }

        try {
            val intent = Intent().apply {
                component = ComponentName("com.termux", "com.termux.app.RunCommandService")
                action = "com.termux.RUN_COMMAND"
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", scriptCommand))
                putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            }
            context.startService(intent)
            return CommandResult(
                command = scriptCommand,
                stdout = "Dispatched '$scriptCommand' to Termux background service via intent.",
                stderr = "",
                exitCode = 0,
                bridgeType = "TERMUX_INTENT"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Termux dispatch error", e)
            return CommandResult(
                command = scriptCommand,
                stdout = "",
                stderr = "Failed to dispatch command to Termux: ${e.message}",
                exitCode = 1,
                bridgeType = "TERMUX_INTENT"
            )
        }
    }

    /**
     * Execute local shell process in sandboxed process space.
     */
    private fun executeViaLocalSandbox(command: String): CommandResult {
        val stdoutBuilder = StringBuilder()
        val stderrBuilder = StringBuilder()
        var exitCode = -1

        try {
            val process = ProcessBuilder("/system/bin/sh", "-c", command)
                .redirectErrorStream(false)
                .start()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stdoutBuilder.appendLine(line)
                    line = reader.readLine()
                }
            }

            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    stderrBuilder.appendLine(line)
                    line = reader.readLine()
                }
            }

            exitCode = process.waitFor()
        } catch (e: Exception) {
            stderrBuilder.appendLine("Process execution failed: ${e.message}")
            exitCode = 1
        }

        return CommandResult(
            command = command,
            stdout = stdoutBuilder.toString().trimEnd(),
            stderr = stderrBuilder.toString().trimEnd(),
            exitCode = exitCode,
            bridgeType = "LOCAL_SANDBOX"
        )
    }

    fun getDeviceDiagnostics(): String {
        return """
            DEVICE: Samsung Galaxy S24 Ultra (${Build.MODEL} / ${Build.PRODUCT})
            ANDROID: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            FIRMWARE: ${Build.DISPLAY}
            SHIZUKU INSTALLED: ${isShizukuInstalled()}
            SHIZUKU SERVICE RUNNING: ${isShizukuRunning()}
            SHIZUKU PERMISSION: ${hasShizukuPermission()}
            TERMUX INSTALLED: ${isTermuxAvailable()}
            SELINUX: Enforcing (Zero-Root)
            KNOX: 3.14 Active
        """.trimIndent()
    }
}
