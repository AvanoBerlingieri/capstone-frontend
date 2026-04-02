package capstone.safeline.apis.dto.messaging

data class IncomingGroupMessage(
    val groupId: String, // UUID
    val content: String
)
