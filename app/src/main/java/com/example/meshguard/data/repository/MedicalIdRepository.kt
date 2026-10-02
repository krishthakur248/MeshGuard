package com.example.meshguard.data.repository

import com.example.meshguard.data.model.MedicalRecord
import kotlinx.coroutines.flow.StateFlow

interface MedicalIdRepository {
    val medicalRecord: StateFlow<MedicalRecord>

    fun updateMedicalRecord(record: MedicalRecord)
    fun toggleEncryption(enable: Boolean)
    fun unlockWithRescuerToken(tokenCode: String): Boolean
}
