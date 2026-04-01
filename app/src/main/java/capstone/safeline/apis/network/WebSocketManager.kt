package capstone.safeline.apis.network

import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.util.UUID

class WebSocketManager(private val repository: MessageRepository) {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // We need this scope to save messages to the database from the background
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        // Use 10.0.2.2 for Android Emulator to hit your laptop's localhost
        val request = Request.Builder().url("ws://10.0.2.2:8091/ws").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)

                    val incomingMessage = MessageEntity(
                        messageUuid = json.optString("id", UUID.randomUUID().toString()),
                        senderId = json.getString("senderId"),
                        receiverId = json.getString("receiverId"),
                        content = json.getString("content"),
                        timestamp = System.currentTimeMillis(),
                        status = "RECEIVED"
                    )

                    // Launch a coroutine to save the message to Room
                    scope.launch {
                        repository.receiveAndSaveMessage(incomingMessage)
                    }
                } catch (e: Exception) {
                    println("Error parsing incoming WebSocket message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket Connection Failed: ${t.message}")
            }
        })
    }

    // Renamed to avoid confusion with the Repository's sendMessage
    fun sendRawMessage(jsonText: String) {
        webSocket?.send(jsonText)
    }
}