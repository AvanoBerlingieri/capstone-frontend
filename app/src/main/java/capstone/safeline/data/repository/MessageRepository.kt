package capstone.safeline.data.repository

import android.util.Log
import capstone.safeline.apis.dto.UpdateMessageStatusDto
import capstone.safeline.apis.network.ApiServiceMessage
import capstone.safeline.apis.network.WebSocketManager // Make sure this import is correct
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class MessageRepository(
    private val messageDao: MessageDao,
    private val apiService: ApiServiceMessage
) {
    // 1. Placeholder for the WebSocket (to avoid circular dependency at startup)
    private var webSocketManager: WebSocketManager? = null

    fun setWebSocketManager(manager: WebSocketManager) {
        this.webSocketManager = manager
    }

    fun getConversation(userId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForUser(userId)

    fun getAllRecentMessages(): Flow<List<MessageEntity>> =
        messageDao.getAllRecentMessages()

    suspend fun receiveAndSaveMessage(message: MessageEntity) {
        val receivedMessage = message.copy(status = "RECEIVED")
        messageDao.insertMessage(receivedMessage)
    }

    suspend fun sendMessage(message: MessageEntity) {
        // 1. Save locally so the user sees their bubble immediately
        messageDao.insertMessage(message)

        // 2. Convert the MessageEntity to JSON for the backend
        val messageJson = JSONObject().apply {
            put("senderId", message.senderId)
            put("receiverId", message.receiverId)
            put("content", message.content)
            put("timestamp", message.timestamp)
        }.toString()

        // 3. Blast it over the WebSocket
        if (webSocketManager != null) {
            webSocketManager?.sendRawMessage(messageJson)
        } else {
            Log.e("MessageRepository", "WebSocketManager not initialized!")
        }
    }

    suspend fun updateStatus(messageUuid: String, newStatus: String) {
        messageDao.updateMessageStatus(messageUuid, newStatus)
    }

    suspend fun markMessageAsRead(messageUuid: String) {
        try {
            val response = apiService.updateMessageStatus(messageUuid, UpdateMessageStatusDto("READ"))
            if (response.isSuccessful) {
                messageDao.updateMessageStatus(messageUuid, "READ")
            }
        } catch (e: Exception) {
            Log.e("Repo", "Read status sync failed: ${e.message}")
        }
    }
}