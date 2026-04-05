package capstone.safeline.ui.calling


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.theme.ThemeManager
import capstone.safeline.webrtc.SignalingClient
import capstone.safeline.webrtc.WebRTCManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.webrtc.*
import kotlin.jvm.java

class ContactCall : ComponentActivity() {

    private lateinit var webRTCManager: WebRTCManager
    private lateinit var signalingClient: SignalingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("callerName") ?: "Contact"
        val targetUserId = intent.getStringExtra("targetUserId") ?: name

        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)

        webRTCManager = WebRTCManager(this).also { it.init() }
        signalingClient = SignalingClient("http://10.0.2.2:8093/ws-call/websocket")

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        setContent {
            val scope = rememberCoroutineScope()
            var callStatus by remember { mutableStateOf("Calling $name...") }

            LaunchedEffect(Unit) {
                scope.launch {
                    val token = dataStoreManager.tokenFlow.first()
                    val currentUserId = token?.let {
                        extractUserIdFromJwt(it)
                    } ?: dataStoreManager.usernameFlow.first()

                    android.util.Log.d("CALL", "Calling as: $currentUserId to: $targetUserId")

                    // Set callbacks before connect() — onConnected fires when STOMP is open.
                    // sendOffer() called on a not-yet-open connection is silently dropped.
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
                            "decline" -> {
                                runOnUiThread {
                                    callStatus = "Call Declined"
                                    android.os.Handler(mainLooper).postDelayed({
                                        startActivity(Intent(this@ContactCall, Contacts::class.java))
                                        finish()
                                    }, 1500)
                                }
                            }
                            "hangup", "end" -> {
                                runOnUiThread {
                                    webRTCManager.hangUp()
                                    signalingClient.disconnect()
                                    startActivity(Intent(this@ContactCall, Contacts::class.java))
                                    finish()
                                }
                            }
                            "error" -> {
                                runOnUiThread { callStatus = "Connection Error" }
                            }
                        }
                    }

                    // Start WebRTC negotiation only after STOMP is open so sendOffer
                    // goes out on a live connection instead of being silently dropped.
                    signalingClient.onConnected = {
                        android.util.Log.d("CALL", "STOMP open — creating offer")
                        webRTCManager.createPeerConnection(object : PeerConnection.Observer {
                            override fun onIceCandidate(candidate: IceCandidate?) {
                                candidate?.let {
                                    signalingClient.sendIceCandidate(targetUserId, it.sdp, currentUserId)
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
                            override fun onTrack(transceiver: RtpTransceiver?) {
                                val track = transceiver?.receiver?.track() as? AudioTrack
                                track?.setEnabled(true)
                            }
                            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                            override fun onAddStream(p0: MediaStream?) {}
                            override fun onRemoveStream(p0: MediaStream?) {}
                            override fun onDataChannel(p0: DataChannel?) {}
                            override fun onRenegotiationNeeded() {}
                            override fun onIceConnectionReceivingChange(p0: Boolean) {}
                        })

                        webRTCManager.createOffer(object : SdpObserver {
                            override fun onCreateSuccess(sdp: SessionDescription?) {
                                sdp?.let {
                                    webRTCManager.setLocalDescription(it, object : SdpObserver {
                                        override fun onSetSuccess() {
                                            signalingClient.sendOffer(targetUserId, it.description, currentUserId)
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

                    signalingClient.connect(currentUserId, token ?: "")
                }
            }

            ContactCallScreen(
                name = name,
                callStatus = callStatus,
                onEndCall = {
                    scope.launch {
                        val token = dataStoreManager.tokenFlow.first()
                        val currentUserId = token?.let {
                            extractUserIdFromJwt(it)
                        } ?: dataStoreManager.usernameFlow.first()
                        signalingClient.sendHangup(targetUserId, currentUserId)
                        webRTCManager.hangUp()
                        signalingClient.disconnect()
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
private fun ContactCallScreen(
    name: String,
    callStatus: String,
    onEndCall: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
            Image(
                painter = painterResource(R.drawable.contactcall_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(ThemeManager.backgroundGradient))
            )
        }

        StrokeText(
            text = callStatus,
            fontFamily = ThemeManager.fontFamily,
            fontSize = 36.sp,
            fillColor = Color.White,
            strokeColor = Color(0xFF0066FF),
            strokeWidth = 2f,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 90.dp)
        )

        Image(
            painter = painterResource(R.drawable.contactcall_endcall_btn),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .size(width = 126.dp, height = 118.dp)
                .clickable { onEndCall() },
            contentScale = ContentScale.Fit
        )
    }
}