package com.example.bridge

import android.content.Context
import android.util.Log
import com.example.data.database.AiPackageEntity
import com.example.data.database.ApexDatabase
import com.example.data.database.ConnectorNodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val TAG = "AiPackageRouter"

class AiPackageRouter(private val context: Context) {

    private val db = ApexDatabase.getDatabase(context)
    private val packagesDir = File(context.filesDir, "ai_packages").apply { mkdirs() }

    /**
     * Automate setting up a local connector node.
     */
    suspend fun automateNewConnectorNode(
        nodeName: String,
        port: Int = 8765
    ): ConnectorNodeEntity = withContext(Dispatchers.IO) {
        val nodeId = "node_" + UUID.randomUUID().toString().take(6)
        val entity = ConnectorNodeEntity(
            nodeId = nodeId,
            nodeName = nodeName,
            port = port,
            endpointUrl = "http://127.0.0.1:$port/node/$nodeId",
            status = "ONLINE",
            activeAiTargets = "@copilot,@grok,@gemini,@muse",
            totalPacketsRouted = 0,
            lastPacketSummary = "Node initialized and listening."
        )
        db.apexDao().insertConnectorNode(entity)
        entity
    }

    /**
     * Packs up an AI message into a tagged .txt or .zip file and routes it through the node.
     */
    suspend fun createAndRoutePackage(
        sourceAi: String,
        targetAi: String,
        messageContent: String,
        format: String // "TXT" or "ZIP"
    ): AiPackageEntity = withContext(Dispatchers.IO) {
        val packageId = "pkg_" + UUID.randomUUID().toString().take(8)
        val file: File

        if (format.equals("ZIP", ignoreCase = true)) {
            // Bundle into .zip archive with manifest.json and payload.txt
            file = File(packagesDir, "$packageId.zip")
            ZipOutputStream(FileOutputStream(file)).use { zos ->
                // 1. Manifest
                val manifestJson = JSONObject().apply {
                    put("packageId", packageId)
                    put("sourceAi", sourceAi)
                    put("targetAi", targetAi)
                    put("timestamp", System.currentTimeMillis())
                }.toString()
                zos.putNextEntry(ZipEntry("manifest.json"))
                zos.write(manifestJson.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 2. Payload file
                zos.putNextEntry(ZipEntry("payload_message.txt"))
                zos.write(messageContent.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        } else {
            // Bundle into tagged .txt file
            file = File(packagesDir, "$packageId.txt")
            val textContent = """
                [METADATA_HEADER]
                PACKAGE_ID: $packageId
                SOURCE_AI: $sourceAi
                TAG_RECIPIENT: $targetAi
                TIMESTAMP: ${System.currentTimeMillis()}
                [PAYLOAD_START]
                $messageContent
                [PAYLOAD_END]
            """.trimIndent()
            file.writeText(textContent, Charsets.UTF_8)
        }

        // Automated Inspection & Response Simulation
        val autoReplyPayload = "Acknowledge receipt from $targetAi: Processed payload from $sourceAi. Coordination plan updated."

        val entity = AiPackageEntity(
            packageId = packageId,
            format = format.uppercase(),
            sourceAi = sourceAi,
            targetAi = targetAi,
            payloadSummary = messageContent.take(80),
            filePath = file.absolutePath,
            status = "AUTO_REPLIED",
            responsePayload = autoReplyPayload
        )

        db.apexDao().insertAiPackage(entity)
        Log.i(TAG, "Routed $format package $packageId from $sourceAi to $targetAi via local node.")
        entity
    }

    /**
     * Inspects the contents of a package (.txt or .zip).
     */
    suspend fun inspectPackageContent(packageEntity: AiPackageEntity): String = withContext(Dispatchers.IO) {
        val file = File(packageEntity.filePath)
        if (!file.exists()) return@withContext "File not found: ${file.name}"

        if (packageEntity.format.equals("ZIP", ignoreCase = true)) {
            val contentBuilder = StringBuilder("[ZIP ARCHIVE INSPECTION: ${file.name}]\n")
            ZipInputStream(FileInputStream(file)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    contentBuilder.appendLine(" • Entry: ${entry.name} (${entry.size} bytes)")
                    if (entry.name == "manifest.json" || entry.name.endsWith(".txt")) {
                        val text = zis.bufferedReader().readText()
                        contentBuilder.appendLine("   >>> Content: $text")
                    }
                    entry = zis.nextEntry
                }
            }
            return@withContext contentBuilder.toString()
        } else {
            return@withContext file.readText(Charsets.UTF_8)
        }
    }
}
