package com.example.meshguard.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.meshguard.data.model.MedicalRecord
import com.example.meshguard.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Local Account Repository managing persistent user profile, selected UserRole,
 * and onboarding status via SharedPreferences.
 */
class AccountRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("meshguard_account_prefs", Context.MODE_PRIVATE)

    private val _userRole = MutableStateFlow(loadUserRole())
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(loadCompletedOnboarding())
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _hasGrantedPermissions = MutableStateFlow(loadGrantedPermissions())
    val hasGrantedPermissions: StateFlow<Boolean> = _hasGrantedPermissions.asStateFlow()

    private val _medicalRecord = MutableStateFlow(loadMedicalRecord())
    val medicalRecord: StateFlow<MedicalRecord> = _medicalRecord.asStateFlow()

    private fun loadUserRole(): UserRole {
        val roleStr = prefs.getString(KEY_USER_ROLE, UserRole.SURVIVOR.name)
        return try {
            UserRole.valueOf(roleStr ?: UserRole.SURVIVOR.name)
        } catch (e: Exception) {
            UserRole.SURVIVOR
        }
    }

    private fun loadCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_COMPLETED_ONBOARDING, false)
    }

    private fun loadGrantedPermissions(): Boolean {
        return prefs.getBoolean(KEY_GRANTED_PERMISSIONS, false)
    }

    private fun loadMedicalRecord(): MedicalRecord {
        val name = prefs.getString(KEY_SURVIVOR_NAME, "Alex Rivera") ?: "Alex Rivera"
        val age = prefs.getInt(KEY_SURVIVOR_AGE, 28)
        val bloodType = prefs.getString(KEY_BLOOD_TYPE, "O+") ?: "O+"
        val allergiesStr = prefs.getString(KEY_ALLERGIES, "Penicillin,Latex") ?: "Penicillin,Latex"
        val allergies = if (allergiesStr.isBlank()) emptyList() else allergiesStr.split(",").map { it.trim() }
        val conditionsStr = prefs.getString(KEY_CONDITIONS, "Type-1 Diabetes,Asthma") ?: "Type-1 Diabetes,Asthma"
        val conditions = if (conditionsStr.isBlank()) emptyList() else conditionsStr.split(",").map { it.trim() }
        val contactName = prefs.getString(KEY_CONTACT_NAME, "Sarah Rivera") ?: "Sarah Rivera"
        val contactRelation = prefs.getString(KEY_CONTACT_RELATION, "Spouse (Phone: +1 555-0192)") ?: "Spouse (Phone: +1 555-0192)"
        val isEncrypted = prefs.getBoolean(KEY_IS_ENCRYPTED, true)

        return MedicalRecord(
            name = name,
            age = age,
            bloodType = bloodType,
            allergies = allergies,
            chronicConditions = conditions,
            emergencyContactName = contactName,
            emergencyContactRelation = contactRelation,
            isEncrypted = isEncrypted,
            encryptedPayloadPreview = "AES256-GCM::9d84f1a0e37bc281...f77a"
        )
    }

    fun saveUserRole(role: UserRole) {
        prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
        _userRole.value = role
    }

    fun saveCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_COMPLETED_ONBOARDING, completed).apply()
        _hasCompletedOnboarding.value = completed
    }

    fun loadIsBroadcasting(): Boolean {
        return prefs.getBoolean(KEY_IS_BROADCASTING, true)
    }

    fun saveIsBroadcasting(broadcasting: Boolean) {
        prefs.edit().putBoolean(KEY_IS_BROADCASTING, broadcasting).apply()
    }

    fun saveGrantedPermissions(granted: Boolean) {
        prefs.edit().putBoolean(KEY_GRANTED_PERMISSIONS, granted).apply()
        _hasGrantedPermissions.value = granted
    }

    fun saveMedicalRecord(record: MedicalRecord) {
        prefs.edit().apply {
            putString(KEY_SURVIVOR_NAME, record.name)
            putInt(KEY_SURVIVOR_AGE, record.age)
            putString(KEY_BLOOD_TYPE, record.bloodType)
            putString(KEY_ALLERGIES, record.allergies.joinToString(","))
            putString(KEY_CONDITIONS, record.chronicConditions.joinToString(","))
            putString(KEY_CONTACT_NAME, record.emergencyContactName)
            putString(KEY_CONTACT_RELATION, record.emergencyContactRelation)
            putBoolean(KEY_IS_ENCRYPTED, record.isEncrypted)
            apply()
        }
        _medicalRecord.value = record
    }

    fun getOrCreateSurvivorId(): String {
        var id = prefs.getString(KEY_SURVIVOR_ID, null)
        if (id == null) {
            id = "USR-" + java.util.UUID.randomUUID().toString().take(6).uppercase()
            prefs.edit().putString(KEY_SURVIVOR_ID, id).apply()
        }
        return id
    }

    companion object {
        private const val KEY_SURVIVOR_ID = "key_survivor_id"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_COMPLETED_ONBOARDING = "key_completed_onboarding"
        private const val KEY_GRANTED_PERMISSIONS = "key_granted_permissions"
        private const val KEY_IS_BROADCASTING = "key_is_broadcasting"
        private const val KEY_SURVIVOR_NAME = "key_survivor_name"
        private const val KEY_SURVIVOR_AGE = "key_survivor_age"
        private const val KEY_BLOOD_TYPE = "key_blood_type"
        private const val KEY_ALLERGIES = "key_allergies"
        private const val KEY_CONDITIONS = "key_conditions"
        private const val KEY_CONTACT_NAME = "key_contact_name"
        private const val KEY_CONTACT_RELATION = "key_contact_relation"
        private const val KEY_IS_ENCRYPTED = "key_is_encrypted"

        @Volatile
        private var instance: AccountRepository? = null

        fun getInstance(context: Context): AccountRepository {
            return instance ?: synchronized(this) {
                instance ?: AccountRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
