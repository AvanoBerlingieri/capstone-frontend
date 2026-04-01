package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.util.Log
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage

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

    @SuppressLint("CheckResult")
    fun connect(token: String) {
        // Prevent multiple simultaneous connection attempts
        if (stompClient?.isConnected == true || isConnecting) return

        isConnecting = true
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, gatewayWsUrl)

        val headers = listOf(StompHeader("Authorization", "Bearer $token"))

        stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    isConnecting = false
                    Log.d("WS", "Connected to Gateway!")
                    subscribeToMessages()
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
        stompClient?.topic("/user/queue/messages")?.subscribe { topicMessage: StompMessage ->
            Log.d("WS", "Received: ${topicMessage.payload}")
        }
    }

    fun disconnect() {
        stompClient?.disconnect()
    }
}