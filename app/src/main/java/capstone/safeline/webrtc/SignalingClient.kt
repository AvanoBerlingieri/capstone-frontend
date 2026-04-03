package capstone.safeline.webrtc

import com.google.gson.Gson
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class SignalingClient(serverUrl: String) {

    private val stompClient: StompClient = Stomp.over(
        Stomp.ConnectionProvider.OKHTTP, serverUrl
    )
    var onSignalReceived: ((SignalMessage) -> Unit)? = null

    fun connect(userId: String) {
        stompClient.connect()
        stompClient.topic("/user/$userId/queue/call").subscribe { message ->
            val signal = Gson().fromJson(message.payload, SignalMessage::class.java)
            onSignalReceived?.invoke(signal)
        }
    }

    fun sendOffer(targetUserId: String, sdp: String, senderId: String) {
        val msg = SignalMessage("offer", senderId, targetUserId, sdp, null)
        stompClient.send("/app/call.offer", Gson().toJson(msg)).subscribe()
    }

    fun sendAnswer(targetUserId: String, sdp: String, senderId: String) {
        val msg = SignalMessage("answer", senderId, targetUserId, sdp, null)
        stompClient.send("/app/call.answer", Gson().toJson(msg)).subscribe()
    }

    fun sendIceCandidate(targetUserId: String, candidate: String, senderId: String) {
        val msg = SignalMessage("ice-candidate", senderId, targetUserId, null, candidate)
        stompClient.send("/app/call.ice", Gson().toJson(msg)).subscribe()
    }

    fun sendHangup(targetUserId: String, senderId: String) {
        val msg = SignalMessage("hangup", senderId, targetUserId, null, null)
        stompClient.send("/app/call.end", Gson().toJson(msg)).subscribe()
    }
    fun sendGroupJoin(roomId: String, senderId: String) {
        val msg = SignalMessage("group-join", senderId, "", null, null, null, roomId)
        stompClient.send("/app/group.join", Gson().toJson(msg)).subscribe()
    }

    fun sendGroupLeave(roomId: String, senderId: String) {
        val msg = SignalMessage("group-leave", senderId, "", null, null, null, roomId)
        stompClient.send("/app/group.leave", Gson().toJson(msg)).subscribe()
    }

    fun sendDecline(targetUserId: String, senderId: String) {
        val msg = SignalMessage("decline", senderId, targetUserId, null, null)
        stompClient.send("/app/call.decline", Gson().toJson(msg)).subscribe()
    }

    fun sendError(targetUserId: String, senderId: String, error: String) {
        val msg = SignalMessage("error", senderId, targetUserId, null, null, error)
        stompClient.send("/app/call.error", Gson().toJson(msg)).subscribe()
    }

    fun disconnect() = stompClient.disconnect()
}