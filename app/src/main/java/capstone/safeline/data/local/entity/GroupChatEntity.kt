package capstone.safeline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_chats")
data class GroupChatEntity(
    @PrimaryKey val groupId: String, // UUID
)