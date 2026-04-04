package capstone.safeline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.repository.AuthRepository

@Composable
fun InitializeSocket() {
    val context = LocalContext.current

    val repo = remember { AuthRepository.getInstance(context) }
    val db = remember { AppDatabase.getDatabase(context) }
    val ws = remember { WebSocketManager.getInstance() }

    LaunchedEffect(Unit) {
        ws.init(repo, db.messageDao())

        repo.tokenFlow.collect { token ->
            if (!token.isNullOrBlank()) {
                ws.connect(token)
            }
        }
    }
}