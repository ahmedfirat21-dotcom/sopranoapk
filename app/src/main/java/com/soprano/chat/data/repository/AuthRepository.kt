package com.soprano.chat.data.repository

import com.soprano.chat.data.local.TokenManager
import com.soprano.chat.data.network.ApiService
import com.soprano.chat.data.network.AuthResponse
import com.soprano.chat.data.network.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun register(username: String, email: String, passwordHash: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(
                mapOf("username" to username, "email" to email, "password" to passwordHash)
            )
            tokenManager.saveToken(response.token)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, passwordHash: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(
                mapOf("email" to email, "password" to passwordHash)
            )
            tokenManager.saveToken(response.token)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
}
