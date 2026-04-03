package capstone.safeline.data.repository

import android.content.Context
import android.util.Log
import capstone.safeline.apis.ApiServiceFriends
import capstone.safeline.apis.dto.friends.FriendRequest
import capstone.safeline.apis.network.ApiClientAuth
import capstone.safeline.apis.network.ApiClientFriends
import capstone.safeline.data.local.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

class FriendRepository(
    private val apiServiceFriends: ApiServiceFriends
) {
    companion object {
        @Volatile
        private var INSTANCE: FriendRepository? = null

        fun getInstance(context: Context): FriendRepository {
            return INSTANCE ?: synchronized(this) {
                val ds = DataStoreManager.getInstance(context)
                val api = ApiClientFriends.provideService(context, ds)

                INSTANCE ?: FriendRepository(api).also { INSTANCE = it }
            }
        }
    }

    /**
     * Fetches all pending friend request UUIDs for the current user.
     * Maps the response to a simple List<String> or List<UUID>.
     */
    suspend fun getPendingRequests(userId: String): Result<List<String>> {
        return try {
            val response = apiServiceFriends.getPendingRequests(userId)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val detail = response.errorBody()?.use { it.string() }?.trim().orEmpty()
                val msg = buildString {
                    append("Failed to fetch pending requests: ${response.code()}")
                    if (detail.isNotEmpty()) append(" — ").append(detail)
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a new friend request.
     */
    suspend fun sendFriendRequest(senderId: String, receiverId: String): Boolean {
        return try {
            val request = FriendRequest(senderId, receiverId, "PENDING")
            val response = apiServiceFriends.friendRequest(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Accepts or Declines a friend request.
     * @param status should be "ACCEPTED" or "DECLINED"
     */
    suspend fun handleFriendRequest(senderId: String, receiverId: String, status: String): Boolean {
        return try {
            val request = FriendRequest(senderId, receiverId, status)
            val response = apiServiceFriends.handleFriendRequest(request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetches accepted friend user ids. Accepts common JSON shapes from the friends service:
     */
    suspend fun getAllFriends(userId: String): Result<List<String>> {
        return try {
            val response = apiServiceFriends.getAllFriends(userId)
            if (!response.isSuccessful) {
                val detail = response.errorBody()?.use { it.string() }?.trim().orEmpty()
                Log.e(
                    "FriendRepository",
                    "getAllFriends HTTP ${response.code()} userId=$userId detail=$detail"
                )
                return Result.failure(
                    Exception(
                        buildString {
                            append("Friends list failed: ${response.code()}")
                            if (detail.isNotEmpty()) append(" — ").append(detail)
                        }
                    )
                )
            }
            val raw = response.body()?.use { it.string() }?.trim().orEmpty()
            if (raw.isEmpty()) {
                return Result.success(emptyList())
            }
            val ids = parseFriendIdsJson(raw)
            Log.d("FriendRepository", "getAllFriends userId=$userId count=${ids.size}")
            Result.success(ids)
        } catch (e: Exception) {
            Log.e("FriendRepository", "getAllFriends exception userId=$userId", e)
            Result.failure(e)
        }
    }

    private fun parseFriendIdsJson(raw: String): List<String> {
        val trimmed = raw.trim()
        val gson = Gson()
        val listType = object : TypeToken<List<String>>() {}.type
        return when {
            trimmed.startsWith("[") -> gson.fromJson(trimmed, listType) ?: emptyList()
            trimmed.startsWith("{") -> {
                val obj = JsonParser.parseString(trimmed).asJsonObject
                val arr = when {
                    obj.has("friendIds") -> obj.get("friendIds")
                    obj.has("friend_ids") -> obj.get("friend_ids")
                    obj.has("friends") -> obj.get("friends")
                    else -> null
                } ?: return emptyList()
                gson.fromJson(arr, listType) ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
