package capstone.safeline.apis.dto.messaging

import java.sql.Timestamp

data class OutgoingGroupMessage(
    val messageId: String, // UUID
    val senderId: String, // UUID
    val groupId: String, // UUID
    val content: String,// Text body (String)
    val timestamp: String, // Timestamp
)
