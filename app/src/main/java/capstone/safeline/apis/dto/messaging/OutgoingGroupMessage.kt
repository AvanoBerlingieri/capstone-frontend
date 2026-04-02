package capstone.safeline.apis.dto.messaging

import java.util.UUID

data class OutgoingGroupMessage(
    val messageId: UUID,
    val groupId: UUID,
    val senderId: UUID,
    val content: String,
    val timestamp: String
)