package capstone.safeline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.FriendRepository
import capstone.safeline.data.repository.MessageRepository

@Composable
fun InitializeSocket() {
    val context = LocalContext.current

    val authRepo = remember { AuthRepository.getInstance(context) }
    val db = remember { AppDatabase.getDatabase(context) }
    val friendRepo = remember { FriendRepository.getInstance(context) }
    val msgRepo = remember { MessageRepository.getInstance(context, db.messageDao()) }
    val ws = remember { WebSocketManager.getInstance() }

    LaunchedEffect(Unit) {
        ws.init(authRepo, db.messageDao(), friendRepo, msgRepo)

        authRepo.tokenFlow.collect { token ->
            if (!token.isNullOrBlank()) {
                ws.connect(token)
            }
        }
    }
}