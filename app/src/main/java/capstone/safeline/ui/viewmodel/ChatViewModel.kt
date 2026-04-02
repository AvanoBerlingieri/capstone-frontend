package capstone.safeline.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.entity.MessageEntity
import capstone.safeline.data.repository.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class ChatViewModel(
    private val repository: MessageRepository,
    private val currentUserId: String,
    private val contactId: String // The person you are texting
) : ViewModel() {

    // 1. THE DATA STREAM: Automatically reads the DB and exposes it to Compose
    val messages: StateFlow<List<MessageEntity>> = repository.getConversation(contactId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        // 1. Create the DTO that matches the backend Record
        val networkMessage = IncomingMessage(
            receiver = UUID.fromString(contactId),
            content = content
        )

        // 2. Send it over the wire
        WebSocketManager.getInstance().sendPrivateMessage(networkMessage)

        val localEntity = MessageEntity(
            // id will default to 0 and auto-generate
            messageUuid = UUID.randomUUID().toString(),
            senderId = currentUserId,
            receiverId = contactId,
            content = content,
            timestamp = System.currentTimeMillis(),
            status = "SENT"
        )

        // Launch a coroutine to save to local DB so the UI updates
        viewModelScope.launch {
            repository.saveMessageLocally(localEntity)
        }
    }
}

// =====================================================================
// THE FACTORY
// Because our ViewModel requires arguments (repository, ids), we cannot
// just use standard Compose viewModel() injection. We need a Factory!
// =====================================================================
class ChatViewModelFactory(
    private val repository: MessageRepository,
    private val currentUserId: String,
    private val contactId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, currentUserId, contactId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}