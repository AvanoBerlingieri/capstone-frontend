package capstone.safeline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_messages")
data class GroupMessageEntity(
    @PrimaryKey val messageId: String, // UUID
    val senderId: String,   // UUID
    val groupId: String,    // UUID
    val content: String,    // Text Body (String)
    val timestamp: String,  // Date
    val status: String,     // ENUM: SENT, DELIVERED
    val isMine: Boolean     // To see ownership quick and for easier display usage
)