package com.example.meshguard.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meshguard.data.local.dao.PacketDao
import com.example.meshguard.data.local.entity.PacketEntity

/**
 * MeshGuard Room SQLite database representing the gossip buffer and local packet storage.
 * Stores packets surviving process termination and phone restarts.
 */
@Database(
    entities = [PacketEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MeshGuardDatabase : RoomDatabase() {

    abstract fun packetDao(): PacketDao

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
