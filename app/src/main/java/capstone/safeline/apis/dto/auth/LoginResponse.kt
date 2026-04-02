package capstone.safeline.apis.dto.auth

data class LoginResponse(
    val token: String,
    val email: String,
    val username: String,
    val status: String
)
