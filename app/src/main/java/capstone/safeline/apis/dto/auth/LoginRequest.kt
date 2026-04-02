package capstone.safeline.apis.dto.auth

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

