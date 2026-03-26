package com.soprano.chat.data.repository

import com.soprano.chat.data.network.ApiService
import com.soprano.chat.data.network.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuraRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUserProfile(userId: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val profile = apiService.getUserProfile(userId)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(updates: Map<String, String>): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val profile = apiService.updateProfile(updates)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAura(query: String): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        try {
            val results = apiService.searchUsers(query)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
