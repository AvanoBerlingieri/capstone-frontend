package capstone.safeline.apis.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)