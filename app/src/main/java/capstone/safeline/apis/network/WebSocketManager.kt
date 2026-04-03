package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import capstone.safeline.apis.dto.messaging.IncomingGroupMessage
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.MessageEntity
import capstone.safeline.data.repository.AuthRepository
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

    fun init(repo: AuthRepository, dao: MessageDao) {
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
                    messageDao?.insertMessage(entity)

                    // ack the message
                    acknowledgeMessage(msg.messageId)
                }
            } catch (e: Exception) {
                Log.e("WS", "Error processing message", e)
            }
        }
//        // Listen for delivery acknowledgments
//        stompClient?.topic("/user/queue/delivery")?.subscribe { ack ->
//            Log.d("WS", "Message delivered to peer: ${ack.payload}")
//        }
    }

    // make it call api get all groups user is in
    @SuppressLint("CheckResult")    // UUID works as String
    private fun subscribeToGroups(groupIds: List<String>) {
        groupIds.forEach { id ->
            stompClient?.topic("/topic/room.$id")?.subscribe { msg ->
                Log.d("WS", "Group $id Message: ${msg.payload}")
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

            // Save to room db
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
                messageDao?.insertMessage(entity)
            }
        }, { error ->
            Log.e("WS", "Failed to send private message", error)
            // Optionally update Room with status = "FAILED"
        })
    }

    /**
     * Sends a group message.
     * /group.send
     */
    @SuppressLint("CheckResult")
    fun sendGroupMessage(message: IncomingGroupMessage) {
        val jsonMessage = Gson().toJson(message)
        stompClient?.send("/app/group.send", jsonMessage)?.subscribe({
            Log.d("WS", "Group message sent to server")
        }, { error ->
            Log.e("WS", "Failed to send group message", error)
        })
        // TODO: Save to rooms db and send group ack

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
            Log.d("WS", "Ack sent for $messageId")
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
            Log.d("WS", "Ack sent for $messageId")
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