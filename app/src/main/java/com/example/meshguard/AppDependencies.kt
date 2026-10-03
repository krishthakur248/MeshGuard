package com.example.meshguard

import android.content.Context
import com.example.meshguard.data.local.MeshGuardDatabase
import com.example.meshguard.data.repository.ChatRepository
import com.example.meshguard.data.repository.FakeChatRepository
import com.example.meshguard.data.repository.FakeMedicalIdRepository
import com.example.meshguard.data.repository.LocationProvider
import com.example.meshguard.data.repository.MedicalIdRepository
import com.example.meshguard.data.repository.MeshRepository
import com.example.meshguard.data.repository.NearbyMeshRepository
import com.example.meshguard.data.repository.RoomSurvivorRepository
import com.example.meshguard.data.repository.SurvivorRepository

object AppDependencies {
    lateinit var appContext: Context
    lateinit var meshRepository: MeshRepository
    lateinit var survivorRepository: SurvivorRepository
    lateinit var chatRepository: ChatRepository
    lateinit var medicalIdRepository: MedicalIdRepository
    lateinit var locationProvider: LocationProvider

    /**
     * Only initializes once. Safe to call from every onCreate —
     * subsequent calls are no-ops so the existing Nearby sessions
     * (advertising/discovery) are not orphaned.
     */
    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext

        // Step 8: Create location provider for real GPS coordinates in packets
        locationProvider = LocationProvider(context)

        val database = MeshGuardDatabase.getInstance(context)
        val survivorRepo = RoomSurvivorRepository(
            context = context,
            packetDao = database.packetDao(),
            locationProvider = locationProvider
        )
        survivorRepository = survivorRepo
        meshRepository = NearbyMeshRepository(
            context,
            survivorRepository = survivorRepo,
            packetDao = database.packetDao()
        )
        chatRepository = FakeChatRepository()
        medicalIdRepository = FakeMedicalIdRepository()
    }
}
