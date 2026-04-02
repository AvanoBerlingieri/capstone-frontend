package capstone.safeline.apis.dto.friends

data class FriendRequest(
    val userId: String, //UUID
    val friendId: String, //UUID
    val status: String
)
