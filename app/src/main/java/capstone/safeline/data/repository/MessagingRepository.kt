package capstone.safeline.data.repository

import android.content.Context
import android.util.Log
import capstone.safeline.apis.ApiServiceMessage
import capstone.safeline.apis.dto.messaging.CreateGroupRequest
import capstone.safeline.apis.dto.messaging.GroupInfoDto
import capstone.safeline.apis.network.ApiClientMessaging
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.FriendEntity
import capstone.safeline.data.local.entity.GroupChatEntity
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

    // Local Database Flows
    fun getPrivateChatMessageFlow(otherUserId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForUser(otherUserId)
    }

    fun getGroupChatMessagesFlow(groupId: String): Flow<List<GroupMessageEntity>> {
        Log.d("REPO_DEBUG", "Fetching local messages for Group ID: '$groupId'")
        return messageDao.getMessagesForGroup(groupId)
    }

    sealed class ChatSummary {
        data class Private(val friend: FriendEntity) : ChatSummary()
        data class Group(val group: GroupChatEntity) : ChatSummary()

        val id: String
            get() = when (this) {
                is Private -> friend.userId
                is Group -> group.groupId
            }

        val displayName: String
            get() = when (this) {
                is Private -> friend.username
                is Group -> group.name
            }
    }

    // Combine the two flows
    fun getAllChatsFlow(): Flow<List<ChatSummary>> {
        return combine(
            messageDao.getAllFriends(),
            messageDao.getAllGroupChats()
        ) { friends, groups ->
            val list = mutableListOf<ChatSummary>()
            list.addAll(friends.map { ChatSummary.Private(it) })
            list.addAll(groups.map { ChatSummary.Group(it) })
            list
        }
    }

    // REST API
    // Syncs group history from server and saves to Room
    suspend fun fetchGroupHistory(groupId: String): Result<Unit> {
        return try {
            val response = apiService.getGroupHistory(groupId)

            if (response.isSuccessful) {

                val history = response.body() ?: emptyList()
                Log.d("HISTORY", "Fetched ${history.size} messages for group $groupId")

                history.forEach { msg ->
                    val actualSenderId = msg.groupId // These are flipped and i can't fix
                    val actualGroupId = msg.senderId

                    val myId = dataStoreManager.userIdFlow.first()?.trim() ?: ""
                    val isActuallyMine = actualSenderId.equals(myId, ignoreCase = true)
                    Log.i("HISTORY", "User $myId is fetching history for group $groupId")
                    Log.i("HISTORY", "message: $msg")
                    Log.i("HISTORY", "message is mine: $isActuallyMine")


                    // Check if the message already exists in local DB
                    val existingMessage = messageDao.findGroupMessage(msg.messageId)
                    if (existingMessage == null) {
                        Log.i("HISTORY","Inserting message into local db")
                        messageDao.insertGroupMessage(
                            GroupMessageEntity(
                                messageId = msg.messageId,
                                senderId = actualSenderId,
                                groupId = groupId,
                                content = msg.content,
                                timestamp = msg.timestamp,
                                status = "DELIVERED",
                                isMine = isActuallyMine
                            )
                        )
                    } else {
                        Log.i("HISTORY", "Message already exists in local db")
                        if (existingMessage.status != "DELIVERED") {
                            messageDao.updateMessageStatus(msg.messageId, "DELIVERED")
                        }
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("HISTORY", "Failed to sync", e)
            Result.failure(e)
        }
    }

    /**
     * Get list of groups the user belongs to
     */
    suspend fun getMyGroups(): Result<List<GroupInfoDto>> {
        return try {
            val myId = dataStoreManager.userIdFlow.first()
                ?: return Result.failure(Exception("No User ID"))
            val response = apiService.getAllUserGroups(myId)
            if (response.isSuccessful) {
                Log.i("MessagingRepo", "List of groups retrieved\n$response")
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load groups"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a new group chat
     */
    suspend fun createGroup(dto: CreateGroupRequest): Result<CreateGroupRequest> {
        return try {
            val group = CreateGroupRequest(dto.groupId, dto.name)
            val response = apiService.createGroup(group)
            if (response.isSuccessful && response.body() != null) {
                //Build group chat
                val entity = GroupChatEntity(
                    groupId = dto.groupId,
                    name = dto.name
                )
                // insert into rooms db
                messageDao.insertGroup(entity)
                Log.i("MessageRepo", "Group $group created")
                Result.success(response.body()!!)
            } else {
                Log.e("MessageRepo", "Error Code: ${response.code()}")
                Result.failure(Exception("Server returned ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    suspend fun deleteGroup(groupId: String): Boolean {
        return try {
            apiService.deleteGroup(groupId).isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}