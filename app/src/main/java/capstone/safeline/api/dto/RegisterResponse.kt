package capstone.safeline.api.dto

import java.util.Date

data class RegisterResponse(
    val id: String,
    val email: String,
    val username: String,
    val password: String,
    val status: String,
    val created_at: Date
)