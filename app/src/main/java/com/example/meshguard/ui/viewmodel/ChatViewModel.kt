package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.ChatMessage
import com.example.meshguard.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceRecordingState(
    val isRecording: Boolean = false,
    val durationSec: Int = 0,
    val maxDurationSec: Int = 15,
    val isPlayingPreview: Boolean = false,
    val liveAmplitudes: List<Float> = emptyList()
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val recordingState: VoiceRecordingState = VoiceRecordingState(),
    val activePlayingVoiceMessageId: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository = AppDependencies.chatRepository
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    private val _recordingState = MutableStateFlow(VoiceRecordingState())
    private val _activePlayingVoiceMessageId = MutableStateFlow<String?>(null)

    private var recordingTimerJob: Job? = null

    val uiState: StateFlow<ChatUiState> = combine(
        chatRepository.messages,
        _inputText,
        _recordingState,
        _activePlayingVoiceMessageId
    ) { messages, text, recState, playingId ->
        ChatUiState(
            messages = messages,
            inputText = text,
            recordingState = recState,
            activePlayingVoiceMessageId = playingId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun onSendTextMessage() {
        val text = _inputText.value.trim()
        if (text.isNotBlank()) {
            chatRepository.sendTextMessage(text)
            _inputText.value = ""
        }
    }

    /** Called when a quick-reply chip is tapped. Sends the preset text immediately. */
    fun onSendQuickReply(text: String) {
        if (text.isNotBlank()) {
            chatRepository.sendTextMessage(text)
        }
    }

    fun onStartRecording() {
        recordingTimerJob?.cancel()
        _recordingState.value = VoiceRecordingState(
            isRecording = true,
            durationSec = 0,
            liveAmplitudes = listOf(0.2f, 0.4f, 0.3f)
        )

        recordingTimerJob = viewModelScope.launch {
            for (sec in 1..15) {
                delay(1000L)
                if (!_recordingState.value.isRecording) break

                val nextAmp = (30..95).random() / 100f
                _recordingState.update { current ->
                    current.copy(
                        durationSec = sec,
                        liveAmplitudes = (current.liveAmplitudes + nextAmp).takeLast(20)
                    )
                }

                if (sec >= 15) {
                    onStopRecording(sendImmediately = true)
                    break
                }
            }
        }
    }

    fun onStopRecording(sendImmediately: Boolean) {
        recordingTimerJob?.cancel()
        val duration = _recordingState.value.durationSec.coerceAtLeast(1)

        if (sendImmediately) {
            chatRepository.sendVoiceDropMessage(
                audioDurationSec = duration,
                voiceBase64Payload = "VOICE_SAMPLE_OPUS_16K_RECORDING"
            )
            _recordingState.value = VoiceRecordingState()
        } else {
            _recordingState.update { it.copy(isRecording = false) }
        }
    }

    fun onCancelRecording() {
        recordingTimerJob?.cancel()
        _recordingState.value = VoiceRecordingState()
    }

    fun onConfirmSendRecordedVoice() {
        val duration = _recordingState.value.durationSec.coerceAtLeast(1)
        chatRepository.sendVoiceDropMessage(
            audioDurationSec = duration,
            voiceBase64Payload = "VOICE_SAMPLE_OPUS_16K_RECORDING"
        )
        _recordingState.value = VoiceRecordingState()
    }

    fun onToggleVoiceMessagePlayback(messageId: String) {
        _activePlayingVoiceMessageId.update { current ->
            if (current == messageId) null else messageId
        }
    }
}
