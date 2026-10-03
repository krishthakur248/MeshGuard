package com.example.meshguard.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Survivor beacon distress packet.
 * Step 7: added `ttl` (time-to-live) for gossip relay.
 * Step 8: added location fields and changed default status to UNKNOWN.
 * Packet is dropped when hopCount >= ttl.
 */
data class SurvivorPacket(
    val packetId: String = UUID.randomUUID().toString().take(8),
    val survivorId: String = "",
    val survivorName: String = "",
    val statusTag: UrgencyStatus = UrgencyStatus.UNKNOWN,
    val medicalData: MedicalRecord = MedicalRecord(),
    val historyNodes: List<HistoryNode> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 3,
    val hopCount: Int = 0,
    val ttl: Int = 10,
    val sectorCode: String = "SEC-4B",
    // Step 8: real GPS location
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationAccuracy: Float = 0f,
    val locationCapturedAt: Long = 0L,
    val isAcknowledged: Boolean = false
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("packetId", packetId)
        root.put("survivorId", survivorId)
        root.put("survivorName", survivorName)
        root.put("statusTag", statusTag.name)
        root.put("isAcknowledged", isAcknowledged)
        root.put("timestamp", timestamp)
        root.put("priority", priority)
        root.put("hopCount", hopCount)
        root.put("ttl", ttl)
        root.put("sectorCode", sectorCode)

        // Step 8: location object
        val loc = JSONObject()
        loc.put("lat", latitude)
        loc.put("lon", longitude)
        loc.put("accuracyMeters", locationAccuracy.toDouble())
        loc.put("capturedAt", locationCapturedAt)
        root.put("location", loc)

        val med = JSONObject()
        med.put("name", medicalData.name)
        med.put("age", medicalData.age)
        med.put("bloodType", medicalData.bloodType)
        med.put("allergies", JSONArray(medicalData.allergies))
        med.put("chronicConditions", JSONArray(medicalData.chronicConditions))
        med.put("emergencyContactName", medicalData.emergencyContactName)
        med.put("emergencyContactRelation", medicalData.emergencyContactRelation)
        med.put("isEncrypted", medicalData.isEncrypted)
        med.put("encryptedPayloadPreview", medicalData.encryptedPayloadPreview)
        root.put("medicalData", med)

        return root.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): SurvivorPacket {
            val root = JSONObject(jsonStr)
            val medObj = root.optJSONObject("medicalData") ?: JSONObject()

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

            val medicalRecord = MedicalRecord(
                name = medObj.optString("name", "Alex Rivera"),
                age = medObj.optInt("age", 28),
                bloodType = medObj.optString("bloodType", "O+"),
                allergies = allergiesList,
                chronicConditions = conditionsList,
                emergencyContactName = medObj.optString("emergencyContactName", "Sarah Rivera"),
                emergencyContactRelation = medObj.optString("emergencyContactRelation", "Spouse"),
                isEncrypted = medObj.optBoolean("isEncrypted", true),
                encryptedPayloadPreview = medObj.optString("encryptedPayloadPreview", "")
            )

            val statusStr = root.optString("statusTag", UrgencyStatus.UNKNOWN.name)
            val status = try {
                UrgencyStatus.valueOf(statusStr)
            } catch (e: Exception) {
                UrgencyStatus.UNKNOWN
            }

            // Step 8: parse location
            val locObj = root.optJSONObject("location")
            val lat = locObj?.optDouble("lat", 0.0) ?: 0.0
            val lon = locObj?.optDouble("lon", 0.0) ?: 0.0
            val acc = locObj?.optDouble("accuracyMeters", 0.0)?.toFloat() ?: 0f
            val capturedAt = locObj?.optLong("capturedAt", 0L) ?: 0L

            return SurvivorPacket(
                packetId = root.optString("packetId", UUID.randomUUID().toString().take(8)),
                survivorId = root.optString("survivorId", "SURVIVOR-01"),
                survivorName = root.optString("survivorName", "Alex Rivera"),
                statusTag = status,
                medicalData = medicalRecord,
                timestamp = root.optLong("timestamp", System.currentTimeMillis()),
                priority = root.optInt("priority", status.priorityLevel),
                hopCount = root.optInt("hopCount", 0),
                ttl = root.optInt("ttl", 10),
                sectorCode = root.optString("sectorCode", "SEC-4B"),
                latitude = lat,
                longitude = lon,
                locationAccuracy = acc,
                locationCapturedAt = capturedAt,
                isAcknowledged = root.optBoolean("isAcknowledged", false)
            )
        }
    }
}
