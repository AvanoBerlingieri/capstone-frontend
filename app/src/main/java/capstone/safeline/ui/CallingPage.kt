package capstone.safeline.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
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
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.webrtc.SignalingClient
import capstone.safeline.webrtc.WebRTCManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class CallingPage : ComponentActivity() {

    private lateinit var webRTCManager: WebRTCManager
    private lateinit var signalingClient: SignalingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("userName") ?: "Friend"
        val targetUserId = intent.getStringExtra("targetUserId") ?: username
        val incomingSdp = intent.getStringExtra("incomingSdp") // null if outgoing call

        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)

        webRTCManager = WebRTCManager(this).also { it.init() }
        signalingClient = SignalingClient("ws://10.0.2.2:8093/ws-call/websocket")

        // Request mic permission
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        setContent {
            val scope = rememberCoroutineScope()
            var callStatus by remember { mutableStateOf("Calling") }
            var isMuted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                scope.launch {
                    val currentUserId = dataStoreManager.usernameFlow.first()

                    signalingClient.connect(currentUserId)

                    // Listen for signals from the other side
                    signalingClient.onSignalReceived = { signal ->
                        when (signal.type) {
                            "answer" -> {
                                webRTCManager.setRemoteDescription(
                                    SessionDescription(
                                        SessionDescription.Type.ANSWER,
                                        signal.sdp
                                    ),
                                    object : SdpObserver {
                                        override fun onSetSuccess() {
                                            runOnUiThread { callStatus = "Connected" }
                                        }
                                        override fun onSetFailure(p0: String?) {}
                                        override fun onCreateSuccess(p0: SessionDescription?) {}
                                        override fun onCreateFailure(p0: String?) {}
                                    }
                                )
                            }
                            "ice-candidate" -> {
                                signal.candidate?.let {
                                    webRTCManager.addIceCandidate(IceCandidate("", 0, it))
                                }
                            }
                            "hangup", "end" -> {
                                runOnUiThread {
                                    webRTCManager.hangUp()
                                    signalingClient.disconnect()
                                    finish()
                                }
                            }
                            "decline" -> {
                                runOnUiThread {
                                    callStatus = "Call Declined"
                                    android.os.Handler(mainLooper).postDelayed({
                                        finish()
                                    }, 1500)
                                }
                            }
                            "error" -> {
                                runOnUiThread { callStatus = "Connection Error" }
                            }
                        }
                    }

                    // Build peer connection observer
                    val peerObserver = object : PeerConnection.Observer {
                        override fun onIceCandidate(candidate: IceCandidate?) {
                            candidate?.let {
                                signalingClient.sendIceCandidate(
                                    targetUserId, it.sdp, currentUserId
                                )
                            }
                        }
                        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                            runOnUiThread {
                                callStatus = when (state) {
                                    PeerConnection.IceConnectionState.CONNECTED -> "Connected"
                                    PeerConnection.IceConnectionState.DISCONNECTED -> "Disconnected"
                                    PeerConnection.IceConnectionState.FAILED -> "Connection Failed"
                                    else -> callStatus
                                }
                            }
                        }
                        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                        override fun onAddStream(p0: MediaStream?) {}
                        override fun onRemoveStream(p0: MediaStream?) {}
                        override fun onDataChannel(p0: DataChannel?) {}
                        override fun onRenegotiationNeeded() {}
                        override fun onIceConnectionReceivingChange(p0: Boolean) {}
                    }

                    webRTCManager.createPeerConnection(peerObserver)

                    if (incomingSdp != null) {
                        // Incoming call — set remote description and send answer
                        webRTCManager.setRemoteDescription(
                            SessionDescription(SessionDescription.Type.OFFER, incomingSdp),
                            object : SdpObserver {
                                override fun onSetSuccess() {
                                    webRTCManager.createAnswer(object : SdpObserver {
                                        override fun onCreateSuccess(sdp: SessionDescription?) {
                                            sdp?.let {
                                                webRTCManager.setLocalDescription(
                                                    it,
                                                    object : SdpObserver {
                                                        override fun onSetSuccess() {
                                                            signalingClient.sendAnswer(
                                                                targetUserId,
                                                                it.description,
                                                                currentUserId
                                                            )
                                                        }
                                                        override fun onSetFailure(p0: String?) {}
                                                        override fun onCreateSuccess(p0: SessionDescription?) {}
                                                        override fun onCreateFailure(p0: String?) {}
                                                    }
                                                )
                                            }
                                        }
                                        override fun onCreateFailure(p0: String?) {}
                                        override fun onSetSuccess() {}
                                        override fun onSetFailure(p0: String?) {}
                                    })
                                }
                                override fun onSetFailure(p0: String?) {}
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onCreateFailure(p0: String?) {}
                            }
                        )
                    } else {
                        // Outgoing call — create and send offer
                        webRTCManager.createOffer(object : SdpObserver {
                            override fun onCreateSuccess(sdp: SessionDescription?) {
                                sdp?.let {
                                    webRTCManager.setLocalDescription(it, object : SdpObserver {
                                        override fun onSetSuccess() {
                                            signalingClient.sendOffer(
                                                targetUserId,
                                                it.description,
                                                currentUserId
                                            )
                                        }
                                        override fun onSetFailure(p0: String?) {}
                                        override fun onCreateSuccess(p0: SessionDescription?) {}
                                        override fun onCreateFailure(p0: String?) {}
                                    })
                                }
                            }
                            override fun onCreateFailure(p0: String?) {
                                runOnUiThread { callStatus = "Failed to start call" }
                            }
                            override fun onSetSuccess() {}
                            override fun onSetFailure(p0: String?) {}
                        })
                    }
                }
            }

            CallingFriendScreen(
                username = username,
                callStatus = callStatus,
                onEndCall = {
                    scope.launch {
                        val currentUserId = dataStoreManager.usernameFlow.first()
                        signalingClient.sendHangup(targetUserId, currentUserId)
                        webRTCManager.hangUp()
                        signalingClient.disconnect()
                    }
                    finish()
                },
                onGoToChat = {
                    val intent = Intent(this, DmPage::class.java)
                    intent.putExtra("userName", username)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                },
                onShareScreen = {},
                onCamera = {},
                onMic = {
                    isMuted = !isMuted
                    if (isMuted) webRTCManager.muteMicrophone()
                    else webRTCManager.unmuteMicrophone()
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
fun CallingFriendScreen(
    username: String,
    callStatus: String = "Calling",
    onEndCall: () -> Unit,
    onGoToChat: () -> Unit,
    onShareScreen: () -> Unit,
    onCamera: () -> Unit,
    onMic: () -> Unit
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
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(241.26.dp, 253.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(0.dp))

                // Dynamic call status
                StrokeText(
                    text = callStatus,
                    fontFamily = Vampiro,
                    fontSize = 20.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-34).dp)
                        .width(200.dp)
                        .height(39.51.dp)
                )

                StrokeText(
                    text = username,
                    fontFamily = Vampiro,
                    fontSize = 20.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-44).dp)
                        .width(106.56.dp)
                        .height(39.51.dp)
                )
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .width(110.dp)
                        .offset(x = (-25).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    IconBtn(R.drawable.share_screen_button, 92.dp, 94.dp, onShareScreen)
                    IconBtn(R.drawable.go_to_chat_button, 92.dp, 94.dp, onGoToChat)
                }

                Box(
                    modifier = Modifier.offset(x = (-15).dp)
                ) {
                    IconBtn(R.drawable.end_call_button, 181.dp, 159.dp, onEndCall, true)
                }

                Column(
                    modifier = Modifier.width(110.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    IconBtn(R.drawable.camera_button, 92.dp, 94.dp, onCamera)
                    IconBtn(R.drawable.microphone_button, 92.dp, 94.dp, onMic)
                }
            }
        }
    }
}

@Composable
private fun IconBtn(
    res: Int,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    big: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.size(if (big) width else 72.dp),
            contentScale = ContentScale.Fit
        )
    }
}