package capstone.safeline.apis.dto.messaging

import java.util.UUID

data class IncomingMessage(
    val receiver: UUID,
    val content: String
)