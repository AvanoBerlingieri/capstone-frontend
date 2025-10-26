package capstone.safeline.api.dto

data class LoginResponse(
    val headers: Map<String, Any>,
    val body: String,
    val statusCodeValue: Int,
    val statusCode: String
)
