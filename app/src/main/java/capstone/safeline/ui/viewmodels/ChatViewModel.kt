package capstone.safeline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import capstone.safeline.data.local.entity.MessageEntity
import capstone.safeline.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

// Define the UI State
data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = true
)

// The ViewModel
class ChatViewModel(
    private val repository: MessageRepository,
    private val currentUserId: String, // Your ID
    private val receiverId: String     // Friend's ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            // Collect the Flow from Room. Every time the DB changes, this triggers!
            repository.getConversation(receiverId)
                .catch { e ->
                    // Handle errors (e.g., log them)
                    println("Error loading messages: ${e.message}")
                }
                .collect { messageList ->
                    _uiState.value = _uiState.value.copy(
                        messages = messageList,
                        isLoading = false
                    )
                }
        }
    }

    fun updateInputText(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun sendMessage() {
        val currentText = _uiState.value.inputText
        if (currentText.isBlank()) return

        // Clear the input box immediately for a snappy UI
        _uiState.value = _uiState.value.copy(inputText = "")

        viewModelScope.launch {
            // Create the real entity using backend-compatible data
            val newMessage = MessageEntity(
                messageUuid = UUID.randomUUID().toString(),
                senderId = currentUserId,
                receiverId = receiverId,
                content = currentText.trim(),
                timestamp = System.currentTimeMillis(), // Maps to Instant later
                status = "PENDING"
            )

            // Save locally and trigger network send
            repository.sendMessage(newMessage)
        }
    }
}

// Factory to create the ViewModel with arguments
class ChatViewModelFactory(
    private val repository: MessageRepository,
    private val currentUserId: String,
    private val receiverId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, currentUserId, receiverId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}