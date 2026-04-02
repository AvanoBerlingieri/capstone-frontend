package capstone.safeline.apis.dto

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

