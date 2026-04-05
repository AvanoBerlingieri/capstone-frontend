package capstone.safeline.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import capstone.safeline.data.local.entity.FriendEntity
import capstone.safeline.data.local.entity.GroupChatEntity
import capstone.safeline.data.local.entity.GroupChatMemberEntity
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

    @Query("SELECT DISTINCT senderId FROM group_chat_messages WHERE groupId = :groupId")
    suspend fun getDistinctGroupSenderIds(groupId: String): List<String>

    @Query("SELECT * FROM group_chat_member WHERE groupId = :groupId ORDER BY username COLLATE NOCASE ASC")
    suspend fun getGroupMembersForGroup(groupId: String): List<GroupChatMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupChatMemberEntity)

    @Query("UPDATE group_chats SET name = :name WHERE groupId = :groupId")
    suspend fun updateGroupChatName(groupId: String, name: String)

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

    @Query("DELETE FROM group_chat_messages WHERE groupId = :groupId")
    suspend fun deleteGroupMessagesForGroup(groupId: String)

    @Query("DELETE FROM group_chat_member WHERE groupId = :groupId")
    suspend fun deleteGroupMembersForGroup(groupId: String)

    @Query("DELETE FROM group_chats WHERE groupId = :groupId")
    suspend fun deleteGroupChatRow(groupId: String)

    @Transaction
    suspend fun purgeGroupLocally(groupId: String) {
        deleteGroupMessagesForGroup(groupId)
        deleteGroupMembersForGroup(groupId)
        deleteGroupChatRow(groupId)
    }
}