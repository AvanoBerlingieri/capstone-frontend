package capstone.safeline.apis.dto

data class LoginResponse(
    val token: String,
    val email: String,
    val username: String,
    val status: String
)
