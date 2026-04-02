package capstone.safeline.webrtc

import android.content.Context
import android.media.AudioManager
import android.os.Build
import org.webrtc.*

class WebRTCManager(private val context: Context) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    // Check if running on emulator
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic"))
    }

    fun init() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer)

        // Only add audio on real devices — emulator crashes with WebRTC audio
        if (!isEmulator()) {
            addLocalAudio()
        }

        return peerConnection
    }

    private fun addLocalAudio() {
        try {
            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
            localAudioTrack?.setEnabled(true)
            val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
            localStream.addTrack(localAudioTrack)
            peerConnection?.addStream(localStream)
        } catch (e: Exception) {
            android.util.Log.e("WebRTC", "Audio init failed: ${e.message}")
        }
    }

    fun createOffer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }
        peerConnection?.createOffer(sdpObserver, constraints)
    }

    fun createAnswer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }
        peerConnection?.createAnswer(sdpObserver, constraints)
    }

    fun setLocalDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection?.setLocalDescription(observer, sdp)
    }

    fun setRemoteDescription(sdp: SessionDescription, observer: SdpObserver) {
        peerConnection?.setRemoteDescription(observer, sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun muteMicrophone() { localAudioTrack?.setEnabled(false) }
    fun unmuteMicrophone() { localAudioTrack?.setEnabled(true) }

    fun setSpeakerOn(speakerOn: Boolean) {
        if (!isEmulator()) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.isSpeakerphoneOn = speakerOn
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        }
    }

    fun hangUp() {
        localAudioTrack?.setEnabled(false)
        peerConnection?.close()
        peerConnection = null
    }
}