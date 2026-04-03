package capstone.safeline.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE receiverId = :userId OR senderId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :newStatus WHERE messageId = :uuid")
    suspend fun updateMessageStatus(uuid: String, newStatus: String)

    @Query("SELECT * FROM messages WHERE status = 'PENDING'")
    suspend fun getUnsentMessages(): List<MessageEntity>

    // grouup chats
//    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
//    fun getMessagesForGroup(groupId: String): Flow<List<MessageEntity>>
}