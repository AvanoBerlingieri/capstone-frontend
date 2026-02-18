package capstone.safeline.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import capstone.safeline.R
import androidx.compose.ui.graphics.graphicsLayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.Global
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.components.BottomNavBar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DmPage : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("userName") ?: "Unknown"

        val global = Global()
        val usersList = global.loadUsersChats(this)
        val user = usersList.firstOrNull { it.name == username }

        setContent {
            DmPageScreen(
                onNavigate = {},
                username = username,
                user = user,
                onBack = { finish() },
                onCall = {
                    val intent = Intent(this, CallingPage::class.java)
                    intent.putExtra("userName", username)
                    startActivity(intent)
                }
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DmPageScreen(
    onNavigate: (String) -> Unit,
    username: String,
    user: ChatUser?,
    onBack: () -> Unit,
    onCall: () -> Unit
)
 {
    val jsonMessages: List<Message> = user?.messages ?: emptyList()
    var chatMessages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }
    var nextJsonIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {},
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
                .padding(innerPadding)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.dm_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize()
            )
            {
                // Custom DM top header like your mock
                DmHeader(
                    username = username,
                    lastSeen = "Last seen: March 10",
                    onBack = onBack,
                    onCall = onCall
                )

                // Messages list (scrollable)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(chatMessages) { msg ->
                        // Simple: alternate like you were doing.
                        // Better: add msg.isMine boolean in your model.
                        val isMine = (chatMessages.indexOf(msg) % 2 == 0)
                        MessageBubble(
                            message = msg.text,
                            isMine = isMine
                        )
                    }
                }

                // Input area like your mock
                DmInputBar(
                    value = text,
                    onValueChange = { text = it },
                    onAttach = { /* TODO */ },
                    onSend = {
                        if (text.isBlank()) return@DmInputBar

                        val currentTime = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("hh:mm a"))


                        chatMessages = chatMessages + Message(text, currentTime)
                        text = ""

                        if (nextJsonIndex < jsonMessages.size) {
                            chatMessages = chatMessages + jsonMessages[nextJsonIndex]
                            nextJsonIndex++
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DmHeader(
    username: String,
    lastSeen: String,
    onBack: () -> Unit,
    onCall: () -> Unit
)
 {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        // Nameplate background
        Image(
            painter = painterResource(R.drawable.friend_nameplate_background),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(90.dp),
            contentScale = ContentScale.FillBounds
        )

        // Back button
        Image(
            painter = painterResource(R.drawable.back_for_dm),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 18.dp)
                .size(width = 90.dp, height = 38.dp)
                .clickable { onBack() },
            contentScale = ContentScale.FillBounds
        )

        // Right icons (avatar + call)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 14.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.avatar),
                contentDescription = "Avatar",
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(R.drawable.call_for_dm),
                contentDescription = "Call",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onCall() },
                contentScale = ContentScale.Fit
            )
        }

        // Username + last seen text (centered)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = username,
                fontSize = 26.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastSeen,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: String,
    isMine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
        ) {
            // Bubble background
            Image(
                painter = painterResource(R.drawable.message_bubble_background),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        // flip for outgoing so it "points" the other way
                        scaleX = if (isMine) -1f else 1f
                    },
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
            )
        }
    }
}

@Composable
private fun DmInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .height(74.dp)
    ) {
        // whole bar background
        Image(
            painter = painterResource(R.drawable.input_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // input box background (purely visual)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.input_box),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // transparent TextField on top of your PNG
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.55f)) },
                    singleLine = true,
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Image(
                painter = painterResource(R.drawable.attach),
                contentDescription = "Attach",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onAttach() },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(10.dp))

            Image(
                painter = painterResource(R.drawable.enter_button),
                contentDescription = "Send",
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onSend() },
                contentScale = ContentScale.Fit
            )
        }
    }
}


