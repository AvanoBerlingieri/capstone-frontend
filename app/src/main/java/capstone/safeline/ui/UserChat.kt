package capstone.safeline.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.Global
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.TopBar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserChat : ComponentActivity(){
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("userName") ?: "Unknown"

        val global = Global()

        val usersList = global.loadUsersChats(this)
        val user = usersList.firstOrNull { it.name == userName }
        setContent {
            UserChatScreen(
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "messages" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> {}
                    }
                },
                userName, user

            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserChatScreen(onNavigate: (String) -> Unit, username: String, user: ChatUser?) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B0014),
            Color(0xFF0D2244)
        )
    )

    val jsonMessages: List<Message> = user?.messages ?: emptyList()
    var chatMessages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }
    var nextJsonIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { TopBar(title = username) },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                chatMessages.forEachIndexed { index, msg ->
                    if (index % 2 != 0) {
                        IncomingMessageCard(msg.text, msg.time)
                    } else {
                        UserMessage(msg.text, msg.time)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Type a message", color = Color.Gray) },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            val currentTime = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("hh:mm a"))
                            val userMsg = Message(text, currentTime)
                            chatMessages = chatMessages + userMsg
                            text = ""

                            if (nextJsonIndex < jsonMessages.size) {
                                chatMessages = chatMessages + jsonMessages[nextJsonIndex]
                                nextJsonIndex++
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D5AFE))
                ) {
                    Text("Send", color = Color.White)
                }
            }
        }
    }
}




@Composable
fun IncomingMessageCard(
    message: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Color(0xFF3E3E3E),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun UserMessage(
    message: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Color(0xFF3D5AFE),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}

