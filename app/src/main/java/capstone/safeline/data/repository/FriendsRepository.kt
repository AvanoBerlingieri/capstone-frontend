package capstone.safeline.data.repository

import capstone.safeline.apis.ApiServiceFriends
import capstone.safeline.apis.dto.FriendRequest

class FriendRepository(
    private val apiServiceFriends: ApiServiceFriends
) {

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
                Result.failure(Exception("Failed to fetch pending requests: ${response.code()}"))
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
     * Fetches the full list of accepted friends.
     */
    suspend fun getAllFriends(userId: String) = try {
        val response = apiServiceFriends.getAllFriends(userId)
        if (response.isSuccessful) response.body() else null
    } catch (e: Exception) {
        null
    }
}