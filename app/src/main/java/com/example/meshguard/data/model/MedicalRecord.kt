package com.example.meshguard.data.model

/**
 * Local survivor profile and encrypted medical record.
 */
data class MedicalRecord(
    val name: String = "Alex Rivera",
    val age: Int = 28,
    val bloodType: String = "O+",
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val isEncrypted: Boolean = true,
    val encryptedPayloadPreview: String = ""
)
