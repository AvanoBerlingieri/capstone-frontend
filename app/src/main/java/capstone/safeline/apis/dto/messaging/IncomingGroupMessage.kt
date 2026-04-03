package capstone.safeline.apis.dto.messaging

data class IncomingGroupMessage(
    val messageId: String,  // UUID
    val groupId: String,    // UUID
    val content: String     // Text body (String)
)
