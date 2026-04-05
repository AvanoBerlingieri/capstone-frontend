package capstone.safeline.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "group_chat_member",
    primaryKeys = ["groupId", "userId"]
)
data class GroupChatMemberEntity(
    val groupId: String,
    val userId: String,
    val username: String
)