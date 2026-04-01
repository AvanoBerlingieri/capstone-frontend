package capstone.safeline.apis.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiServiceMessage {

    @PUT("messages/{messageUuid}/status")
    suspend fun updateMessageStatus(
        @Path("messageUuid") messageUuid: String,
        @Body request: Map<String, String>
    ): Response<Void>
}