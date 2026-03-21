package capstone.safeline.data.repository

import capstone.safeline.apis.ApiServiceAuth
import capstone.safeline.apis.dto.RegisterRequest
import capstone.safeline.apis.dto.UpdateEmailDto
import capstone.safeline.apis.dto.UpdatePasswordDto
import capstone.safeline.apis.dto.UpdateUsernameDto
import capstone.safeline.data.local.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    suspend fun changeUsername(dto: UpdateUsernameDto) = try {
        apiServiceAuth.changeUsername(dto).isSuccessful
    } catch (e: Exception) {
        false
    }

    suspend fun changeEmail(dto: UpdateEmailDto) = try {
        apiServiceAuth.changeEmail(dto).isSuccessful
    } catch (e: Exception) {
        false
    }

    suspend fun updatePassword(dto: UpdatePasswordDto) = try {
        apiServiceAuth.updatePassword(dto).isSuccessful
    } catch (e: Exception) {
        false
    }

    suspend fun deleteAccount() = try {
        val response = apiServiceAuth.deleteAccount()
        if (response.isSuccessful) dataStoreManager.clearToken()
        response.isSuccessful
    } catch (e: Exception) {
        false
    }
}