package capstone.safeline.models


data class Message(
    val text: String,
    val time: String
)

data class ChatUser(
    val name: String,
    val messages: List<Message>
)