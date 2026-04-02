package capstone.safeline.apis.dto.messaging

import java.util.UUID

data class IncomingGroupMessage(
    val groupId: UUID,
    val content: String
)