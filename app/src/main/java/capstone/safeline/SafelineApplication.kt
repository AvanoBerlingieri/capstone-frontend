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
    val dataStoreManager by lazy { DataStoreManager(this, cryptoManager) }

    // 3. Setup the Local Database
    val database by lazy { AppDatabase.getDatabase(this) }

    // 4. Setup the Repository (Armed with both Local Room DB and Network API)
    val repository by lazy {
        MessageRepository(
            messageDao = database.messageDao(),
            apiService = ApiClient.provideMessageApiService(this, dataStoreManager)
        )
    }

    // 5. Setup the WebSocket Listener
    val webSocketManager by lazy { WebSocketManager(repository) }

    override fun onCreate() {
        super.onCreate()

        // 6. Start listening for messages the moment the app opens
        webSocketManager.connect()
    }
}