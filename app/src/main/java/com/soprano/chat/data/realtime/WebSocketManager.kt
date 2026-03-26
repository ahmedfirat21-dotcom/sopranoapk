package com.soprano.chat.data.realtime

import com.soprano.chat.data.local.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenManager: TokenManager
) {
    private var webSocket: WebSocket? = null
    private val WS_URL = "ws://10.0.2.2:3000/ws" 
    private val scope = CoroutineScope(Dispatchers.IO)

    // Events
    private val _incomingWhispers = MutableSharedFlow<JSONObject>()
    val incomingWhispers: SharedFlow<JSONObject> = _incomingWhispers.asSharedFlow()

    private val _incomingCalls = MutableSharedFlow<JSONObject>()
    val incomingCalls: SharedFlow<JSONObject> = _incomingCalls.asSharedFlow()

    fun connect() {
        val request = Request.Builder().url(WS_URL).build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Send authentication payload immediately
                val token = tokenManager.getToken()
                if (token != null) {
                    val authMessage = JSONObject().apply {
                        put("type", "auth")
                        put("token", token)
                    }
                    webSocket.send(authMessage.toString())
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "whisper_received" -> scope.launch { _incomingWhispers.emit(json) }
                        "incoming_call" -> scope.launch { _incomingCalls.emit(json) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                // Implement automatic reconnect logic here
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    fun emit(eventData: JSONObject) {
        webSocket?.send(eventData.toString())
    }
}
