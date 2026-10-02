package com.example.meshguard.data.local.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meshguard.data.local.entity.PacketEntity
import kotlinx.coroutines.flow.Flow

private const val TAG = "PacketDao"

/**
 * Data Access Object for survivor distress packets.
 * Enforces newest-timestamp wins sync rule (docs/03-architecture.md).
 * Uses abstract class (not interface) so concrete helper methods can live here.
 *
 * Step 7: added clock safety, buffer cap eviction, and sync queries for gossip relay.
 */
@Dao
abstract class PacketDao {

    companion object {
        /** Max packets stored locally. When exceeded, lowest-priority oldest are evicted. */
        const val BUFFER_CAP = 500

        /** Reject incoming packets whose timestamp is more than this many ms in the future. */
        const val CLOCK_DRIFT_MAX_MS = 10L * 60 * 1000  // 10 minutes
    }

    @Query("SELECT * FROM packets ORDER BY priority ASC, timestamp DESC")
    abstract fun getAllPackets(): Flow<List<PacketEntity>>

    /** Blocking snapshot of all packets, sorted by priority (lowest first) then newest.
     *  Used for gossip sync when a new peer connects. */
    @Query("SELECT * FROM packets ORDER BY priority ASC, timestamp DESC")
    abstract suspend fun getAllPacketsSync(): List<PacketEntity>

    @Query("SELECT * FROM packets WHERE survivorId = :survivorId LIMIT 1")
    abstract suspend fun getPacketBySurvivorId(survivorId: String): PacketEntity?

    @Query("SELECT COUNT(*) FROM packets")
    abstract fun getPacketCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM packets")
    abstract suspend fun getPacketCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdate(packet: PacketEntity)

    @Query("UPDATE packets SET isAcknowledged = 1, priority = priority + 10 WHERE survivorId = :survivorId")
    abstract suspend fun markAcknowledged(survivorId: String)

    @Query("DELETE FROM packets WHERE survivorId = :survivorId")
    abstract suspend fun deleteBySurvivorId(survivorId: String)

    @Query("DELETE FROM packets")
    abstract suspend fun clearAll()

    /**
     * Delete lowest-priority (highest number), oldest packets when buffer exceeds [BUFFER_CAP].
     * Priority DESC puts low-priority (high number) first; timestamp ASC puts oldest first.
     * We keep only the top BUFFER_CAP rows and delete the rest, protecting [mySurvivorId].
     */
    @Query("""
        DELETE FROM packets WHERE survivorId != :mySurvivorId AND survivorId NOT IN (
            SELECT survivorId FROM packets ORDER BY priority ASC, timestamp DESC LIMIT :cap
        )
    """)
    abstract suspend fun evictBeyondCap(cap: Int = BUFFER_CAP, mySurvivorId: String = "")

    /**
     * Storage & Sync rule (Step 7 gossip relay):
     * 1. Drop if hopCount >= ttl.
     * 2. Clock safety: reject if timestamp > 10 min ahead of receiver clock.
     * 3. Newest timestamp wins. If timestamps are equal, existing packet is kept.
     * 4. After insert, enforce buffer cap (never evicting mySurvivorId).
     * Returns true if packet was stored, false if discarded.
     */
    suspend fun upsertIfNewer(newPacket: PacketEntity, mySurvivorId: String = ""): Boolean {
        // Step 7: TTL check — drop expired packets
        if (newPacket.hopCount >= newPacket.ttl) {
            Log.d(TAG, "upsertIfNewer: DROPPED ${newPacket.survivorId} — hopCount(${newPacket.hopCount}) >= ttl(${newPacket.ttl})")
            return false
        }

        // Step 7: Clock safety — reject packets too far in the future
        val now = System.currentTimeMillis()
        if (newPacket.timestamp > now + CLOCK_DRIFT_MAX_MS) {
            Log.w(TAG, "upsertIfNewer: REJECTED ${newPacket.survivorId} — timestamp ${newPacket.timestamp} is >10 min ahead of now ($now)")
            return false
        }

        val existing = getPacketBySurvivorId(newPacket.survivorId)
        if (existing == null) {
            insertOrUpdate(newPacket)
            enforceBufferCap(mySurvivorId)
            return true
        }
        if (newPacket.timestamp > existing.timestamp) {
            // Preserve acknowledged status if existing was already acked
            val packetToSave = if (existing.isAcknowledged && !newPacket.isAcknowledged) {
                newPacket.copy(isAcknowledged = true, priority = newPacket.priority + 10)
            } else {
                newPacket
            }
            insertOrUpdate(packetToSave)
            // No need to enforce cap here — we replaced, not added
            return true
        }
        // Older or equal timestamp — discard
        return false
    }

    /** Enforce buffer cap after insertions. */
    private suspend fun enforceBufferCap(mySurvivorId: String = "") {
        val count = getPacketCountSync()
        if (count > BUFFER_CAP) {
            Log.d(TAG, "enforceBufferCap: $count > $BUFFER_CAP — evicting excess")
            evictBeyondCap(BUFFER_CAP, mySurvivorId)
        }
    }
}
