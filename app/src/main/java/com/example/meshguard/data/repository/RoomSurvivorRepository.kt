package com.example.meshguard.data.repository

import android.content.Context
import android.os.Build
import com.example.meshguard.data.local.dao.PacketDao
import com.example.meshguard.data.local.entity.PacketEntity
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Persistent Room-backed implementation of SurvivorRepository.
 * Stores and loads received survivor packets using SQLite via PacketDao.
 * Preserves received packets and triaged survivor data across app restarts.
 * Step 8: default status is UNKNOWN; location from LocationProvider is wired in.
 */
class RoomSurvivorRepository(
    context: Context,
    private val packetDao: PacketDao,
    private val accountRepository: AccountRepository = AccountRepository.getInstance(context),
    private val locationProvider: LocationProvider? = null
) : SurvivorRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val mySurvivorId = accountRepository.getOrCreateSurvivorId()

    private val _mySurvivorPacket = MutableStateFlow(
        SurvivorPacket(
            survivorId = mySurvivorId,
            survivorName = accountRepository.medicalRecord.value.name.ifBlank { "Alex Rivera" },
            statusTag = UrgencyStatus.UNKNOWN,
            medicalData = accountRepository.medicalRecord.value,
            historyNodes = emptyList(),
            timestamp = System.currentTimeMillis(),
            priority = UrgencyStatus.UNKNOWN.priorityLevel,
            hopCount = 0,
            sectorCode = "SEC-4B"
        )
    )
    override val mySurvivorPacket: StateFlow<SurvivorPacket> = _mySurvivorPacket.asStateFlow()

    init {
        // Sync mySurvivorPacket whenever accountRepository's medicalRecord updates
        scope.launch {
            accountRepository.medicalRecord.collect { record ->
                _mySurvivorPacket.update { current ->
                    current.copy(
                        survivorName = record.name.ifBlank { "Alex Rivera" },
                        medicalData = record
                    )
                }
            }
        }

        // Step 8: Wire location updates into mySurvivorPacket.
        // When a new location fix arrives, update the packet with fresh coordinates.
        // This ensures every gossip sync sends the latest known position.
        if (locationProvider != null) {
            scope.launch {
                locationProvider.currentLocation.collect { locData ->
                    if (locData.latitude != 0.0 || locData.longitude != 0.0) {
                        _mySurvivorPacket.update { current ->
                            current.copy(
                                latitude = locData.latitude,
                                longitude = locData.longitude,
                                locationAccuracy = locData.accuracyMeters,
                                locationCapturedAt = locData.capturedAt,
                                // Refresh timestamp so gossip sync picks up the new location
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
        }
    }

    override val triagedSurvivors: StateFlow<List<SurvivorPacket>> = packetDao.getAllPackets()
        .map { entities -> entities.map { it.toSurvivorPacket() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

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
        return triagedSurvivors.value.find { it.survivorId == survivorId }
            ?: if (_mySurvivorPacket.value.survivorId == survivorId) _mySurvivorPacket.value else null
    }

    override fun markSurvivorAcknowledged(survivorId: String) {
        scope.launch {
            packetDao.markAcknowledged(survivorId)
        }
    }

    override fun unlockSurvivorMedicalData(survivorId: String, rescuerAuthCode: String): Boolean {
        if (rescuerAuthCode.isNotBlank()) {
            val survivor = triagedSurvivors.value.find { it.survivorId == survivorId } ?: return false
            val updated = survivor.copy(
                medicalData = survivor.medicalData.copy(isEncrypted = false)
            )
            scope.launch {
                packetDao.insertOrUpdate(PacketEntity.fromSurvivorPacket(updated))
            }
            return true
        }
        return false
    }

    override fun recordReceivedPacket(packet: SurvivorPacket) {
        scope.launch {
            packetDao.upsertIfNewer(PacketEntity.fromSurvivorPacket(packet))
        }
    }
}
