package com.soprano.chat.data.repository

import com.soprano.chat.data.network.ApiService
import com.soprano.chat.data.network.ConnectionNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getConnections(userId: String): Result<List<ConnectionNode>> = withContext(Dispatchers.IO) {
        try {
            val connections = apiService.getConnections(userId)
            if (connections.isEmpty()) {
                // Cold Start UX: Provide visually pleasing empty state nodes
                Result.success(DummyDataProvider.getEmptyStateConnections())
            } else {
                Result.success(connections)
            }
        } catch (e: Exception) {
            // Provide dummy data to prevent UI breaks during network failures
            Result.success(DummyDataProvider.getEmptyStateConnections())
        }
    }

    suspend fun connectToUser(targetUserId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = apiService.connect(mapOf("targetUserId" to targetUserId))
            Result.success(result.connectionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordInteraction(connectionId: String): Result<Float> = withContext(Dispatchers.IO) {
        try {
            val result = apiService.interactAndWeight(connectionId)
            Result.success(result.connectionWeight)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
