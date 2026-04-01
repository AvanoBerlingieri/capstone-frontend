package capstone.safeline.apis

import capstone.safeline.apis.dto.GetUserByIdResponse
import capstone.safeline.apis.dto.GetUserIdByUsernameResponse
import capstone.safeline.apis.dto.LoginRequest
import capstone.safeline.apis.dto.LoginResponse
import capstone.safeline.apis.dto.RegisterRequest
import capstone.safeline.apis.dto.RegisterResponse
import capstone.safeline.apis.dto.UpdateEmailDto
import capstone.safeline.apis.dto.UpdatePasswordDto
import capstone.safeline.apis.dto.UpdateResponseDto
import capstone.safeline.apis.dto.UpdateUsernameDto
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

    @GET("auth/users/{id}")
    suspend fun getUserById(@Path("id") id: UUID): retrofit2.Response<GetUserByIdResponse>

    @GET("users/{id}")
    suspend fun getUserByIdUnderUsers(
        @Path("id") id: UUID
    ): retrofit2.Response<GetUserByIdResponse>

    @GET("auth/users/get-id-by-username/{username}")
    suspend fun getIdByUsername(
        @Path("username") username: String
    ): retrofit2.Response<GetUserIdByUsernameResponse>

}