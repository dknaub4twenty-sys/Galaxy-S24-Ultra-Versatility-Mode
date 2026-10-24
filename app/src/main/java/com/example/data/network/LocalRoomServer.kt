package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.database.ApexDatabase
import com.example.data.database.ChatMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID

private const val TAG = "LocalRoomServer"

class LocalRoomServer(
    private val context: Context,
    private val port: Int = 8765,
    private val onMessageReceived: (ChatMessageEntity) -> Unit = {}
) {
    private var serverSocket: ServerSocket? = null
    private val db = ApexDatabase.getDatabase(context)
    private val serverJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serverJob)
    val serverToken: String = UUID.randomUUID().toString().take(8)

    @Volatile
    private var isRunning = false

    fun start() {
        if (isRunning) return
        scope.launch {
            try {
                val socket = ServerSocket(port)
                serverSocket = socket
                isRunning = true
                Log.i(TAG, "Standard ServerSocket LocalRoomServer started on port $port [Token: $serverToken]")

                while (isActive && !socket.isClosed) {
                    try {
                        val client = socket.accept()
                        launch { handleClientSocket(client) }
                    } catch (e: Exception) {
                        if (!socket.isClosed) {
                            Log.w(TAG, "Socket accept error: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to bind port $port on Android runtime: ${e.message}")
                isRunning = false
            }
        }
    }

    private suspend fun handleClientSocket(socket: Socket) {
        socket.use { client ->
            try {
                val reader = BufferedReader(InputStreamReader(client.getInputStream(), Charsets.UTF_8))
                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0]
                val uri = parts[1]

                // Read headers
                val headers = mutableMapOf<String, String>()
                var headerLine = reader.readLine()
                var contentLength = 0
                while (!headerLine.isNullOrEmpty()) {
                    val colonIdx = headerLine.indexOf(':')
                    if (colonIdx > 0) {
                        val key = headerLine.substring(0, colonIdx).trim().lowercase()
                        val value = headerLine.substring(colonIdx + 1).trim()
                        headers[key] = value
                        if (key == "content-length") {
                            contentLength = value.toIntOrNull() ?: 0
                        }
                    }
                    headerLine = reader.readLine()
                }

                // Read body if POST
                val body = if (contentLength > 0) {
                    val charArray = CharArray(contentLength)
                    var readSoFar = 0
                    while (readSoFar < contentLength) {
                        val count = reader.read(charArray, readSoFar, contentLength - readSoFar)
                        if (count <= 0) break
                        readSoFar += count
                    }
                    String(charArray, 0, readSoFar)
                } else ""

                val out = client.getOutputStream()

                when {
                    uri.startsWith("/status") -> {
                        val resp = JSONObject().apply {
                            put("service", "Apex Nexus Room Server")
                            put("status", "ONLINE")
                            put("port", port)
                            put("token", serverToken)
                            put("runtime", "Android 17 / One UI 9.0")
                        }.toString()
                        writeResponse(out, 200, "OK", "application/json", resp)
                    }
                    uri.startsWith("/room/messages") -> {
                        val roomId = uri.substringAfter("room=").substringBefore("&").ifBlank { "ultra-ops" }
                        val messages = db.apexDao().getRecentMessages(50)
                        val jsonArr = JSONArray()
                        messages.filter { it.roomId == roomId }.forEach { msg ->
                            jsonArr.put(JSONObject().apply {
                                put("id", msg.id)
                                put("sender", msg.senderName)
                                put("role", msg.senderRole)
                                put("content", msg.content)
                                put("timestamp", msg.timestamp)
                                put("isBridged", msg.isBridged)
                            })
                        }
                        writeResponse(out, 200, "OK", "application/json", jsonArr.toString())
                    }
                    uri.startsWith("/room/send") && method.equals("POST", ignoreCase = true) -> {
                        try {
                            val json = JSONObject(body)
                            val roomId = json.optString("roomId", "ultra-ops")
                            val senderName = json.optString("senderName", "Operator")
                            val senderRole = json.optString("senderRole", "USER")
                            val content = json.optString("content", "")

                            if (content.isNotBlank()) {
                                val entity = ChatMessageEntity(
                                    roomId = roomId,
                                    senderName = senderName,
                                    senderRole = senderRole,
                                    avatarId = "avatar_tactical",
                                    content = content
                                )
                                db.apexDao().insertMessage(entity)
                                onMessageReceived(entity)
                                writeResponse(out, 200, "OK", "application/json", "{\"success\":true}")
                            } else {
                                writeResponse(out, 400, "Bad Request", "application/json", "{\"error\":\"Empty content\"}")
                            }
                        } catch (e: Exception) {
                            writeResponse(out, 400, "Bad Request", "application/json", "{\"error\":\"JSON parse error\"}")
                        }
                    }
                    else -> {
                        val html = "<html><body style='background:#0B0E14;color:#00F0FF;font-family:monospace'><h3>Apex Nexus Private Room Server Online</h3><p>Port: $port | Token: $serverToken</p></body></html>"
                        writeResponse(out, 200, "OK", "text/html", html)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error handling client connection: ${e.message}")
            }
        }
    }

    private fun writeResponse(out: OutputStream, code: Int, status: String, contentType: String, body: String) {
        val bytes = body.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $code $status\r\n" +
                "Content-Type: $contentType; charset=utf-8\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing server socket: ${e.message}")
        }
        serverSocket = null
        serverJob.cancel()
    }

    fun isRunning(): Boolean = isRunning

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is InetAddress && addr.hostAddress?.contains(":") == false) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve local IP", e)
        }
        return "127.0.0.1"
    }

    fun generateShareableLink(roomId: String): String {
        val ip = getLocalIpAddress()
        return "apex://room/$roomId?host=$ip&port=$port&token=$serverToken"
    }
}
