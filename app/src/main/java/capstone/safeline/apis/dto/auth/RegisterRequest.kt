package capstone.safeline.apis.dto.auth

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)