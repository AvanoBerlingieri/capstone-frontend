package capstone.safeline

import android.app.Application
import capstone.safeline.apis.network.ApiClient
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.data.security.CryptoManager

class SafelineApplication : Application() {

    // 1. Setup Security for the DataStore
    val cryptoManager by lazy { CryptoManager() }

    // 2. Setup DataStoreManager (Now with the required CryptoManager!)
    val dataStoreManager by lazy { DataStoreManager(context = this, cryptoManager) }

    // 3. Setup the Local Database
    val database by lazy { AppDatabase.getDatabase(context = this) }

    // 4. Setup the Repository (Renamed to messageRepository to match Chat.kt!)
    val messageRepository by lazy {
        MessageRepository(
            messageDao = database.messageDao(),
            apiService = ApiClient.provideMessageApiService(context = this, dataStoreManager)
        )
    }

    // 5. Setup the WebSocket Listener (Updated to pass messageRepository)
    val webSocketManager by lazy { WebSocketManager(messageRepository) }

    override fun onCreate() {
        super.onCreate()

        // 6. Start the connection
        webSocketManager.connect()

        // 7. THE GLUE: Give the repository a reference to the websocket
        // This allows repository.sendMessage() to actually work!
        messageRepository.setWebSocketManager(webSocketManager)
    }
}