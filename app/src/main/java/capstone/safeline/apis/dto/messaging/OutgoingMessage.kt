package capstone.safeline.apis.dto.messaging

import java.util.UUID

data class OutgoingMessage(
    val messageId: UUID,
    val sender: UUID,
    val content: String,
    val timestamp: String
)