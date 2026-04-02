package capstone.safeline.apis

import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import retrofit2.Response
import retrofit2.http.*
import java.util.UUID

interface ApiServiceMessage {

    // History Routes

    @GET("history/private/{userId}")
    suspend fun getPrivateHistory(
        @Path("userId") userId: UUID
    ): Response<List<OutgoingMessage>>

    @GET("history/group/{groupId}")
    suspend fun getGroupHistory(
        @Path("groupId") groupId: UUID
    ): Response<List<OutgoingGroupMessage>>

    // Group Management

    @POST("groups")
    suspend fun createGroup(
        @Query("name") name: String
    ): Response<Void>

    @POST("groups/{groupId}/members")
    suspend fun addUserToGroup(
        @Path("groupId") groupId: UUID,
        @Query("userId") userId: UUID
    ): Response<Void>

    @DELETE("groups/{groupId}/leave")
    suspend fun leaveGroup(
        @Path("groupId") groupId: UUID
    ): Response<Void>

    @DELETE("groups/{groupId}")
    suspend fun deleteGroup(
        @Path("groupId") groupId: UUID
    ): Response<Void>
}