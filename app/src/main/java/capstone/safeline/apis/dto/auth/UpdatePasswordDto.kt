package capstone.safeline.apis.dto.auth

data class UpdatePasswordDto(
    val currentPassword: String,
    val newPassword: String
)
