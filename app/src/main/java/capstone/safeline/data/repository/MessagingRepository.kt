package capstone.safeline.data.repository

import android.content.Context
import android.util.Log
import capstone.safeline.apis.ApiServiceMessage
import capstone.safeline.apis.dto.messaging.CreateGroupRequest
import capstone.safeline.apis.dto.messaging.GroupInfoDto
import capstone.safeline.apis.dto.messaging.GroupMemberDto
import capstone.safeline.apis.dto.messaging.RenameGroupRequest
import capstone.safeline.apis.dto.messaging.resolveDisplayUsername
import capstone.safeline.apis.dto.messaging.resolveMemberId
import capstone.safeline.apis.network.ApiClientMessaging
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.FriendEntity
import capstone.safeline.data.local.entity.GroupChatEntity
import capstone.safeline.data.local.entity.GroupChatMemberEntity
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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

    suspend fun fetchPrivateHistory(otherUserId: String): Result<Unit> {
        return try {
            val response = apiService.getPrivateHistory(otherUserId) // Assumes this endpoint exists
            if (response.isSuccessful) {
                val history = response.body() ?: emptyList()
                history.forEach { msg ->
                    val myId = dataStoreManager.userIdFlow.first() ?: ""
                    messageDao.insertPrivateMessage(
                        MessageEntity(
                            messageId = msg.messageId,
                            senderId = msg.senderId,
                            receiverId = msg.receiverId,
                            content = msg.content,
                            timestamp = msg.timestamp,
                            status = "DELIVERED",
                            isMine = msg.senderId == myId
                        )
                    )
                }
                Result.success(Unit)
            } else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
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
            Log.w("MessagingRepo", "addUserToGroup failed", e)
            false
        }
    }

    /**
     * Fetches members from the API (supports several JSON shapes), replaces cached rows for this
     * group on success, then merges anyone seen in local messages or the current user so the list
     * is complete even if the server omits silent members.
     */
    suspend fun getGroupMembersWithCache(groupId: String): List<GroupChatMemberEntity> {
        if (groupId.isBlank()) return emptyList()
        var apiSucceeded = false
        var parsed = emptyList<GroupMemberDto>()
        try {
            val jsonMedia = "application/json; charset=utf-8".toMediaType()
            val emptyListBody = "{}".toRequestBody(jsonMedia)
            // Server returns 405 for GET …/members; only POST is allowed (see Allow: POST).
            var response = apiService.listGroupMembersWithBody(groupId, emptyListBody)
            if (!response.isSuccessful) {
                Log.w(
                    "MessagingRepo",
                    "listGroupMembersWithBody failed code=${response.code()}, retrying without body"
                )
                response = apiService.listGroupMembersNoBody(groupId)
            }
            if (response.isSuccessful) {
                apiSucceeded = true
                val raw = response.body()?.use { it.string() } ?: "[]"
                parsed = try {
                    parseGroupMembersJson(raw)
                } catch (e: Exception) {
                    Log.e("MessagingRepo", "parseGroupMembersJson failed raw=$raw", e)
                    emptyList()
                }
                Log.d("MessagingRepo", "Group $groupId members parsed count=${parsed.size}")
            } else {
                Log.e(
                    "MessagingRepo",
                    "listGroupMembers failed code=${response.code()} body=${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            Log.w("MessagingRepo", "listGroupMembers request failed", e)
        }

        if (apiSucceeded) {
            messageDao.deleteGroupMembersForGroup(groupId)
            for (dto in parsed) {
                val uid = dto.resolveMemberId()
                if (uid.isBlank()) continue
                val label = dto.resolveDisplayUsername().ifBlank { uid.take(8).ifEmpty { "?" } }
                messageDao.insertGroupMember(
                    GroupChatMemberEntity(
                        groupId = groupId,
                        userId = uid,
                        username = label
                    )
                )
            }
        }

        val existing = messageDao.getGroupMembersForGroup(groupId).associateBy { it.userId }.toMutableMap()
        val senders = messageDao.getDistinctGroupSenderIds(groupId)
        val me = dataStoreManager.userIdFlow.first()
        for (sid in (senders + listOfNotNull(me)).distinct()) {
            if (sid.isBlank()) continue
            if (existing[sid] == null) {
                val row = GroupChatMemberEntity(
                    groupId = groupId,
                    userId = sid,
                    username = sid.take(8).ifEmpty { "?" }
                )
                messageDao.insertGroupMember(row)
                existing[sid] = row
            }
        }

        return existing.values.sortedBy { it.username.lowercase() }
    }

    suspend fun getLocalGroupParticipantIds(groupId: String): List<String> =
        messageDao.getDistinctGroupSenderIds(groupId)

    suspend fun renameGroup(groupId: String, newName: String): Boolean {
        val trimmed = newName.trim()
        if (groupId.isBlank() || trimmed.isEmpty()) return false
        return try {
            val response = apiService.renameGroupChat(groupId, RenameGroupRequest(trimmed))
            if (!response.isSuccessful) {
                Log.e(
                    "MessagingRepo",
                    "renameGroup failed code=${response.code()} body=${response.errorBody()?.string()}"
                )
                return false
            }
            messageDao.updateGroupChatName(groupId, trimmed)
            true
        } catch (e: Exception) {
            Log.e("MessagingRepo", "renameGroup exception", e)
            false
        }
    }

    suspend fun leaveGroup(groupId: String): Boolean {
        return try {
            if (groupId.isBlank()) {
                Log.e("MessagingRepo", "leaveGroup: empty groupId")
                return false
            }
            val token = dataStoreManager.tokenFlow.first()
            if (token.isNullOrBlank()) {
                Log.e("MessagingRepo", "leaveGroup: not signed in")
                return false
            }
            val response = apiService.leaveGroupApi(groupId)
            if (!response.isSuccessful) {
                Log.e(
                    "MessagingRepo",
                    "leaveGroup failed code=${response.code()} body=${response.errorBody()?.string()}"
                )
                return false
            }
            messageDao.purgeGroupLocally(groupId)
            Log.d("MessagingRepo", "leaveGroup: purged local group $groupId")
            true
        } catch (e: Exception) {
            Log.e("MessagingRepo", "leaveGroup exception", e)
            false
        }
    }

    suspend fun deleteGroup(groupId: String): Boolean {
        return try {
            if (groupId.isBlank()) return false
            val response = apiService.deleteGroup(groupId)
            if (!response.isSuccessful) return false
            messageDao.purgeGroupLocally(groupId)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseGroupMembersJson(json: String): List<GroupMemberDto> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "null") return emptyList()
        val gson = Gson()
        val memberListType = object : TypeToken<List<GroupMemberDto>>() {}.type
        val root = JsonParser.parseString(trimmed)
        val array: JsonArray = when {
            root.isJsonArray -> root.asJsonArray
            root.isJsonObject -> {
                val o = root.asJsonObject
                extractMemberJsonArray(o) ?: return parseSingleMemberObject(o, gson)
            }
            else -> return emptyList()
        }
        if (array.size() == 0) return emptyList()
        val first = array[0]
        if (first.isJsonPrimitive && first.asJsonPrimitive.isString) {
            return array.mapNotNull { el ->
                if (el.isJsonPrimitive && el.asJsonPrimitive.isString) {
                    GroupMemberDto(userId = el.asString, username = null, email = null, user = null)
                } else {
                    null
                }
            }
        }
        return gson.fromJson<List<GroupMemberDto>>(array, memberListType) ?: emptyList()
    }

    private fun extractMemberJsonArray(o: JsonObject): JsonArray? {
        for (key in listOf("members", "content", "data", "items")) {
            o.get(key)?.takeIf { it.isJsonArray }?.let { return it.asJsonArray }
        }
        val emb = o.getAsJsonObject("_embedded") ?: return null
        for (key in listOf("members", "memberList", "users", "userList")) {
            emb.get(key)?.takeIf { it.isJsonArray }?.let { return it.asJsonArray }
        }
        return null
    }

    private fun parseSingleMemberObject(o: JsonObject, gson: Gson): List<GroupMemberDto> {
        if (o.has("userId") || o.has("user") || o.has("id") || o.has("memberId") || o.has("user_id")) {
            return listOf(gson.fromJson(o, GroupMemberDto::class.java))
        }
        return emptyList()
    }
}