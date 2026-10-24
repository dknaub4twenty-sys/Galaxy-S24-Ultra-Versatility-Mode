package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: String,
    val senderName: String,
    val senderRole: String, // "USER", "COMPANION", "COPILOT", "GROK", "MUSE", "GEMINI", "SYSTEM"
    val avatarId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBridged: Boolean = false,
    val targetAi: String? = null
)

@Entity(tableName = "macro_rules")
data class MacroRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val targetPackage: String,
    val targetNodeText: String, // Text or substring to locate in accessibility node
    val targetViewId: String = "",
    val actionType: String, // "CLICK_NODE", "SOUNDBOARD_CHEERS", "SAY_VOICE", "CHAIN_DISCORD_WELCOME"
    val payload: String = "", // Speech text or soundboard sound name
    val isEnabled: Boolean = true,
    val triggerCount: Int = 0,
    val lastTriggeredAt: Long = 0
)

@Entity(tableName = "app_folders")
data class AppFolderEntity(
    @PrimaryKey val folderId: String,
    val folderName: String,
    val iconName: String,
    val app1Package: String,
    val app2Package: String,
    val app3Package: String = "",
    val app4Package: String = "",
    val isVpnEnabled: Boolean = false,
    val isWakeLockForced: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "coordinator_expansions")
data class CoordinatorExpansionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val summaryTitle: String,
    val synthesizedContent: String,
    val modelUsed: String,
    val totalContextItemsPooled: Int,
    val createdAt: Long = System.currentTimeMillis()
)
