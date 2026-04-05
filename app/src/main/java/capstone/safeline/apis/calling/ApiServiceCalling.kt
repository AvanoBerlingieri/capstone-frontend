package capstone.safeline.apis.calling

import capstone.safeline.models.CallRecord
import capstone.safeline.models.GroupRoom
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiServiceCalling {

    @GET("calls/history/{userId}")
    suspend fun getCallHistory(@Path("userId") userId: String): Response<List<CallRecord>>

    @POST("calls/save")
    suspend fun saveCall(@Body record: CallRecord): Response<CallRecord>

    @POST("group-calls/create/{creatorId}")
    suspend fun createGroupRoom(@Path("creatorId") creatorId: String): Response<GroupRoom>

    @POST("group-calls/join/{roomId}/{userId}")
    suspend fun joinGroupRoom(@Path("roomId") roomId: String, @Path("userId") userId: String): Response<GroupRoom>

    @POST("group-calls/leave/{roomId}/{userId}")
    suspend fun leaveGroupRoom(@Path("roomId") roomId: String, @Path("userId") userId: String): Response<Void>
}