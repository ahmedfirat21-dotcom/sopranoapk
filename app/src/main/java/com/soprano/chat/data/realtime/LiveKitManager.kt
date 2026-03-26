package com.soprano.chat.data.realtime

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.room.Room
import io.livekit.android.events.RoomEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveKitManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var room: Room? = null

    // VAD (Voice Activity Detection): Which user is speaking with what volume level
    private val _activeSpeakers = MutableStateFlow<Map<String, Float>>(emptyMap())
    val activeSpeakers: StateFlow<Map<String, Float>> = _activeSpeakers.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO)

    fun joinRoom(url: String, token: String) {
        val newRoom = LiveKit.create(
            appContext = context,
            options = RoomOptions(
                adaptiveStream = true,
                dynacast = true
            )
        )
        room = newRoom

        // Listen for active speaker changes using Coroutines Flow in SDK v2.x
        scope.launch {
            newRoom.events.events.collect { event ->
                if (event is RoomEvent.ActiveSpeakersChanged) {
                    val currentSpeakers = mutableMapOf<String, Float>()
                    event.speakers.forEach { participant ->
                        if (participant.isSpeaking) {
                            currentSpeakers[participant.identity?.toString() ?: ""] = participant.audioLevel
                        }
                    }
                    _activeSpeakers.value = currentSpeakers
                }
            }
        }

        scope.launch {
            newRoom.connect(url, token)
        }
    }

    fun leaveRoom() {
        scope.launch {
            room?.disconnect()
        }
        room = null
        _activeSpeakers.value = emptyMap()
    }

    fun toggleMicrophone(enabled: Boolean) {
        scope.launch {
            room?.localParticipant?.setMicrophoneEnabled(enabled)
        }
    }

    fun toggleCamera(enabled: Boolean) {
        scope.launch {
            room?.localParticipant?.setCameraEnabled(enabled)
        }
    }
}
