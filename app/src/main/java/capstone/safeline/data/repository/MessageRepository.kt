package capstone.safeline.data.repository

import capstone.safeline.apis.dto.UpdateMessageStatusDto
import capstone.safeline.apis.network.ApiServiceMessage
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class MessageRepository(
    private val messageDao: MessageDao,
    private val apiService: ApiServiceMessage
) {

    // A Flow of messages for a specific conversation to keep the UI reactive
    fun getConversation(userId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForUser(userId)
    }

    // This is the function the "Second Phone" will call when a message arrives via WebSocket
    suspend fun receiveAndSaveMessage(message: MessageEntity) {
        val receivedMessage = message.copy(status = "RECEIVED")
        messageDao.insertMessage(receivedMessage)
    }

    // Useful for the sender's side or for updating status locally
    suspend fun updateStatus(messageUuid: String, newStatus: String) {
        messageDao.updateMessageStatus(messageUuid, newStatus)
    }

    // This talks to the backend
    suspend fun markMessageAsRead(messageUuid: String) {
        try {
            // 1. Tell the server the message was read
            val response = apiService.updateMessageStatus(
                messageUuid,
                UpdateMessageStatusDto(status = "READ")
            )

            // 2. If the server says "OK" (200 series status code), update our local database
            if (response.isSuccessful) {
                messageDao.updateMessageStatus(messageUuid, "READ")
            }
        } catch (e: Exception) {
            // If the phone has no internet or the server is down, we catch the crash here
            println("Failed to update read status on server: ${e.message}")
        }
    }
}