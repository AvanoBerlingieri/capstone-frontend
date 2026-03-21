package capstone.safeline.apis.dto

data class UpdatePasswordDto(
    val currentPassword: String,
    val newPassword: String
)
