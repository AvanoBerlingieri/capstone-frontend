package capstone.safeline.data.repository

import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.apis.ApiServiceAuth
import capstone.safeline.apis.dto.RegisterRequest
import kotlinx.coroutines.flow.*

class AuthRepository(
    private val dataStoreManager: DataStoreManager,
    private val apiServiceAuth: ApiServiceAuth
) {
    val tokenFlow: Flow<String?> = dataStoreManager.tokenFlow
    val isLoggedIn: Flow<Boolean> = tokenFlow.map { !it.isNullOrBlank() }

    suspend fun saveToken(token: String) = dataStoreManager.saveToken(token)

    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiServiceAuth.logout()

            // Clear local data
            if (response.isSuccessful || response.code() == 401) {
                dataStoreManager.clearToken()
                Result.success(Unit)
            } else {
                dataStoreManager.clearToken()
                Result.failure(Exception("Server error during logout"))
            }
        } catch (e: Exception) {
            // still clear token
            dataStoreManager.clearToken()
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Boolean {
        return try {
            val response = apiServiceAuth.register(RegisterRequest(username, email, password))
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}