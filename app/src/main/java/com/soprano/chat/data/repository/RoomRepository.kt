package com.soprano.chat.data.repository

import com.soprano.chat.data.network.ApiService
import com.soprano.chat.data.network.RoomJoinResponse
import com.soprano.chat.data.network.RoomNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getLiveRooms(): Result<List<RoomNode>> = withContext(Dispatchers.IO) {
        try {
            val rooms = apiService.getLiveRooms(active = true)
            if (rooms.isEmpty()) {
                // Cold Start UX: Fallback to dummy data
                Result.success(DummyDataProvider.getLoungeFallback())
            } else {
                Result.success(rooms)
            }
        } catch (e: Exception) {
            // Provide dummy data so the UI doesn't break even on network failure
            Result.success(DummyDataProvider.getLoungeFallback())
        }
    }

    suspend fun joinRoom(roomId: String): Result<RoomJoinResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.joinRoom(roomId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
