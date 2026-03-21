package capstone.safeline.apis

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
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiServiceAuth {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest):
            retrofit2.Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest):
            retrofit2.Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): retrofit2.Response<Void>

    @DELETE("auth/delete")
    suspend fun deleteAccount(): retrofit2.Response<Void>

    @PUT("auth/password")
    suspend fun updatePassword(@Body request: UpdatePasswordDto): retrofit2.Response<Void>

    @PUT("auth/username")
    suspend fun changeUsername(@Body request: UpdateUsernameDto): retrofit2.Response<UpdateResponseDto>

    @PUT("auth/email")
    suspend fun changeEmail(@Body request: UpdateEmailDto): retrofit2.Response<UpdateResponseDto>

}