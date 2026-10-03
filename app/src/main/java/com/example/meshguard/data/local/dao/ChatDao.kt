package com.example.meshguard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meshguard.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Step 12: DAO for chat_messages table.
 * OnConflictStrategy.IGNORE means a gossip-relayed duplicate of a message
 * we already stored is silently dropped — no crash, no update needed.
 */
@Dao
interface ChatDao {

    /** Live stream of all messages newest-last (chat order). */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    /** Blocking check: have we already stored this message id? */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE id = :id")
    suspend fun countById(id: String): Int

    /**
     * Insert a new message. IGNORE on conflict so relay loops
     * that re-deliver the same id are harmless.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}
