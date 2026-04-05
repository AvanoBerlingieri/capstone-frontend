package capstone.safeline.models

data class CallRecord(
    val id: Long? = null,
    val callerId: String,
    val receiverId: String,
    val status: String,      // "completed", "declined", "missed", "failed"
    val startTime: String?,
    val endTime: String?,
    val duration: Long?
)