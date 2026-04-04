package capstone.safeline.apis.network

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.dto.messaging.OutgoingMessage
import capstone.safeline.data.local.dao.MessageDao
import capstone.safeline.data.local.entity.MessageEntity
import capstone.safeline.data.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class WebSocketManager {
    companion object {
        private const val PRIVATE_SEND_MAX_ATTEMPTS = 4
        private const val PRIVATE_SEND_RETRY_DELAY_MS = 2_000L

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

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    fun init(repo: AuthRepository, dao: MessageDao) {
        this.authRepository = repo
        this.messageDao = dao
    }

    private val gatewayWsUrl = "ws://10.0.2.2:8091/ws"
    private var stompClient: StompClient? = null
    private var isConnecting = false

    @SuppressLint("CheckResult")
    fun connect(token: String) {
        if (authRepository == null || messageDao == null) return
        if (stompClient?.isConnected == true || isConnecting) return

        isConnecting = true

        if (stompClient == null) {
            // Optimize OkHttpClient for faster handshakes on Pixel devices
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, gatewayWsUrl, null, okHttpClient)
        }

        val headers = listOf(StompHeader("Authorization", "Bearer $token"))

        stompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    isConnecting = false
                    _connectionStatus.value = true
                    Log.d("WS", "Connected to Gateway!")

                    CoroutineScope(Dispatchers.IO).launch {
                        authRepository?.updateStatus("ONLINE")
                    }
                    subscribeToMessages()
                    stompClient?.send("/app/message.sync")?.subscribe({}, {})
                }
                LifecycleEvent.Type.ERROR -> {
                    isConnecting = false
                    _connectionStatus.value = false
                    Log.e("WS", "Connection Error", lifecycleEvent.exception)
                }
                LifecycleEvent.Type.CLOSED -> {
                    isConnecting = false
                    _connectionStatus.value = false
                }
                else -> {}
            }
        }
        stompClient?.connect(headers)
    }

    @SuppressLint("CheckResult")
    private fun subscribeToMessages() {
        val gson = Gson()
        stompClient?.topic("/user/queue/messages")?.subscribe { topicMessage ->
            try {
                val msg = gson.fromJson(topicMessage.payload, OutgoingMessage::class.java)
                CoroutineScope(Dispatchers.IO).launch {
                    val entity = MessageEntity(
                        messageId = msg.messageId,
                        senderId = msg.senderId,
                        receiverId = msg.receiverId,
                        content = msg.content,
                        timestamp = msg.timestamp,
                        status = "DELIVERED",
                        isMine = false
                    )
                    messageDao?.insertPrivateMessage(entity)
                    acknowledgeMessage(msg.messageId)
                }
            } catch (e: Exception) { Log.e("WS", "Error processing message", e) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendPrivateMessage(message: IncomingMessage) {
        val jsonMessage = Gson().toJson(message)
        CoroutineScope(Dispatchers.IO).launch {
            repeat(PRIVATE_SEND_MAX_ATTEMPTS) { attempt ->
                if (stompClient?.isConnected != true) {
                    val token = authRepository?.tokenFlow?.first()
                    if (!token.isNullOrBlank()) connect(token)
                    delay(PRIVATE_SEND_RETRY_DELAY_MS)
                }

                if (trySendPrivateStomp(jsonMessage)) {
                    persistOutboundPrivateMessage(message)
                    return@launch
                }
                delay(PRIVATE_SEND_RETRY_DELAY_MS)
            }
        }
    }

    private suspend fun trySendPrivateStomp(jsonMessage: String): Boolean =
        suspendCancellableCoroutine { cont ->
            val client = stompClient
            if (client == null || !client.isConnected) {
                if (cont.isActive) cont.resume(false)
                return@suspendCancellableCoroutine
            }
            val d = client.send("/app/message.send", jsonMessage).subscribe(
                { if (cont.isActive) cont.resume(true) },
                { if (cont.isActive) cont.resume(false) }
            )
            cont.invokeOnCancellation { d.dispose() }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun persistOutboundPrivateMessage(message: IncomingMessage) {
        val myId = authRepository?.userIdFlow?.first() ?: ""
        val entity = MessageEntity(
            messageId = message.messageId,
            senderId = myId,
            receiverId = message.receiver,
            content = message.content,
            timestamp = Instant.now().toString(),
            status = "SENT",
            isMine = true
        )
        messageDao?.insertPrivateMessage(entity)
    }

    @SuppressLint("CheckResult")
    fun acknowledgeMessage(messageId: String) {
        val jsonAck = Gson().toJson(mapOf("messageId" to messageId))
        stompClient?.send("/app/message.ack", jsonAck)?.subscribe({
            CoroutineScope(Dispatchers.IO).launch {
                messageDao?.updateMessageStatus(messageId, "DELIVERED")
            }
        }, {})
    }

    fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            authRepository?.updateStatus("OFFLINE")
            stompClient?.disconnect()
            _connectionStatus.value = false
        }
    }
}