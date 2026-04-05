package capstone.safeline.apis

import capstone.safeline.apis.dto.messaging.CreateGroupRequest
import capstone.safeline.apis.dto.messaging.GroupInfoDto
import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

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

    @POST("groups/{groupId}/members")
    suspend fun addUserToGroup(
        @Path("groupId") groupId: String,
        @Query("userId") userId: String
    ): Response<Void>

    @DELETE("groups/{groupId}/leave")
    suspend fun leaveGroup(
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