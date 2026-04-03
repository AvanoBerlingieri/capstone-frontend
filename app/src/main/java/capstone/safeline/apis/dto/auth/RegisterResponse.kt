package capstone.safeline.apis.dto.auth

data class RegisterResponse(
    val id: String, // UUID received as string
    val username: String,
    val email: String,
    val createdAt: String // Date received as string
)
