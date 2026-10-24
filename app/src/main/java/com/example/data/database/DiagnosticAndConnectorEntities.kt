package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boot_diagnostic_logs")
data class BootDiagnosticLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lifecycleEvent: String,
    val details: String,
    val memoryAllocatedMb: Long,
    val threadName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "connector_nodes")
data class ConnectorNodeEntity(
    @PrimaryKey val nodeId: String,
    val nodeName: String,
    val port: Int,
    val endpointUrl: String,
    val status: String, // "ONLINE", "ROUTING", "STANDBY"
    val activeAiTargets: String, // e.g. "@copilot,@grok,@gemini,@muse"
    val totalPacketsRouted: Int = 0,
    val lastPacketSummary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_packages")
data class AiPackageEntity(
    @PrimaryKey val packageId: String,
    val format: String, // "TXT", "ZIP"
    val sourceAi: String,
    val targetAi: String,
    val payloadSummary: String,
    val filePath: String,
    val status: String, // "ROUTED", "INSPECTED", "AUTO_REPLIED"
    val responsePayload: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
