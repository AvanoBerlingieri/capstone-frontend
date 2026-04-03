package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.util.Log
import capstone.safeline.apis.dto.messaging.IncomingGroupMessage
import capstone.safeline.apis.dto.messaging.IncomingMessage
import com.google.gson.Gson
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.util.UUID

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

    private val gatewayWsUrl = "ws://10.0.2.2:8091/ws"
    private var stompClient: StompClient? = null
    private var isConnecting = false

    @Volatile
    var onPrivateMessagePayload: ((String) -> Unit)? = null

    @SuppressLint("CheckResult")
    fun connect(token: String) {

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, gatewayWsUrl)
        // Prevent multiple simultaneous connection attempts
        if (stompClient?.isConnected == true || isConnecting) return

        isConnecting = true
        val headers = listOf(StompHeader("Authorization", "Bearer $token"))

        stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {

                LifecycleEvent.Type.OPENED -> {
                    isConnecting = false
                    Log.d("WS", "Connected to Gateway!")

                    // Listens for new messages in queue and message acknowledgments
                    subscribeToMessages()

                    // Trigger sync for massages not delivered
                    stompClient?.send("/app/message.sync")?.subscribe({
                        Log.d("WS", "Sync request sent successfully")
                    }, { error ->
                        Log.e("WS", "Failed to send sync request", error)
                    })
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
                // TODO: Lhek save group messages to Room DB
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
        stompClient?.disconnect()
    }

}