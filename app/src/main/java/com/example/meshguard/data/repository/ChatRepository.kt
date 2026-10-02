package com.example.meshguard.data.repository

import com.example.meshguard.data.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val messages: StateFlow<List<ChatMessage>>

    fun sendTextMessage(content: String)
    fun sendVoiceDropMessage(audioDurationSec: Int, voiceBase64Payload: String)
    fun markDeliveredToRescuer(messageId: String)
}
