package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatMessageEntity::class,
        MacroRuleEntity::class,
        AppFolderEntity::class,
        CoordinatorExpansionEntity::class,
        BootDiagnosticLogEntity::class,
        ConnectorNodeEntity::class,
        AiPackageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ApexDatabase : RoomDatabase() {
    abstract fun apexDao(): ApexDao

    companion object {
        @Volatile
        private var INSTANCE: ApexDatabase? = null

        fun getDatabase(context: Context): ApexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ApexDatabase::class.java,
                    "apex_nexus.db"
                ).fallbackToDestructiveMigration(dropAllTables = false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
