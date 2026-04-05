package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.apis.network.CallingApiClient
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.webrtc.SignalingClient
import capstone.safeline.webrtc.WebRTCManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

class GroupCallRoom : ComponentActivity() {

    private lateinit var webRTCManager: WebRTCManager
    private lateinit var signalingClient: SignalingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roomId = intent.getStringExtra("roomId") ?: ""
        val participants = intent.getStringArrayListExtra("participants") ?: arrayListOf()

        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)

        webRTCManager = WebRTCManager(this).also { it.init() }
        signalingClient = SignalingClient("http://10.0.2.2:8093/ws-call/websocket")

        setContent {
            val scope = rememberCoroutineScope()
            var activeParticipants by remember { mutableStateOf(participants.toList()) }
            var callStatus by remember { mutableStateOf("Connecting...") }
            var isMuted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        // Use UUID from token
                        val token = dataStoreManager.tokenFlow.first()
                        val userId = token?.let {
                            extractUserIdFromJwt(it)
                        } ?: dataStoreManager.usernameFlow.first()

                        android.util.Log.d("GROUP", "Connecting as: $userId")

                        // Connect with token
                        signalingClient.connect(userId, token ?: "")
                        callStatus = "Connected"

                        signalingClient.onSignalReceived = { signal ->
                            when (signal.type) {
                                "group-join" -> {
                                    runOnUiThread {
                                        if (!activeParticipants.contains(signal.senderId)) {
                                            activeParticipants = activeParticipants + signal.senderId
                                        }
                                    }
                                }
                                "group-leave" -> {
                                    runOnUiThread {
                                        activeParticipants = activeParticipants.filter {
                                            it != signal.senderId
                                        }
                                    }
                                }
                                "ice-candidate" -> {
                                    signal.candidate?.let {
                                        webRTCManager.addIceCandidate(
                                            org.webrtc.IceCandidate("", 0, it)
                                        )
                                    }
                                }
                                "hangup", "end" -> {
                                    runOnUiThread {
                                        webRTCManager.hangUp()
                                        signalingClient.disconnect()
                                        finish()
                                    }
                                }
                            }
                        }

                        signalingClient.sendGroupJoin(roomId, userId)

                    } catch (e: Exception) {
                        android.util.Log.e("GROUP", "Error: ${e.message}")
                    }
                }
            }

            GroupCallRoomScreen(
                roomId = roomId,
                callStatus = callStatus,
                participants = activeParticipants,
                isMuted = isMuted,
                onMute = {
                    isMuted = !isMuted
                    if (isMuted) webRTCManager.muteMicrophone()
                    else webRTCManager.unmuteMicrophone()
                },
                onLeave = {
                    scope.launch {
                        try {
                            val token = dataStoreManager.tokenFlow.first()
                            val userId = token?.let {
                                extractUserIdFromJwt(it)
                            } ?: dataStoreManager.usernameFlow.first()

                            CallingApiClient.service.leaveGroupRoom(roomId, userId)
                            signalingClient.sendGroupLeave(roomId, userId)
                            webRTCManager.hangUp()
                            signalingClient.disconnect()
                        } catch (e: Exception) {
                            android.util.Log.e("GROUP", "Leave error: ${e.message}")
                        }
                    }
                    startActivity(Intent(this, Contacts::class.java))
                    finish()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCManager.hangUp()
        signalingClient.disconnect()
    }
}

@Composable
private fun GroupCallRoomScreen(
    roomId: String,
    callStatus: String,
    participants: List<String>,
    isMuted: Boolean,
    onMute: () -> Unit,
    onLeave: () -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(R.drawable.top_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StrokeTitle(
                    text = "GROUP CALL",
                    fontFamily = Vampiro,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Room: $roomId",
                    fontFamily = Tapestry,
                    fontSize = 14.sp,
                    color = Color(0xFFD9D9D9),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                StrokeText(
                    text = callStatus,
                    fontFamily = Vampiro,
                    fontSize = 18.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "${participants.size} participant(s)",
                    fontFamily = Tapestry,
                    fontSize = 14.sp,
                    color = Color(0xFFD9D9D9)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(participants) { participant ->
                        ParticipantAvatar(name = participant)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Image(
                painter = painterResource(R.drawable.down_background_for_buttons),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(412.dp)
                    .height(367.dp),
                contentScale = ContentScale.FillBounds
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(412.dp)
                    .height(367.dp)
                    .padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GroupIconBtn(
                    res = R.drawable.microphone_button,
                    width = 92.dp,
                    height = 94.dp,
                    label = if (isMuted) "Unmute" else "Mute",
                    onClick = onMute
                )

                GroupIconBtn(
                    res = R.drawable.end_call_button,
                    width = 181.dp,
                    height = 159.dp,
                    label = "",
                    onClick = onLeave,
                    big = true
                )

                GroupIconBtn(
                    res = R.drawable.camera_button,
                    width = 92.dp,
                    height = 94.dp,
                    label = "Camera",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun ParticipantAvatar(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.avatar_placeholder),
            contentDescription = name,
            modifier = Modifier.size(70.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(4.dp))
        StrokeText(
            text = name,
            fontFamily = Vampiro,
            fontSize = 12.sp,
            fillColor = Color.White,
            strokeColor = Color(0xFF002BFF),
            strokeWidth = 1f,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(70.dp)
        )
    }
}

@Composable
private fun GroupIconBtn(
    res: Int,
    width: Dp,
    height: Dp,
    label: String,
    onClick: () -> Unit,
    big: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width, height)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(res),
                contentDescription = label,
                modifier = Modifier.size(if (big) width else 72.dp),
                contentScale = ContentScale.Fit
            )
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = Color.White,
                fontFamily = Tapestry,
                fontSize = 12.sp
            )
        }
    }
}