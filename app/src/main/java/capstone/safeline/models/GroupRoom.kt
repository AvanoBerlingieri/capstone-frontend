package capstone.safeline.models

data class GroupRoom(
    val id: Long? = null,
    val roomId: String,
    val creatorId: String,
    val status: String,
    val createdAt: String?
)