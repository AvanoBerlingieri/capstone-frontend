package capstone.safeline.apis.dto.friends

data class FriendResponse(
    val userId: String, //UUID
    val friendId: String, //UUID
    val status: String
)
