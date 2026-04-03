package capstone.safeline.webrtc

data class SignalMessage(
    val type: String,           // "offer","answer","ice-candidate","hangup","decline","end","error"
    val senderId: String,
    val targetUserId: String,
    val sdp: String?,
    val candidate: String?,
    val errorMessage: String? = null,
    val roomId: String? = null  // for group calls
)