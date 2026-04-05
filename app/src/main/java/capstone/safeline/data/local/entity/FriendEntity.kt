package capstone.safeline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val friendId: String, // UUID
    val userId: String, //UUID
    val username: String
)