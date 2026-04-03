package capstone.safeline.webrtc

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import org.webrtc.*

class WebRTCManager(private val context: Context) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null

    private val iceServers = listOf(
        PeerConnection.IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    // Detect emulator
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))
    }

    // Initialize WebRTC
    fun init() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()

        peerConnectionFactory = PeerConnectionFactory
            .builder()
            .setOptions(options)
            .createPeerConnectionFactory()

        startAudio()
    }

    // Create Peer Connection
    fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer)

        if (!isEmulator()) {
            addLocalAudio()
        }

        return peerConnection
    }

    // Add microphone audio
    private fun addLocalAudio() {
        try {
            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
            localAudioTrack?.setEnabled(true)

            peerConnection?.addTrack(localAudioTrack)
            Log.d("WebRTC", "Local audio track added")
        } catch (e: Exception) {
            Log.e("WebRTC", "Audio init failed: ${e.message}")
        }
    }

    // Create Offer
    fun createOffer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
        peerConnection?.createOffer(sdpObserver, constraints)
    }

    // Create Answer
    fun createAnswer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
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

    fun muteMicrophone() {
        localAudioTrack?.setEnabled(false)
    }

    fun unmuteMicrophone() {
        localAudioTrack?.setEnabled(true)
    }

    // Route audio to speaker
    private fun startAudio() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }

    fun setSpeakerOn(speakerOn: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = speakerOn
    }

    fun hangUp() {
        try {
            localAudioTrack?.setEnabled(false)
            peerConnection?.close()
            peerConnection = null
        } catch (e: Exception) {
            Log.e("WebRTC", "Hangup error: ${e.message}")
        }
    }
}