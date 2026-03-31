package capstone.safeline.apis.dto

data class FriendResponse(
    val userId: String, //UUID
    val friendId: String, //UUID
    val status: String
)
