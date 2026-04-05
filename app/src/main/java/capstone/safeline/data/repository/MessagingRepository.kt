package capstone.safeline.data.repository

import android.content.Context
import android.util.Log
import capstone.safeline.apis.ApiServiceMessage
import capstone.safeline.apis.dto.messaging.CreateGroupRequest
import capstone.safeline.apis.dto.messaging.GroupInfoDto
import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import capstone.safeline.apis.network.ApiClientMessaging
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MessageRepository(
    private val apiService: ApiServiceMessage,
    private val messageDao: MessageDao,
    private val dataStoreManager: DataStoreManager
) {
    companion object {
        @Volatile
        private var INSTANCE: MessageRepository? = null

        fun getInstance(context: Context, dao: MessageDao): MessageRepository {
            return INSTANCE ?: synchronized(this) {
                val ds = DataStoreManager.getInstance(context)
                val api = ApiClientMessaging.provideMessageApiService(context, ds)
                INSTANCE ?: MessageRepository(api, dao, ds).also { INSTANCE = it }
            }
        }
    }

    // Add these to MessageRepository class
    /**
     * Saves an incoming private message received via WebSocket
     */
    suspend fun saveIncomingPrivateMessage(msg: OutgoingMessage) {
        val myId = dataStoreManager.userIdFlow.first() ?: ""
        val entity = MessageEntity(
            messageId = msg.messageId,
            senderId = msg.senderId,
            receiverId = msg.receiverId,
            content = msg.content,
            timestamp = msg.timestamp,
            status = "DELIVERED",
            isMine = msg.senderId == myId
        )
        messageDao.insertPrivateMessage(entity)
    }

    /**
     * Saves an incoming group message received via WebSocket
     */
    suspend fun saveIncomingGroupMessage(msg: OutgoingGroupMessage) {
        val myId = dataStoreManager.userIdFlow.first() ?: ""
        val entity = GroupMessageEntity(
            messageId = msg.messageId,
            senderId = msg.senderId,
            groupId = msg.groupId,
            content = msg.content,
            timestamp = msg.timestamp,
            status = "DELIVERED",
            isMine = msg.senderId == myId
        )
        messageDao.insertGroupMessage(entity)
    }

    /**
     * Updates message status in local DB (e.g., when Ack is successful)
     */
    suspend fun updateLocalMessageStatus(messageId: String, isGroup: Boolean, status: String) {
        if (isGroup) {
            messageDao.updateGroupMessageStatus(messageId, status)
        } else {
            messageDao.updateMessageStatus(messageId, status)
        }
    }

    // Local Database Flows

    fun getPrivateChatFlow(otherUserId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForUser(otherUserId)
    }

    fun getGroupChatFlow(groupId: String): Flow<List<GroupMessageEntity>> {
        return messageDao.getMessagesForGroup(groupId)
    }

    // REST API

    /**
     * Syncs group history from server and saves to Room
     */
    suspend fun fetchGroupHistory(groupId: String): Result<Unit> {
        return try {
            val response = apiService.getGroupHistory(groupId)
            if (response.isSuccessful) {
                val history = response.body() ?: emptyList()
                history.forEach { msg ->
                    val myId = dataStoreManager.userIdFlow.first() ?: ""
                    messageDao.insertGroupMessage(
                        GroupMessageEntity(
                            messageId = msg.messageId,
                            senderId = msg.senderId,
                            groupId = msg.groupId,
                            content = msg.content,
                            timestamp = msg.timestamp,
                            status = "DELIVERED",
                            isMine = msg.senderId == myId
                        )
                    )
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch group history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get list of groups the user belongs to
     */
    suspend fun getMyGroups(): Result<List<GroupInfoDto>> {
        return try {
            val myId = dataStoreManager.userIdFlow.first() ?: return Result.failure(Exception("No User ID"))
            val response = apiService.getAllUserGroups(myId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load groups"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new group on the server
     */
    suspend fun createGroup(name: String): Boolean {
        return try {
            val response = apiService.createGroup(CreateGroupRequest(name))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MessageRepo", "Create group failed", e)
            false
        }
    }

    /**
     * Adds a user to an existing group
     */
    suspend fun addUserToGroup(groupId: String, userIdToAdd: String): Boolean {
        return try {
            val response = apiService.addUserToGroup(groupId, userIdToAdd)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun leaveGroup(groupId: String): Boolean {
        return try {
            apiService.leaveGroup(groupId).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteGroup(groupId: String): Boolean{
        return try{
            apiService.deleteGroup(groupId).isSuccessful
        } catch (e: Exception){
            false
        }
    }
}