package capstone.safeline.apis.dto.messaging

import java.sql.Timestamp
import java.util.UUID

data class OutgoingGroupMessage(
    val messageId: String, // UUID
    val groupId: String, // UUID
    val senderId: String, // UUID
    val content: String,
    val timestamp: Timestamp, // DATE
)
