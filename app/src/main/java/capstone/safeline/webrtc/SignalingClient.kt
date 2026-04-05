package capstone.safeline.webrtc

import com.google.gson.Gson
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class SignalingClient(serverUrl: String) {

    private val stompClient: StompClient = Stomp.over(
        Stomp.ConnectionProvider.OKHTTP, serverUrl
    )
        .withClientHeartbeat(10000)
        .withServerHeartbeat(10000)

    var onSignalReceived: ((SignalMessage) -> Unit)? = null
    var onConnected: (() -> Unit)? = null

    // Spring user destinations: subscribe like chat (`/user/queue/messages`), not `/user/{id}/queue/...`.
    private val callUserQueue = "/user/queue/call"

    fun connect(userId: String, token: String = "") {
        try {
            val headers = if (token.isNotEmpty()) {
                listOf(StompHeader("Authorization", "Bearer $token"))
            } else {
                emptyList()
            }

            var openedCallbackFired = false
            fun fireOpenedOnce() {
                if (openedCallbackFired) return
                openedCallbackFired = true
                android.util.Log.d(
                    "SignalingClient",
                    "STOMP session ready for userId=$userId — subscribing to $callUserQueue"
                )
                stompClient.topic(callUserQueue)
                    .subscribe({ message ->
                        try {
                            android.util.Log.d("SignalingClient", "Received message: ${message.payload}")
                            val signal = Gson().fromJson(
                                message.payload, SignalMessage::class.java
                            )
                            onSignalReceived?.invoke(signal)
                        } catch (e: Exception) {
                            android.util.Log.e("SignalingClient", "Parse error: ${e.message}")
                        }
                    }, { error ->
                        android.util.Log.e("SignalingClient", "Topic error: ${error.message}")
                    })
                onConnected?.invoke()
            }

            stompClient.lifecycle()
                .subscribe({ event ->
                    when (event.type) {
                        LifecycleEvent.Type.OPENED -> {
                            android.util.Log.d("SignalingClient", "WebSocket OPENED")
                            fireOpenedOnce()
                        }
                        LifecycleEvent.Type.ERROR ->
                            android.util.Log.e("SignalingClient", "STOMP error: ${event.exception?.message}")
                        LifecycleEvent.Type.CLOSED ->
                            android.util.Log.d("SignalingClient", "STOMP closed")
                        else -> {}
                    }
                }, { error ->
                    android.util.Log.e("SignalingClient", "Lifecycle error: ${error.message}")
                })

            stompClient.connect(headers)
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "Connect error: ${e.message}")
        }
    }

    fun sendOffer(targetUserId: String, sdp: String, senderId: String) {
        try {
            val msg = SignalMessage("offer", senderId, targetUserId, sdp, null)
            stompClient.send("/app/call.offer", Gson().toJson(msg))
                .subscribe({}, { error ->
                    android.util.Log.e("SignalingClient", "Send offer error: ${error.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendOffer error: ${e.message}")
        }
    }

    fun sendAnswer(targetUserId: String, sdp: String, senderId: String) {
        try {
            val msg = SignalMessage("answer", senderId, targetUserId, sdp, null)
            stompClient.send("/app/call.answer", Gson().toJson(msg))
                .subscribe({}, { error ->
                    android.util.Log.e("SignalingClient", "Send answer error: ${error.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendAnswer error: ${e.message}")
        }
    }

    fun sendIceCandidate(targetUserId: String, candidate: String, senderId: String) {
        try {
            val msg = SignalMessage("ice-candidate", senderId, targetUserId, null, candidate)
            stompClient.send("/app/call.ice", Gson().toJson(msg))
                .subscribe({}, { error ->
                    android.util.Log.e("SignalingClient", "Send ICE error: ${error.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendIceCandidate error: ${e.message}")
        }
    }

    fun sendHangup(targetUserId: String, senderId: String) {
        try {
            val msg = SignalMessage("hangup", senderId, targetUserId, null, null)
            stompClient.send("/app/call.end", Gson().toJson(msg))
                .subscribe({}, { error ->
                    android.util.Log.e("SignalingClient", "Send hangup error: ${error.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendHangup error: ${e.message}")
        }
    }

    fun sendDecline(targetUserId: String, senderId: String, onComplete: (() -> Unit)? = null) {
        try {
            val msg = SignalMessage("decline", senderId, targetUserId, null, null)
            stompClient.send("/app/call.decline", Gson().toJson(msg))
                .subscribe({
                    onComplete?.invoke()
                }, { error ->
                    android.util.Log.e("SignalingClient", "Send decline error: ${error.message}")
                    onComplete?.invoke()
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendDecline error: ${e.message}")
            onComplete?.invoke()
        }
    }

    fun sendError(targetUserId: String, senderId: String, error: String) {
        try {
            val msg = SignalMessage("error", senderId, targetUserId, null, null, error)
            stompClient.send("/app/call.error", Gson().toJson(msg))
                .subscribe({}, { err ->
                    android.util.Log.e("SignalingClient", "Send error: ${err.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendError error: ${e.message}")
        }
    }

    fun sendGroupJoin(roomId: String, senderId: String) {
        try {
            val msg = SignalMessage("group-join", senderId, "", null, null, null, roomId)
            stompClient.send("/app/group.join", Gson().toJson(msg))
                .subscribe({}, { error ->
                    android.util.Log.e("SignalingClient", "Send group join error: ${error.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendGroupJoin error: ${e.message}")
        }
    }

    fun sendGroupLeave(roomId: String, senderId: String) {
        try {
            val msg = SignalMessage("group-leave", senderId, "", null, null, null, roomId)
            stompClient.send("/app/group.leave", Gson().toJson(msg))
                .subscribe({}, { error ->
                    android.util.Log.e("SignalingClient", "Send group leave error: ${error.message}")
                })
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "sendGroupLeave error: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (stompClient.isConnected) {
                stompClient.disconnect()
            }
        } catch (e: Exception) {
            android.util.Log.e("SignalingClient", "Disconnect error: ${e.message}")
        }
    }
}