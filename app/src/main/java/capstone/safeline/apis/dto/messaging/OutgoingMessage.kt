package capstone.safeline.apis.dto.messaging

import java.sql.Timestamp

data class OutgoingMessage(
    val messageId: String,  // UUID
    val senderId: String,   // UUID
    val receiverId: String, // UUID
    val content: String,    // Text body (String)
    val timestamp: String,  // Timestamp
)
