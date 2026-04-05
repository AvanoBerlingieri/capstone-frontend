package capstone.safeline.ui.calling

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.webrtc.SignalingClient
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class IncomingCallActivity : ComponentActivity() {

    private lateinit var signalingClient: SignalingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callerName = intent.getStringExtra("callerName") ?: "Unknown"
        val targetUserId = intent.getStringExtra("targetUserId") ?: ""
        val incomingSdp = intent.getStringExtra("incomingSdp") ?: ""

        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)
        signalingClient = SignalingClient("http://10.0.2.2:8093/ws-call/websocket")

        setContent {
            IncomingCallScreen(
                callerName = callerName,
                onAccept = {
                    val intent = Intent(this, CallingPage::class.java)
                    intent.putExtra("userName", callerName)
                    intent.putExtra("targetUserId", targetUserId)
                    intent.putExtra("incomingSdp", incomingSdp)
                    startActivity(intent)
                    finish()
                },
                onDecline = decline@{
                    if (targetUserId.isBlank()) {
                        finish()
                        return@decline
                    }
                    val finished = AtomicBoolean(false)
                    fun endDeclineFlow() {
                        if (!finished.compareAndSet(false, true)) return
                        signalingClient.disconnect()
                        finish()
                    }

                    lifecycleScope.launch {
                        try {
                            val token = withContext(Dispatchers.IO) {
                                dataStoreManager.tokenFlow.first()
                            }
                            val currentUserId = token?.let { extractUserIdFromJwt(it) }
                                ?: dataStoreManager.usernameFlow.first()

                            signalingClient.onConnected = {
                                signalingClient.sendDecline(targetUserId, currentUserId) {
                                    runOnUiThread { endDeclineFlow() }
                                }
                            }
                            signalingClient.connect(currentUserId, token ?: "")

                            launch {
                                delay(10_000)
                                runOnUiThread { endDeclineFlow() }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("INCOMING", "Decline error: ${e.message}")
                            endDeclineFlow()
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun IncomingCallScreen(
    callerName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(R.drawable.contactcall_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                StrokeText(
                    text = callerName,
                    fontFamily = Vampiro,
                    fontSize = 32.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 2f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                StrokeText(
                    text = "Incoming Call...",
                    fontFamily = Vampiro,
                    fontSize = 20.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(R.drawable.contactcall_endcall_btn),
                        contentDescription = "Decline",
                        modifier = Modifier
                            .size(width = 126.dp, height = 118.dp)
                            .clickable { onDecline() },
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(R.drawable.calls_anwsered_icon),
                        contentDescription = "Accept",
                        modifier = Modifier
                            .size(width = 126.dp, height = 118.dp)
                            .clickable { onAccept() },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}