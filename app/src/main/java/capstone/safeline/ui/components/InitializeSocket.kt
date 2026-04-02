package capstone.safeline.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import capstone.safeline.apis.extractUserIdFromJwt // Assuming this is the correct import path
import capstone.safeline.apis.network.ApiClientAuth
import capstone.safeline.apis.network.ApiClientMessaging
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.data.security.CryptoManager

@Composable
fun InitializeSocket() {
    // ALWAYS use applicationContext for databases to prevent memory leaks
    val context = LocalContext.current.applicationContext

    // --- 1. SETUP AUTH & STORAGE ---
    val dsManager = remember { DataStoreManager(context, CryptoManager()) }
    val authRepo = remember {
        AuthRepository(
            dsManager,
            ApiClientAuth.provideApiService(context, dsManager)
        )
    }

    // --- 2. SETUP DATABASE & APIS ---
    val database = remember { AppDatabase.getDatabase(context) }
    val apiServiceMessage = remember {
        ApiClientMessaging.provideMessageApiService(context, dsManager)
    }

    val wsManager = WebSocketManager.getInstance()
    val token by authRepo.tokenFlow.collectAsState(initial = null)

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {

            // --- 3. GET CURRENT USER ID ---
            val currentUserId = extractUserIdFromJwt(token!!)

            if (currentUserId != null) {
                Log.d("InitializeSocket", "Successfully extracted User ID: $currentUserId")

                // --- 4. BUILD THE REPOSITORY ---
                val messageRepo = MessageRepository(
                    messageDao = database.messageDao(),
                    groupMessageDao = database.groupMessageDao(),
                    apiService = apiServiceMessage,
                    currentUserId = currentUserId
                )

                // --- 5. THE HANDSHAKE ---
                wsManager.messageListener = messageRepo

                // --- 6. CONNECT ---
                wsManager.connect(token!!)
            } else {
                Log.e("InitializeSocket", "CRITICAL: Could not extract User ID from Token!")
            }
        } else {
            // If the token is null (e.g., user logged out), kill the socket connection
            Log.d("InitializeSocket", "Token is null. Disconnecting socket.")
            wsManager.disconnect()
        }
    }
}