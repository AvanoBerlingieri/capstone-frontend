package capstone.safeline.apis

import capstone.safeline.apis.dto.auth.GetUserByIdResponse
import capstone.safeline.apis.dto.auth.LoginRequest
import capstone.safeline.apis.dto.auth.LoginResponse
import capstone.safeline.apis.dto.auth.RegisterRequest
import capstone.safeline.apis.dto.auth.RegisterResponse
import capstone.safeline.apis.dto.auth.UpdateEmailDto
import capstone.safeline.apis.dto.auth.UpdatePasswordDto
import capstone.safeline.apis.dto.auth.UpdateResponseDto
import capstone.safeline.apis.dto.auth.UpdateUsernameDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

interface ApiServiceAuth {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest):
            retrofit2.Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest):
            retrofit2.Response<RegisterResponse>

    @POST("api/auth/logout")
    suspend fun logout(): retrofit2.Response<Void>

    @DELETE("api/auth/delete")
    suspend fun deleteAccount(): retrofit2.Response<Void>

    @PUT("api/auth/password")
    suspend fun updatePassword(@Body request: UpdatePasswordDto): retrofit2.Response<Void>

    @PUT("api/auth/username")
    suspend fun changeUsername(@Body request: UpdateUsernameDto): retrofit2.Response<UpdateResponseDto>

    @PUT("api/auth/email")
    suspend fun changeEmail(@Body request: UpdateEmailDto): retrofit2.Response<UpdateResponseDto>

    /** Same module as [login] — try this first. */
    @GET("api/auth/users/{id}")
    suspend fun getUserById(@Path("id") id: UUID): retrofit2.Response<GetUserByIdResponse>

    @GET("users/{id}")
    suspend fun getUserByIdUnderUsers(
        @Path("id") id: UUID
    ): retrofit2.Response<GetUserByIdResponse>

    @GET("api/auth/users/get-id-by-username/{username}")
    suspend fun getIdByUsername(
        @Path("username") username: String
    ): retrofit2.Response<Map<String, String>>


}