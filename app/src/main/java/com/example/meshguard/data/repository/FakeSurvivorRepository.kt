package com.example.meshguard.data.repository

import com.example.meshguard.data.model.HistoryNode
import com.example.meshguard.data.model.MedicalRecord
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSurvivorRepository : SurvivorRepository {

    private val _mySurvivorPacket = MutableStateFlow(
        SurvivorPacket(
            survivorId = "USR-7701",
            survivorName = "Alex Rivera",
            statusTag = UrgencyStatus.TRAPPED,
            medicalData = MedicalRecord(
                bloodType = "O+",
                allergies = listOf("Penicillin", "Latex"),
                chronicConditions = listOf("Asthma"),
                emergencyContactName = "Sarah Rivera",
                emergencyContactRelation = "Spouse",
                isEncrypted = true,
                encryptedPayloadPreview = "AES256-GCM::7f9a21b...80c2"
            ),
            historyNodes = listOf(
                HistoryNode(
                    deviceAlias = "Stationary BLE Tag #02",
                    lastSeenTimestamp = System.currentTimeMillis() - 600000L,
                    signalStrengthRssi = -70,
                    hopIndex = 0
                )
            ),
            timestamp = System.currentTimeMillis() - 240000L,
            priority = UrgencyStatus.TRAPPED.priorityLevel,
            hopCount = 1,
            sectorCode = "SEC-4B"
        )
    )
    override val mySurvivorPacket: StateFlow<SurvivorPacket> = _mySurvivorPacket.asStateFlow()

    private val _triagedSurvivors = MutableStateFlow<List<SurvivorPacket>>(
        listOf(
            SurvivorPacket(
                survivorId = "A8F2",
                survivorName = "Anonymous Citizen #A8F2",
                statusTag = UrgencyStatus.TRAPPED,
                medicalData = MedicalRecord(
                    bloodType = "A-",
                    allergies = listOf("Aspirin"),
                    chronicConditions = listOf("Coronary Artery Disease"),
                    emergencyContactName = "David Hall",
                    emergencyContactRelation = "Brother",
                    isEncrypted = true,
                    encryptedPayloadPreview = "AES256-GCM::d83f2a...981c"
                ),
                historyNodes = listOf(
                    HistoryNode("Node A", System.currentTimeMillis() - 480000L, -75, 0)
                ),
                timestamp = System.currentTimeMillis() - 240000L,
                priority = UrgencyStatus.TRAPPED.priorityLevel,
                hopCount = 2,
                sectorCode = "SEC-4B"
            ),
            SurvivorPacket(
                survivorId = "B402",
                survivorName = "Marcus Chen",
                statusTag = UrgencyStatus.INJURED,
                medicalData = MedicalRecord(
                    bloodType = "B+",
                    allergies = listOf("Sulfa drugs"),
                    chronicConditions = listOf("Type-1 Diabetes"),
                    emergencyContactName = "Elena Chen",
                    emergencyContactRelation = "Wife",
                    isEncrypted = true,
                    encryptedPayloadPreview = "AES256-GCM::aa83f1...e042"
                ),
                historyNodes = listOf(
                    HistoryNode("Mule Peer #09", System.currentTimeMillis() - 480000L, -68, 1)
                ),
                timestamp = System.currentTimeMillis() - 480000L,
                priority = UrgencyStatus.INJURED.priorityLevel,
                hopCount = 1,
                sectorCode = "SEC-4C"
            )
        )
    )
    override val triagedSurvivors: StateFlow<List<SurvivorPacket>> = _triagedSurvivors.asStateFlow()

    override fun updateMyStatus(status: UrgencyStatus) {
        _mySurvivorPacket.update { current ->
            current.copy(
                statusTag = status,
                priority = status.priorityLevel,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    override fun updateSectorCode(sector: String) {
        _mySurvivorPacket.update { it.copy(sectorCode = sector) }
    }

    override fun getSurvivorById(survivorId: String): SurvivorPacket? {
        return _triagedSurvivors.value.find { it.survivorId == survivorId }
            ?: if (_mySurvivorPacket.value.survivorId == survivorId) _mySurvivorPacket.value else null
    }

    override fun markSurvivorAcknowledged(survivorId: String) {
        _triagedSurvivors.update { list ->
            list.map { packet ->
                if (packet.survivorId == survivorId) {
                    packet.copy(priority = packet.priority + 10)
                } else packet
            }
        }
    }

    override fun unlockSurvivorMedicalData(survivorId: String, rescuerAuthCode: String): Boolean {
        if (rescuerAuthCode.isNotBlank()) {
            _triagedSurvivors.update { list ->
                list.map { packet ->
                    if (packet.survivorId == survivorId) {
                        packet.copy(
                            medicalData = packet.medicalData.copy(isEncrypted = false)
                        )
                    } else packet
                }
            }
            return true
        }
        return false
    }

    override fun recordReceivedPacket(packet: SurvivorPacket) {
        _triagedSurvivors.update { list ->
            listOf(packet) + list.filterNot { it.survivorId == packet.survivorId }
        }
    }
}
