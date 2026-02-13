package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.ChatCard
import capstone.safeline.ui.components.TopBar

class Chat : ComponentActivity() {

    private val chatUsers = listOf(
        ChatUser("John Doe", messages = listOf(Message("Hey, how are you?", "12:30 AM"))),
        ChatUser("Jane Smith", messages = listOf(Message("Meeting at 2 PM", "1:00 PM"))),
        ChatUser("Mike Lee", messages = listOf(Message("Call me back!", "10:00 AM"))),
        ChatUser("Jeff", messages = listOf(Message("I might be too cool", "2:00 AM"))),
        ChatUser("Savs", messages = listOf(Message("Gymnastics is at 3", "9:00 AM"))),
        ChatUser("Avano", messages = listOf(Message("Dude we are cooked!!!", "11:59 PM")))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatScreen(
                chatUsers = chatUsers,
                onSendClick = {
                    // TODO
                },
                onUserClick = { user ->
                    val intent = Intent(this, UserChat::class.java)
                    intent.putExtra("userName", user.name)
                    startActivity(intent)
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "messages" -> {}
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                    }
                }
            )
        }
    }
}

@Composable
fun ChatScreen(
    chatUsers: List<ChatUser>,
    onSendClick: () -> Unit,
    onUserClick: (ChatUser) -> Unit,
    onNavigate: (String) -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B0014), Color(0xFF0D2244))
    )

    Scaffold(
        topBar = { TopBar(title = "Chats") },
        bottomBar = {
            BottomNavBar(
                currentScreen = "messages",
                onNavigate = onNavigate
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(chatUsers) { user ->
                        ChatCard(user = user, onClick = onUserClick)
                    }
                }
            }
        }
    }
}
