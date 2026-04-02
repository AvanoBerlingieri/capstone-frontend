package capstone.safeline.apis.dto.messaging

// Maps to the server's MessageAck (Used for both sending and receiving)
data class MessageAck(
    val messageId: String
)
