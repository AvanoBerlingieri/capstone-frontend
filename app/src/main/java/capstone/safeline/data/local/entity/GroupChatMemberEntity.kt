package capstone.safeline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_chat_member")
data class GroupChatMemberEntity(
    @PrimaryKey val groupId: String, // UUID
    val userId: String, // UUID
    val username: String
)