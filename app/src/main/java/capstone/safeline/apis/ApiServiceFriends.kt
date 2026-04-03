package capstone.safeline.apis

import capstone.safeline.apis.dto.friends.FriendRequest
import capstone.safeline.apis.dto.friends.FriendResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiServiceFriends {
    @POST("friendRequest")
    suspend fun friendRequest(@Body request: FriendRequest):
            retrofit2.Response<FriendResponse>

    @POST("handleFriendRequest")
    suspend fun handleFriendRequest(@Body request: FriendRequest):
            retrofit2.Response<ResponseBody>

    @GET("getAllFriends/{user_id}")
    suspend fun getAllFriends(@Path("user_id") userId: String):
            retrofit2.Response<ResponseBody>

    @GET("getAllPendingRequests/{user_id}")
    suspend fun getPendingRequests(@Path("user_id") userId: String):
            retrofit2.Response<List<String>>


}