package capstone.safeline.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import capstone.safeline.data.local.entity.FriendEntity
import capstone.safeline.data.local.entity.GroupChatEntity
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrivateMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(message: GroupMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(group: GroupChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFriend(friend: FriendEntity)

    @Query("SELECT * FROM friends WHERE userId = :id LIMIT 1")
    suspend fun findFriend(id: String): FriendEntity?

    @Query("SELECT * FROM group_chat_messages WHERE messageId = :id LIMIT 1")
    suspend fun findGroupMessage(id: String): GroupMessageEntity?

    @Query("SELECT * FROM group_chats")
    fun getAllGroupChats(): Flow<List<GroupChatEntity>>

    @Query("SELECT * FROM friends")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM messages WHERE receiverId = :userId OR senderId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM group_chat_messages WHERE groupId = :groupId ORDER BY datetime(timestamp) ASC")
    fun getMessagesForGroup(groupId: String): Flow<List<GroupMessageEntity>>

    @Query("UPDATE group_chat_messages SET status = :status WHERE messageId = :id")
    suspend fun updateMessageStatus(id: String, status: String)

    @Query("UPDATE group_chat_messages SET status = :newStatus WHERE messageId = :uuid")
    suspend fun updateGroupMessageStatus(uuid: String, newStatus: String)
}