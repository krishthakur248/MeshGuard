package com.example.meshguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.meshguard.data.model.HistoryNode
import com.example.meshguard.data.model.MedicalRecord
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * Room entity representing a survivor distress packet stored in local SQLite database.
 * Primary key is [survivorId] ensuring each phone keeps ONLY the newest packet per survivor
 * as defined in docs/00-core-idea.md and docs/03-architecture.md.
 * Step 7: added [ttl] column for gossip relay TTL enforcement.
 * Step 8: added location columns (latitude, longitude, locationAccuracy, locationCapturedAt).
 */
@Entity(tableName = "packets")
data class PacketEntity(
    @PrimaryKey
    val survivorId: String,
    val packetId: String,
    val survivorName: String,
    val statusTag: String,
    val priority: Int,
    val timestamp: Long,
    val hopCount: Int,
    val ttl: Int = 10,
    val sectorCode: String,
    val medicalDataJson: String,
    val isAcknowledged: Boolean = false,
    val receivedAt: Long = System.currentTimeMillis(),
    // Step 8: GPS location
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationAccuracy: Float = 0f,
    val locationCapturedAt: Long = 0L
) {
    fun toSurvivorPacket(): SurvivorPacket {
        val status = try {
            UrgencyStatus.valueOf(statusTag)
        } catch (e: Exception) {
            UrgencyStatus.UNKNOWN
        }

        val medRecord = try {
            val medObj = JSONObject(medicalDataJson)
            val allergiesArray = medObj.optJSONArray("allergies")
            val allergiesList = mutableListOf<String>()
            if (allergiesArray != null) {
                for (i in 0 until allergiesArray.length()) {
                    allergiesList.add(allergiesArray.getString(i))
                }
            }
            val conditionsArray = medObj.optJSONArray("chronicConditions")
            val conditionsList = mutableListOf<String>()
            if (conditionsArray != null) {
                for (i in 0 until conditionsArray.length()) {
                    conditionsList.add(conditionsArray.getString(i))
                }
            }
            MedicalRecord(
                name = medObj.optString("name", survivorName),
                age = medObj.optInt("age", 0),
                bloodType = medObj.optString("bloodType", ""),
                allergies = allergiesList,
                chronicConditions = conditionsList,
                emergencyContactName = medObj.optString("emergencyContactName", ""),
                emergencyContactRelation = medObj.optString("emergencyContactRelation", ""),
                isEncrypted = medObj.optBoolean("isEncrypted", true),
                encryptedPayloadPreview = medObj.optString("encryptedPayloadPreview", "")
            )
        } catch (e: Exception) {
            MedicalRecord(name = survivorName)
        }

        return SurvivorPacket(
            packetId = packetId,
            survivorId = survivorId,
            survivorName = survivorName,
            statusTag = status,
            medicalData = medRecord,
            historyNodes = emptyList(),
            timestamp = timestamp,
            priority = if (isAcknowledged) priority + 10 else priority,
            hopCount = hopCount,
            ttl = ttl,
            sectorCode = sectorCode,
            latitude = latitude,
            longitude = longitude,
            locationAccuracy = locationAccuracy,
            locationCapturedAt = locationCapturedAt
        )
    }

    companion object {
        fun fromSurvivorPacket(packet: SurvivorPacket, isAcknowledged: Boolean = false): PacketEntity {
            val med = JSONObject()
            med.put("name", packet.medicalData.name)
            med.put("age", packet.medicalData.age)
            med.put("bloodType", packet.medicalData.bloodType)
            med.put("allergies", JSONArray(packet.medicalData.allergies))
            med.put("chronicConditions", JSONArray(packet.medicalData.chronicConditions))
            med.put("emergencyContactName", packet.medicalData.emergencyContactName)
            med.put("emergencyContactRelation", packet.medicalData.emergencyContactRelation)
            med.put("isEncrypted", packet.medicalData.isEncrypted)
            med.put("encryptedPayloadPreview", packet.medicalData.encryptedPayloadPreview)

            return PacketEntity(
                survivorId = packet.survivorId,
                packetId = packet.packetId,
                survivorName = packet.survivorName,
                statusTag = packet.statusTag.name,
                priority = packet.priority,
                timestamp = packet.timestamp,
                hopCount = packet.hopCount,
                ttl = packet.ttl,
                sectorCode = packet.sectorCode,
                medicalDataJson = med.toString(),
                isAcknowledged = isAcknowledged,
                receivedAt = System.currentTimeMillis(),
                latitude = packet.latitude,
                longitude = packet.longitude,
                locationAccuracy = packet.locationAccuracy,
                locationCapturedAt = packet.locationCapturedAt
            )
        }
    }
}
