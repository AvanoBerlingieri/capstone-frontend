package capstone.safeline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import capstone.safeline.data.local.entity.MessageEntity
import capstone.safeline.data.repository.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ChatListViewModel(repository: MessageRepository) : ViewModel() {

    // This pulls all recent messages from the DB and keeps them updated
    val recentMessages: StateFlow<List<MessageEntity>> = repository.getAllRecentMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

class ChatListViewModelFactory(private val repository: MessageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}