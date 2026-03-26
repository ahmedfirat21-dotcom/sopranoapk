package com.soprano.chat.data.network

import retrofit2.http.*

interface ApiService {

    // ── Auth ──
    @POST("/api/auth/register")
    suspend fun register(@Body body: Map<String, String>): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body body: Map<String, String>): AuthResponse

    // ── Users / Aura ──
    @GET("/api/users/{id}")
    suspend fun getUserProfile(@Path("id") id: String): UserProfile

    @PATCH("/api/users/me")
    suspend fun updateProfile(@Body body: Map<String, String>): UserProfile

    @GET("/api/users/search")
    suspend fun searchUsers(@Query("q") query: String): List<UserProfile>

    // ── Connections ──
    @GET("/api/connections/{userId}")
    suspend fun getConnections(@Path("userId") userId: String): List<ConnectionNode>

    @POST("/api/connections")
    suspend fun connect(@Body body: Map<String, String>): ConnectionResult

    @PATCH("/api/connections/{id}/interact")
    suspend fun interactAndWeight(@Path("id") connectionId: String): InteractionResult

    // ── Echoes & Whispers ──
    @GET("/api/echoes")
    suspend fun getEchoes(@Query("cursor") cursor: String? = null): EchoResponse

    @POST("/api/echoes")
    suspend fun createEcho(@Body body: Map<String, Any>): EchoNode

    @POST("/api/echoes/{id}/resonate")
    suspend fun resonateEcho(@Path("id") echoId: String): ResonateResponse

    @GET("/api/whispers/{contactId}")
    suspend fun getWhisperHistory(@Path("contactId") contactId: String): List<WhisperMessage>

    @POST("/api/whispers")
    suspend fun sendWhisper(@Body body: Map<String, Any>): WhisperMessage

    // ── Media ──
    @POST("/api/media/upload-url")
    suspend fun getPresignedUrl(@Body body: Map<String, Any>): PresignedUrlResponse

    // ── Rooms ──
    @GET("/api/rooms")
    suspend fun getLiveRooms(@Query("active") active: Boolean = true): List<RoomNode>

    @POST("/api/rooms/{id}/join")
    suspend fun joinRoom(@Path("id") roomId: String): RoomJoinResponse
}

// ── Models ──
data class AuthResponse(val user: UserProfile, val token: String)
data class UserProfile(
    val id: String, 
    val username: String, 
    val displayName: String?, 
    val avatarUrl: String?, 
    val auraColor: String?, 
    val resonanceScore: Int,
    val isOnline: Boolean
)
data class ConnectionNode(val id: String, val user: UserProfile, val connectionWeight: Float, val interactionCount: Int)
data class ConnectionResult(val connectionId: String)
data class InteractionResult(val connectionWeight: Float, val interactionCount: Int)
data class EchoNode(val id: String, val author: UserProfile, val mediaUrl: String, val mediaType: String, val duration: Int)
data class EchoResponse(val echoes: List<EchoNode>, val nextCursor: String?)
data class ResonateResponse(val resonated: Boolean)
data class WhisperMessage(val id: String, val senderId: String, val receiverId: String, val mediaUrl: String, val isResonated: Boolean)
data class PresignedUrlResponse(val uploadUrl: String, val mediaId: String, val cdnUrl: String)
data class RoomNode(val id: String, val title: String, val livekitRoomName: String, val host: UserProfile, val participantCount: Int)
data class RoomJoinResponse(val livekitToken: String?, val livekitUrl: String?)
