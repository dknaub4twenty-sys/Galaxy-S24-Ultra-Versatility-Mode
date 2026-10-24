package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApexDao {

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun clearRoom(roomId: String)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessageEntity>

    // Macro Rules
    @Query("SELECT * FROM macro_rules ORDER BY id ASC")
    fun getAllMacroRules(): Flow<List<MacroRuleEntity>>

    @Query("SELECT * FROM macro_rules WHERE isEnabled = 1")
    suspend fun getActiveMacroRules(): List<MacroRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacroRule(rule: MacroRuleEntity): Long

    @Update
    suspend fun updateMacroRule(rule: MacroRuleEntity)

    @Query("DELETE FROM macro_rules WHERE id = :id")
    suspend fun deleteMacroRule(id: Long)

    @Query("UPDATE macro_rules SET triggerCount = triggerCount + 1, lastTriggeredAt = :now WHERE id = :id")
    suspend fun recordMacroTrigger(id: Long, now: Long = System.currentTimeMillis())

    // App Folders
    @Query("SELECT * FROM app_folders ORDER BY sortOrder ASC")
    fun getAllFolders(): Flow<List<AppFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: AppFolderEntity)

    @Update
    suspend fun updateFolder(folder: AppFolderEntity)

    @Query("DELETE FROM app_folders WHERE folderId = :folderId")
    suspend fun deleteFolder(folderId: String)

    // Coordinator Expansions
    @Query("SELECT * FROM coordinator_expansions ORDER BY createdAt DESC")
    fun getAllExpansions(): Flow<List<CoordinatorExpansionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpansion(expansion: CoordinatorExpansionEntity): Long

    // Boot Diagnostic Logs
    @Query("SELECT * FROM boot_diagnostic_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDiagnosticLogs(limit: Int = 100): Flow<List<BootDiagnosticLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosticLog(log: BootDiagnosticLogEntity): Long

    @Query("DELETE FROM boot_diagnostic_logs")
    suspend fun clearDiagnosticLogs()

    // Connector Nodes
    @Query("SELECT * FROM connector_nodes ORDER BY createdAt DESC")
    fun getAllConnectorNodes(): Flow<List<ConnectorNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectorNode(node: ConnectorNodeEntity)

    @Update
    suspend fun updateConnectorNode(node: ConnectorNodeEntity)

    @Query("DELETE FROM connector_nodes WHERE nodeId = :nodeId")
    suspend fun deleteConnectorNode(nodeId: String)

    // AI Packages
    @Query("SELECT * FROM ai_packages ORDER BY timestamp DESC")
    fun getAllAiPackages(): Flow<List<AiPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiPackage(pkg: AiPackageEntity)

    @Update
    suspend fun updateAiPackage(pkg: AiPackageEntity)
}
