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

    // Filter by BOTH users to get the specific 1-on-1 conversation
    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :currentUserId AND receiverId = :contactId) 
           OR (senderId = :contactId AND receiverId = :currentUserId) 
        ORDER BY timestamp ASC
    """)
    fun getConversation(currentUserId: String, contactId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :newStatus WHERE messageUuid = :uuid")
    suspend fun updateMessageStatus(uuid: String, newStatus: String)

    @Query("SELECT * FROM messages WHERE status = 'PENDING'")
    suspend fun getUnsentMessages(): List<MessageEntity>
}