package capstone.safeline.api
import capstone.safeline.api.dto.LoginRequest
import capstone.safeline.api.dto.LoginResponse
import capstone.safeline.api.dto.RegisterRequest
import capstone.safeline.api.dto.RegisterResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/createUser")
    suspend fun createUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    @POST("api/loginUser")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
}