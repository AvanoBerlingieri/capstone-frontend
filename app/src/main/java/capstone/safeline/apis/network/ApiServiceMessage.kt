package capstone.safeline.apis.network

import capstone.safeline.apis.dto.UpdateMessageStatusDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiServiceMessage {

    @PUT("messages/{messageUuid}/status")
    suspend fun updateMessageStatus(
        @Path("messageUuid") messageUuid: String,
        @Body request: UpdateMessageStatusDto
    ): Response<Void>
}