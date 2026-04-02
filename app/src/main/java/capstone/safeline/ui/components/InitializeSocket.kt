package capstone.safeline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import capstone.safeline.apis.network.ApiClientAuth
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager

@Composable
fun InitializeSocket() {
    val context = LocalContext.current
    val dsManager = remember { DataStoreManager(context, CryptoManager()) }
    val repo = remember {
        AuthRepository(
            dsManager,
            ApiClientAuth.provideApiService(context, dsManager)
        )
    }
    val wsManager = WebSocketManager.getInstance()

    //TODO: Lhek add db here


    val token by repo.tokenFlow.collectAsState(initial = null)

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            wsManager.connect(token!!)
        }
    }
}