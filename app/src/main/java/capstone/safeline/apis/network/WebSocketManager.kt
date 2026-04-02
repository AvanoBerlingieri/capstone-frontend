package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.util.Log
import capstone.safeline.apis.dto.messaging.IncomingGroupMessage
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.dto.messaging.OutgoingGroupMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

// 1. Define the Listener Interface
interface WebSocketMessageListener {
    fun onPrivateMessageReceived(message: OutgoingMessage)
    fun onGroupMessageReceived(message: OutgoingGroupMessage)
    fun onMessageDeliveredAck(messageId: String)
}

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

    // 2. Add a variable to hold the listener
    var messageListener: WebSocketMessageListener? = null
    private val gson = Gson()

    private val gatewayWsUrl = "ws://10.0.2.2:9000/ws"
    private var stompClient: StompClient? = null
    private var isConnecting = false

    @Volatile
    var onPrivateMessagePayload: ((String) -> Unit)? = null

    @SuppressLint("CheckResult")
    fun connect(token: String) {
        if (stompClient != null && (stompClient!!.isConnected || isConnecting)) return

        isConnecting = true

        // We create a custom OkHttpClient to "smuggle" the token
        // into the very first HTTP request headers.
        val httpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        // Pass this custom client to Stomp.over
        // Note: Use port 9000 first, if that fails, try 8091
        val socketUrl = "ws://10.0.2.2:9000/ws"

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, socketUrl, null, httpClient)

        stompClient?.lifecycle()?.subscribe({ lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    isConnecting = false
                    Log.d("WS", "SUCCESS: Connected to Gateway!")
                    subscribeToMessages()
                }
                LifecycleEvent.Type.ERROR -> {
                    isConnecting = false
                    Log.e("WS", "STOMP ERROR: ${lifecycleEvent.exception?.message}")
                }
                LifecycleEvent.Type.CLOSED -> {
                    isConnecting = false
                    Log.d("WS", "Socket Closed Gracefully")
                }
                else -> {}
            }
        }, { error ->
            Log.e("WS", "Unhandled Rx Error", error)
        })

        stompClient?.connect()
    }

    @SuppressLint("CheckResult")
    private fun subscribeToMessages() {
        // Listen for private messages
        stompClient?.topic("/user/queue/messages")?.subscribe { topicMessage ->
            Log.d("WS", "Received Private Message: ${topicMessage.payload}")
            try {
                // Parse the JSON into our DTO
                val message = gson.fromJson(topicMessage.payload, OutgoingMessage::class.java)

                // Pass it to the Repository (via the listener) to handle the DB insert
                messageListener?.onPrivateMessageReceived(message)

                // Immediately send an acknowledgment back to the server
                // Note: using message.id based on the DTO we structured earlier
                acknowledgeMessage(messageId = message.messageId.toString())
            } catch (e: Exception) {
                Log.e("WS", "Error parsing incoming private message", e)
            }
        }

        // Listen for delivery acknowledgments
        stompClient?.topic("/user/queue/delivery")?.subscribe { ack ->
            Log.d("WS", "Message delivered to peer: ${ack.payload}")
            try {
                // Parse the ack payload JSON object like {"messageId": "uuid"}
                val mapType = object : TypeToken<Map<String, String>>() {}.type
                val ackData: Map<String, String> = gson.fromJson(ack.payload, mapType)
                val deliveredMessageId = ackData["messageId"]

                if (deliveredMessageId != null) {
                    // Tell Repository to update Room DB status to DELIVERED
                    messageListener?.onMessageDeliveredAck(deliveredMessageId)
                }
            } catch (e: Exception) {
                Log.e("WS", "Error parsing ack", e)
            }
        }
    }

    // make it call api get all groups user is in
    @SuppressLint("CheckResult")
    private fun subscribeToGroups(groupIds: List<String>) {
        groupIds.forEach { id ->
            stompClient?.topic("/topic/room.$id")?.subscribe { msg ->
                Log.d("WS", "Group $id Message: ${msg.payload}")
                try {
                    // Change the parsing line to:
                    val groupMessage = gson.fromJson(msg.payload, OutgoingGroupMessage::class.java)

                    // Pass to Repository to save
                    messageListener?.onGroupMessageReceived(groupMessage)

                    // Send Ack back to server
                    acknowledgeGroupMessage(messageId = groupMessage.messageId.toString())
                } catch (e: Exception) {
                    Log.e("WS", "Error parsing group message", e)
                }
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
     * /group.ack
     */
    @SuppressLint("CheckResult")
    fun acknowledgeGroupMessage(messageId: String) {
        val ackData = mapOf("messageId" to messageId)
        val jsonAck = Gson().toJson(ackData)

        stompClient?.send("/app/group.ack", jsonAck)?.subscribe({
            Log.d("WS", "Ack sent for group message $messageId")
        }, { error ->
            Log.e("WS", "Ack failed for group", error)
        })
    }

    fun disconnect() {
        stompClient?.disconnect()
    }
}