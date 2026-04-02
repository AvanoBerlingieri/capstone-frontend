package capstone.safeline.apis.dto.messaging

data class IncomingMessage(
    val receiver: String, // UUID
    val content: String
)
