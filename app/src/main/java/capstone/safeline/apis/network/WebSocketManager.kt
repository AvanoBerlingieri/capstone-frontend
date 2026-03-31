package capstone.safeline.apis.network

import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.data.local.entity.MessageEntity
import okhttp3.*
import org.json.JSONObject
import java.util.UUID

class WebSocketManager(private val repository: MessageRepository) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder().url("ws://10.0.2.2:8091/ws").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onMessage(webSocket: WebSocket, text: String) {
                // 1. We "Catch" the message from the server
                val json = JSONObject(text)

                // 2. Convert JSON to our MessageEntity
                val incomingMessage = MessageEntity(
                    messageUuid = json.optString("id", UUID.randomUUID().toString()),
                    senderId = json.getString("senderId"),
                    receiverId = json.getString("receiverId"),
                    content = json.getString("content"),
                    timestamp = System.currentTimeMillis(),
                    status = "RECEIVED"
                )

                // 3. Hand it to the repository to save in Room!
                // (Note: This needs to be in a Coroutine scope, which we'll add next)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket Connection Failed: ${t.message}")
            }
        })
    }

    fun sendMessage(text: String) {
        webSocket?.send(text)
    }
}