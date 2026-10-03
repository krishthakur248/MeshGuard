package com.example.meshguard

import android.content.Context
import com.example.meshguard.data.local.MeshGuardDatabase
import com.example.meshguard.data.repository.AccountRepository
import com.example.meshguard.data.repository.ChatRepository
import com.example.meshguard.data.repository.FakeMedicalIdRepository
import com.example.meshguard.data.repository.LocationProvider
import com.example.meshguard.data.repository.MedicalIdRepository
import com.example.meshguard.data.repository.MeshRepository
import com.example.meshguard.data.repository.NearbyMeshChatRepository
import com.example.meshguard.data.repository.NearbyMeshRepository
import com.example.meshguard.data.repository.PersistentMedicalIdRepository
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

        locationProvider = LocationProvider(context)

        val database = MeshGuardDatabase.getInstance(context)
        val survivorRepo = RoomSurvivorRepository(
            context = context,
            packetDao = database.packetDao(),
            locationProvider = locationProvider
        )
        survivorRepository = survivorRepo

        // Step 12: Create ONE NearbyMeshRepository (chatRepository starts null).
        val nearbyRepo = NearbyMeshRepository(
            context = context,
            survivorRepository = survivorRepo,
            packetDao = database.packetDao()
            // chatRepository left null — wired below via setChatRepository()
        )

        // Step 12: Create the chat repository, injecting lambdas that call into
        // the SAME nearbyRepo instance that the app will use for all connections.
        val realChatRepo = NearbyMeshChatRepository(
            chatDao = database.chatDao(),
            accountRepository = AccountRepository.getInstance(context),
            sendRawToAllPeers = { bytes -> nearbyRepo.sendRawToAllPeers(bytes) },
            sendRawToAllPeersExcept = { bytes, exclude ->
                nearbyRepo.sendRawToAllPeersExcept(bytes, exclude)
            }
        )

        // Wire chat repo into the same nearbyRepo so incoming CHAT payloads are dispatched.
        nearbyRepo.setChatRepository(realChatRepo)

        meshRepository = nearbyRepo
        chatRepository = realChatRepo
        medicalIdRepository = PersistentMedicalIdRepository(AccountRepository.getInstance(context))
    }
}
