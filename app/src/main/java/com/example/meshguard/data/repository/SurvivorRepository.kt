package com.example.meshguard.data.repository

import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import kotlinx.coroutines.flow.StateFlow

interface SurvivorRepository {
    val mySurvivorPacket: StateFlow<SurvivorPacket>
    val triagedSurvivors: StateFlow<List<SurvivorPacket>>

    fun updateMyStatus(status: UrgencyStatus)
    fun updateSectorCode(sector: String)
    fun getSurvivorById(survivorId: String): SurvivorPacket?
    fun markSurvivorAcknowledged(survivorId: String)
    fun unlockSurvivorMedicalData(survivorId: String, rescuerAuthCode: String): Boolean
    fun recordReceivedPacket(packet: SurvivorPacket)
}
