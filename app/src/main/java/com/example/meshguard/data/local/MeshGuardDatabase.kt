package com.example.meshguard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meshguard.data.local.dao.ChatDao
import com.example.meshguard.data.local.dao.PacketDao
import com.example.meshguard.data.local.entity.ChatMessageEntity
import com.example.meshguard.data.local.entity.PacketEntity

/**
 * MeshGuard Room SQLite database.
 * Step 12: added ChatMessageEntity / ChatDao (version bump 3 → 4).
 * fallbackToDestructiveMigration() handles the schema change on first install of this version.
 */
@Database(
    entities = [PacketEntity::class, ChatMessageEntity::class],
    version = 4,
    exportSchema = false
)
abstract class MeshGuardDatabase : RoomDatabase() {

    abstract fun packetDao(): PacketDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: MeshGuardDatabase? = null

        fun getInstance(context: Context): MeshGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeshGuardDatabase::class.java,
                    "meshguard.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
