package capstone.safeline.data.repository

import android.util.Log
import capstone.safeline.apis.ApiServiceAuth
import capstone.safeline.apis.dto.GetUserByIdResponse
import capstone.safeline.apis.dto.LoginRequest
import capstone.safeline.apis.dto.RegisterRequest
import capstone.safeline.apis.dto.UpdateEmailDto
import capstone.safeline.apis.dto.UpdatePasswordDto
import capstone.safeline.apis.dto.UpdateUsernameDto
import capstone.safeline.data.local.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepository(
    private val dataStoreManager: DataStoreManager,
    private val apiServiceAuth: ApiServiceAuth
) {
    val tokenFlow: Flow<String?> = dataStoreManager.tokenFlow
    val isLoggedIn: Flow<Boolean> = tokenFlow.map { !it.isNullOrBlank() }
    val usernameFlow = dataStoreManager.usernameFlow
    val emailFlow = dataStoreManager.emailFlow

    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiServiceAuth.logout()

            // Clear local data
            if (response.isSuccessful || response.code() == 401) {
                dataStoreManager.clearAll()
                Result.success(Unit)
            } else {
                dataStoreManager.clearAll()
                Result.failure(Exception("Server error during logout"))
            }
        } catch (e: Exception) {
            // still clear token
            dataStoreManager.clearAll()
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

    suspend fun login(usernameOrEmail: String, password: String): Boolean {
        return try {
            val response = apiServiceAuth.login(LoginRequest(usernameOrEmail, password))
            val body = response.body()

            println("Status Code: ${response.code()}")
            println("Parsed Body: $body")

            if (response.isSuccessful && body != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                    dataStoreManager.saveToken(body.token)
                    dataStoreManager.saveUserInfo(body.username, body.email)
                }
                return true
            }
            false
        } catch (e: Exception) {
            println("Exception during login: ${e.message}")
            false
        }
    }

    suspend fun changeUsername(dto: UpdateUsernameDto) = try {
        val response = apiServiceAuth.changeUsername(dto)
        if (response.isSuccessful) {
            response.body()?.let { dataStoreManager.saveUserInfo(it.username, it.email) }
            true
        } else false
    } catch (e: Exception) {
        false
    }

    suspend fun changeEmail(dto: UpdateEmailDto) = try {
        val response = apiServiceAuth.changeEmail(dto)
        if (response.isSuccessful) {
            response.body()?.let { dataStoreManager.saveUserInfo(it.username, it.email) }
            true
        } else false
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
        if (response.isSuccessful){
            dataStoreManager.clearAll()
        }
        response.isSuccessful
    } catch (e: Exception) {
        false
    }

    suspend fun getUserById(id: UUID): Result<GetUserByIdResponse> {
        return try {
            val primary = apiServiceAuth.getUserById(id)
            val primaryBody = primary.body()
            if (primary.isSuccessful && primaryBody != null) {
                return Result.success(primaryBody)
            }
            Log.e("AuthRepository", "getUserById primary failed: id=$id code=${primary.code()}")

            val tertiary = apiServiceAuth.getUserByIdUnderUsers(id)
            val tertiaryBody = tertiary.body()
            if (tertiary.isSuccessful && tertiaryBody != null) {
                return Result.success(tertiaryBody)
            }
            Log.e("AuthRepository", "getUserById /users failed: id=$id code=${tertiary.code()}")

            Result.failure(
                Exception(
                    "Failed to fetch user by id: api=${primary.code()}, users=${tertiary.code()}"
                )
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "getUserById exception: id=$id message=${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getIdByUsername(username: String): Result<String> {
        return try {
            val trimmed = username.trim()
            if (trimmed.isEmpty()) {
                return Result.failure(Exception("Username is empty"))
            }

            val response = apiServiceAuth.getIdByUsername(trimmed)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val id = body["id"] ?: body["userId"]
                if (!id.isNullOrBlank()) {
                    return Result.success(id)
                }
            }
            Result.failure(Exception("Failed to fetch username"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "getIdByUsername exception: username=$username message=${e.message}", e)
            Result.failure(e)
        }
    }
}