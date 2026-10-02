package com.example.meshguard.data.repository

import com.example.meshguard.AppConfig
import com.example.meshguard.data.model.MedicalRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeMedicalIdRepository : MedicalIdRepository {

    private val _medicalRecord = MutableStateFlow(
        MedicalRecord(
            name = "Alex Rivera",
            age = 28,
            bloodType = "O+",
            allergies = listOf("Penicillin", "Latex"),
            chronicConditions = listOf("Type-1 Diabetes", "Asthma"),
            emergencyContactName = "Sarah Rivera",
            emergencyContactRelation = "Spouse (Phone: +1 555-0192)",
            isEncrypted = true,
            encryptedPayloadPreview = "AES256-GCM::9d84f1a0e37bc281...f77a"
        )
    )
    override val medicalRecord: StateFlow<MedicalRecord> = _medicalRecord.asStateFlow()

    override fun updateMedicalRecord(record: MedicalRecord) {
        _medicalRecord.value = record
    }

    override fun toggleEncryption(enable: Boolean) {
        _medicalRecord.update { it.copy(isEncrypted = enable) }
    }

    override fun unlockWithRescuerToken(tokenCode: String): Boolean {
        if (tokenCode.trim() == AppConfig.RESCUER_ACCESS_CODE_DEMO || tokenCode.trim().length >= 4) {
            _medicalRecord.update { it.copy(isEncrypted = false) }
            return true
        }
        return false
    }
}
