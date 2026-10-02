package com.example.meshguard.data.model

enum class MessageType {
    TEXT, VOICE
}

enum class DeliveryState {
    STORED, RELAYED, DELIVERED_TO_RESCUER
}

/**
 * Offline mesh chat or voice drop message.
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: MessageType = MessageType.TEXT,
    val content: String = "",
    val audioDurationSec: Int = 0,
    val hopCount: Int = 0,
    val deliveryState: DeliveryState = DeliveryState.STORED,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false
)
