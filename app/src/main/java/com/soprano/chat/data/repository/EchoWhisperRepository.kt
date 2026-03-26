package com.soprano.chat.data.repository

import com.soprano.chat.data.network.ApiService
import com.soprano.chat.data.network.EchoNode
import com.soprano.chat.data.network.PresignedUrlResponse
import com.soprano.chat.data.network.WhisperMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EchoWhisperRepository @Inject constructor(
    private val apiService: ApiService
) {
    // ── Echoes ──
    suspend fun getEchoes(cursor: String? = null): Result<List<EchoNode>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEchoes(cursor)
            if (response.echoes.isEmpty() && cursor == null) {
                // Cold Start UX: Soprano Founder Hoş Geldin Yankısı
                Result.success(DummyDataProvider.getWelcomeEcho())
            } else {
                Result.success(response.echoes)
            }
        } catch (e: Exception) {
            // Prevent empty screen on network error
            Result.success(if (cursor == null) DummyDataProvider.getWelcomeEcho() else emptyList())
        }
    }

    suspend fun createEcho(mediaUrl: String, mediaType: String, duration: Int): Result<EchoNode> = withContext(Dispatchers.IO) {
        try {
            val body = mapOf(
                "mediaUrl" to mediaUrl,
                "mediaType" to mediaType,
                "duration" to duration
            )
            val echo = apiService.createEcho(body)
            Result.success(echo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resonateEcho(echoId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resonateEcho(echoId)
            Result.success(response.resonated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Whispers ──
    suspend fun getWhisperHistory(contactId: String): Result<List<WhisperMessage>> = withContext(Dispatchers.IO) {
        try {
            val whispers = apiService.getWhisperHistory(contactId)
            Result.success(whispers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendWhisper(receiverId: String, mediaUrl: String, duration: Int): Result<WhisperMessage> = withContext(Dispatchers.IO) {
        try {
            val body = mapOf(
                "receiverId" to receiverId,
                "mediaUrl" to mediaUrl,
                "mediaType" to "AUDIO",
                "duration" to duration
            )
            val whisper = apiService.sendWhisper(body)
            Result.success(whisper)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Media Storage Upload ──
    suspend fun getPresignedUrl(mediaType: String, duration: Int): Result<PresignedUrlResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPresignedUrl(
                mapOf("mediaType" to mediaType, "duration" to duration)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload with standard OkHttpClient (Direct to Cloudflare R2)
    suspend fun uploadToR2(presignedUrl: String, fileBytes: ByteArray, contentType: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val requestBody = fileBytes.toRequestBody(contentType.toMediaTypeOrNull())
            val request = okhttp3.Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build()

            val client = okhttp3.OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
