package capstone.safeline.data.repository

import capstone.safeline.apis.ApiServiceMessage
import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import capstone.safeline.apis.network.WebSocketMessageListener
import capstone.safeline.data.local.dao.GroupMessageDao
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MessageRepository(
    private val messageDao: MessageDao,
    private val groupMessageDao: GroupMessageDao,
    private val apiService: ApiServiceMessage,
    private val currentUserId: String
) : WebSocketMessageListener {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Expose the Flow to the ViewModel
    fun getConversation(contactId: String): Flow<List<MessageEntity>> {
        return messageDao.getConversation(currentUserId, contactId)
    }

    // Helper for manual inserts (Optimistic UI)
    suspend fun saveMessageLocally(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    // Handle incoming private messages from Socket
    override fun onPrivateMessageReceived(message: OutgoingMessage) {
        repositoryScope.launch {
            val entity = MessageEntity(
                messageUuid = message.messageId.toString(),
                senderId = message.sender.toString(),
                receiverId = currentUserId,
                content = message.content,
                timestamp = System.currentTimeMillis(),
                status = "DELIVERED"
            )
            messageDao.insertMessage(entity)
        }
    }

    // Handle delivery acknowledgments
    override fun onMessageDeliveredAck(messageId: String) {
        repositoryScope.launch {
            messageDao.updateMessageStatus(messageId, "DELIVERED")
        }
    }

    override fun onGroupMessageReceived(message: OutgoingGroupMessage) {
        // Implement group logic here similar to private messages
    }
}