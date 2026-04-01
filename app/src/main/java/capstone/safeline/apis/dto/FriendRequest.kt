package capstone.safeline.apis.dto

data class FriendRequest(
    val userId: String, //UUID
    val friendId: String, //UUID
    val status: String
)
