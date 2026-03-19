package capstone.safeline.apis

import capstone.safeline.apis.dto.LoginRequest
import capstone.safeline.apis.dto.LoginResponse
import capstone.safeline.apis.dto.RegisterRequest
import capstone.safeline.apis.dto.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServiceAuth {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest):
            retrofit2.Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest):
            retrofit2.Response<RegisterResponse>

    @POST("auth/logout")
    suspend fun logout(): retrofit2.Response<Void>

}