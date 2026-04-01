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

    // Using Flow so the UI updates automatically when new messages arrive
    @Query("SELECT * FROM messages WHERE receiverId = :userId OR senderId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :newStatus WHERE messageUuid = :uuid")
    suspend fun updateMessageStatus(uuid: String, newStatus: String)

    @Query("SELECT * FROM messages WHERE status = 'PENDING'")
    suspend fun getUnsentMessages(): List<MessageEntity>

    @Query("""
    SELECT * FROM messages 
    WHERE id IN (
        SELECT MAX(id) FROM messages 
        GROUP BY CASE WHEN senderId < receiverId THEN senderId || receiverId ELSE receiverId || senderId END
    ) 
    ORDER BY timestamp DESC
""")
    fun getAllRecentMessages(): Flow<List<MessageEntity>>
}