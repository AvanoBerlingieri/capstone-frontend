package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import capstone.safeline.apis.dto.messaging.IncomingGroupMessage
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.local.entity.MessageEntity
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.FriendRepository
import capstone.safeline.data.repository.MessageRepository

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.time.LocalDateTime

class WebSocketManager {
    companion object {
        @Volatile
        private var INSTANCE: WebSocketManager? = null

        fun getInstance(): WebSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketManager().also { INSTANCE = it }
            }
        }
    }

    private var authRepository: AuthRepository? = null
    private var messageDao: MessageDao? = null

    fun init(
        repo: AuthRepository,
        dao: MessageDao,
        friendRepo: FriendRepository,
        msgRepo: MessageRepository
    ) {
        this.authRepository = repo
        this.messageDao = dao
    }

    private val gatewayWsUrl = "ws://10.0.2.2:8091/ws"
    private var stompClient: StompClient? = null
    private var isConnecting = false
    @SuppressLint("CheckResult")
    fun connect(token: String) {
        val repo = authRepository
        val dao = messageDao

        if (repo == null || dao == null) {
            Log.e("WS", "Cannot connect: Manager not fully initialized (Repo: ${repo != null}, Dao: ${dao != null})")
            return
        }

        // check if connection is already alive
        if (stompClient?.isConnected == true || isConnecting) {
            Log.d("WS", "Already connected or connecting, skipping...")
            return
        }

        isConnecting = true

        // create new client if null
        if (stompClient == null) {
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, gatewayWsUrl)
        }

        val headers = listOf(StompHeader("Authorization", "Bearer $token"))

        stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    isConnecting = false
                    Log.d("WS", "Connected to Gateway!")

                    CoroutineScope(Dispatchers.IO).launch {
                        if (authRepository == null) {
                            Log.e("WS", "AuthRepository is NULL")
                        }
                        val success = repo.updateStatus("ONLINE")
                        Log.d("WS", "Status update successful: $success")
                    }

                    subscribeToMessages()
