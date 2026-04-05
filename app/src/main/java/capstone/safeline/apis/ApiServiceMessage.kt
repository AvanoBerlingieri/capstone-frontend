package capstone.safeline.apis

import capstone.safeline.apis.dto.messaging.CreateGroupRequest
import capstone.safeline.apis.dto.messaging.GroupInfoDto
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.RenameGroupRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServiceMessage {

    // History Routes

    @GET("history/private/{userId}")
    suspend fun getPrivateHistory(
        @Path("userId") userId: String
    ): Response<List<OutgoingMessage>>

    @GET("history/group/{groupId}")
    suspend fun getGroupHistory(
        @Path("groupId") groupId: String
    ): Response<List<OutgoingGroupMessage>>

    // Group Management

    @POST("groups")
    suspend fun createGroup(
        @Body request: CreateGroupRequest
    ): Response<CreateGroupRequest>

    /** Add member — POST with `userId` query only (no body; avoids Retrofit/Spring issues with nullable @Body). */
    @POST("groups/{groupId}/members")
    suspend fun addUserToGroup(
        @Path("groupId") groupId: String,
        @Query("userId") userId: String
    ): Response<Void>

    /**
     * List members — GET is 405 on this server; use POST. Separate methods so invite/add never sends a JSON body.
     */
    @POST("groups/{groupId}/members")
    suspend fun listGroupMembersWithBody(
        @Path("groupId") groupId: String,
        @Body body: RequestBody
    ): Response<ResponseBody>

    @POST("groups/{groupId}/members")
    suspend fun listGroupMembersNoBody(
        @Path("groupId") groupId: String
    ): Response<ResponseBody>

    @PUT("groups/{groupId}")
    suspend fun renameGroupChat(
        @Path("groupId") groupId: String,
        @Body body: RenameGroupRequest
    ): Response<Void>

    /** Matches backend: DELETE /groups/{groupId}/leave — caller from JWT [Principal] only. */
    @DELETE("groups/{groupId}/leave")
    suspend fun leaveGroupApi(
        @Path("groupId") groupId: String
    ): Response<Void>

    @DELETE("groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: String
    ): Response<Void>

    @GET("groups/{userId}")
    suspend fun getAllUserGroups(
        @Path("userId") userId: String
    ): Response<List<GroupInfoDto>>
}