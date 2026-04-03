package capstone.safeline.apis.dto.auth

data class GetUserByIdResponse(
    val id: String,
    val username: String,
    val email: String
)