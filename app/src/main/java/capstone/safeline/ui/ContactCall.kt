package capstone.safeline.ui

import android.content.Intent
import android.content.Intent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.webrtc.SignalingClient
import capstone.safeline.webrtc.WebRTCManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.DataChannel
import org.webrtc.MediaConstraints
import org.webrtc.SessionDescription
import org.webrtc.SdpObserver

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

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
        signalingClient = SignalingClient("ws://10.0.2.2:8093/ws-call/websocket")

        // Request mic permission
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        setContent {
            ContactCallScreen(
                name = name,
                onEndCall = {
                    startActivity(Intent(this, Contacts::class.java))
                    finish()
                }
            )
        }
            val scope = rememberCoroutineScope()
            var callStatus by remember { mutableStateOf("Calling $name...") }

            LaunchedEffect(Unit) {
                scope.launch {
                    // Get current username from DataStore
                    val currentUserId = dataStoreManager.usernameFlow.first()

                    // Connect to signaling server
                    signalingClient.connect(currentUserId)

                    // Listen for responses from the other side
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

                    // Create peer connection and send offer
                    webRTCManager.createPeerConnection(object : PeerConnection.Observer {
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
                    })

                    // Create and send offer to target user
                    webRTCManager.createOffer(object : SdpObserver {
                        override fun onCreateSuccess(sdp: SessionDescription?) {
                            sdp?.let {
                                webRTCManager.setLocalDescription(it, object : SdpObserver {
                                    override fun onSetSuccess() {
                                        signalingClient.sendOffer(
                                            targetUserId, it.description, currentUserId
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

            ContactCallScreen(
                name = name,
                callStatus = callStatus,
                onEndCall = {
                    scope.launch {
                        val currentUserId = dataStoreManager.usernameFlow.first()
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
    onEndCall: () -> Unit
) {
private fun ContactCallScreen(name: String) {
private fun ContactCallScreen(
    name: String,
    callStatus: String,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.contactcall_bg),
            painter = painterResource(R.drawable.calls_bg),
            painter = painterResource(R.drawable.contactcall_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        StrokeText(
            text = "Calling $name...",
            fontFamily = Vampiro,
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