//                    subscribeToGroups()
                    stompClient?.send("/app/message.sync")?.subscribe({
                        Log.d("WS", "Sync request sent")
                    }, { Log.e("WS", "Sync failed", it) })
                }

                LifecycleEvent.Type.ERROR -> {
                    isConnecting = false
                    Log.e("WS", "Connection Error", lifecycleEvent.exception)
                }

                LifecycleEvent.Type.CLOSED -> {
                    isConnecting = false
                    Log.d("WS", "Connection Closed")
                }

                else -> {}
            }
        }

        stompClient?.connect(headers)
    }

    @SuppressLint("CheckResult")
    private fun subscribeToMessages() {
        val gson = Gson()
        // Listen for private messages
        stompClient?.topic("/user/queue/messages")?.subscribe { topicMessage ->
            val payload = topicMessage.payload
            Log.d("WS", "Received Private Message: $payload")

            try {
                // Parse the incoming message
                val msg = gson.fromJson(payload, OutgoingMessage::class.java)

                CoroutineScope(Dispatchers.IO).launch {
                    // create message entity for room db
                    val entity = MessageEntity(
                        messageId = msg.messageId,
                        senderId = msg.senderId,
                        receiverId = msg.receiverId,
                        content = msg.content,
                        timestamp = msg.timestamp,
                        status = "DELIVERED",
                        isMine = false
                    )
                    // insert message
                    messageDao?.insertPrivateMessage(entity)

                    // ack the message
                    acknowledgeMessage(msg.messageId)
                }
            } catch (e: Exception) {
                Log.e("WS", "Error processing message", e)
            }
        }
    }

    @SuppressLint("CheckResult")
    fun subscribeToGroups(groupIds: List<String>) {
        val gson = Gson()
        groupIds.forEach { id ->
            // Subscribing to each group's topic
            stompClient?.topic("/topic/room.$id")?.subscribe { topicMessage ->
                try {
                    val msg = gson.fromJson(topicMessage.payload, OutgoingGroupMessage::class.java)
                    Log.i("WS", "GROUP MESSAGE $msg")

                    CoroutineScope(Dispatchers.IO).launch {
                        val myId = authRepository?.userIdFlow?.first() ?: ""

                        // If user didn't send it, save it.
                        if (msg.senderId != myId) {
                            val entity = GroupMessageEntity(
                                messageId = msg.messageId,
                                senderId = msg.senderId,
                                groupId = msg.groupId,
                                content = msg.content,
                                timestamp = msg.timestamp,
                                status = "DELIVERED",
                                isMine = false
                            )
                            messageDao?.insertGroupMessage(entity)
                        } else{
                            acknowledgeGroupMessage(msg.messageId)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("WS", "Group parse error", e)
                }
            }
        }
    }

    /**
     * Sends a 1-to-1 message.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    fun sendPrivateMessage(message: IncomingMessage) {
        val gson = Gson()
        val jsonMessage = gson.toJson(message)

        // Send to Server
        stompClient?.send("/app/message.send", jsonMessage)?.subscribe({
            Log.d("WS", "Private message sent to server")

            // Save to messages table
            CoroutineScope(Dispatchers.IO).launch {
                val myId = authRepository?.userIdFlow?.first() ?: ""

                val entity = MessageEntity(
                    messageId = message.messageId,
                    senderId = myId,
                    receiverId = message.receiver,
                    content = message.content,
                    timestamp = LocalDateTime.now().toString(),
                    status = "SENT",
                    isMine = true
                )
                messageDao?.insertPrivateMessage(entity)

            }
        }, { error ->
            Log.e("WS", "Failed to send private message", error)
        })
    }

    /**
     * Sends a group message.
     * /group.send
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    fun sendGroupMessage(message: IncomingGroupMessage) {
        val gson = Gson()
        val jsonMessage = gson.toJson(message)

        stompClient?.send("/app/group.send", jsonMessage)?.subscribe({
            Log.d("WS", "Group message sent to server")

            CoroutineScope(Dispatchers.IO).launch {
                val myId = authRepository?.userIdFlow?.first() ?: ""

                // Save to group_chat_messages table
                val entity = GroupMessageEntity(
                    messageId = message.messageId,
                    senderId = myId,
                    groupId = message.groupId,
                    content = message.content,
                    timestamp = LocalDateTime.now().toString(),
                    status = "SENT",
                    isMine = true
                )
                messageDao?.insertGroupMessage(entity)
            }
        }, { error ->
            Log.e("WS", "Failed to send group message", error)
        })

    }

    /**
     * Sends an acknowledgment for a received message.
     * /message.ack
     */
    @SuppressLint("CheckResult")
    fun acknowledgeMessage(messageId: String) {
        val ackData = mapOf("messageId" to messageId)
        val jsonAck = Gson().toJson(ackData)

        stompClient?.send("/app/message.ack", jsonAck)?.subscribe({

            CoroutineScope(Dispatchers.IO).launch {
                messageDao?.updateMessageStatus(messageId, "DELIVERED")
                Log.d("WS", "Ack sent for $messageId")
            }

        }, { error ->
            Log.e("WS", "Ack failed", error)
        })
    }

    /**
     * Sends an acknowledgment for a received group message.
     * /message.ack
     */
    @SuppressLint("CheckResult")
    fun acknowledgeGroupMessage(messageId: String) {
        val ackData = mapOf("messageId" to messageId)
        val jsonAck = Gson().toJson(ackData)

        stompClient?.send("/app/group.ack", jsonAck)?.subscribe({

            CoroutineScope(Dispatchers.IO).launch {
                messageDao?.updateGroupMessageStatus(messageId, "DELIVERED")
                Log.d("WS", "Ack sent for $messageId")
            }

        }, { error ->
            Log.e("WS", "Ack failed", error)
        })
    }

    fun disconnect() {
        // When disconnecting set user status to offline
        CoroutineScope(Dispatchers.IO).launch {
            authRepository?.updateStatus("OFFLINE")
            stompClient?.disconnect()
        }
    }
}