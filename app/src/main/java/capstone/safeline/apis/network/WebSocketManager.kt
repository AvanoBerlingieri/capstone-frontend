package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.util.Log
import capstone.safeline.apis.dto.messaging.IncomingGroupMessage
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

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

    fun init(repo: AuthRepository) {
        this.authRepository = repo
    }

    private val gatewayWsUrl = "ws://10.0.2.2:8091/ws"
    private var stompClient: StompClient? = null
    private var isConnecting = false

    @SuppressLint("CheckResult")
    fun connect(token: String) {
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
                        val success = authRepository?.updateStatus("ONLINE")
                        Log.d("WS", "Status update to ONLINE successful: $success")
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
        // Listen for private messages
        stompClient?.topic("/user/queue/messages")?.subscribe { topicMessage ->
            Log.d("WS", "Received Private Message: ${topicMessage.payload}")
            // TODO: Lhek parse the message and insert into the DB
        }

        // Listen for delivery acknowledgments
        stompClient?.topic("/user/queue/delivery")?.subscribe { ack ->
            Log.d("WS", "Message delivered to peer: ${ack.payload}")
            // TODO: Lhek update the message status to delivered and send ack
        }
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
     * /message.send
     */
    @SuppressLint("CheckResult")
    fun sendPrivateMessage(message: IncomingMessage) {
        val jsonMessage = Gson().toJson(message)
        stompClient?.send("/app/message.send", jsonMessage)?.subscribe({
            Log.d("WS", "Private message sent to server")
        }, { error ->
            Log.e("WS", "Failed to send private message", error)
        })
        // TODO: Save to rooms db and send private ack
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
            authRepository?.updateStatus("Offline")
            stompClient?.disconnect()
        }
    }
}