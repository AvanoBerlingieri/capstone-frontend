package capstone.safeline.apis.dto.messaging

data class IncomingMessage(
    val messageId: String,  // UUID
    val receiver: String,   // UUID
    val content: String     // Text body (String)
)
