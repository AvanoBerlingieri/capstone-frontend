package capstone.safeline.ui

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import capstone.safeline.ui.viewmodels.ChatViewModel
import capstone.safeline.ui.viewmodels.ChatViewModelFactory
import capstone.safeline.data.local.entity.MessageEntity
import java.time.Instant
import java.time.ZoneId
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.Global
import capstone.safeline.R
import capstone.safeline.SafelineApplication
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



class DmPage : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("userName") ?: "Unknown"
        val lastSeen = intent.getStringExtra("lastSeen") ?: "Last seen: Just now"

        // The friend's ID passed from Chat.kt
        val friendId = intent.getStringExtra("friendId") ?: "unknown_id"

        val app = applicationContext as SafelineApplication
        val repository = app.messageRepository

        setContent {
            // 1. Read the REAL User ID from the DataStore!
            val myUserId by app.dataStoreManager.userIdFlow.collectAsState(initial = "")

            // 2. Only draw the screen once we have the ID
            if (myUserId.isNotEmpty()) {

                // Pass BOTH real IDs into the ViewModel
                val viewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(
                        repository = repository,
                        currentUserId = myUserId,
                        receiverId = friendId
                    )
                )

                DmPageScreen(
                    onNavigate = { /* Add navigation logic if needed */ },
                    username = username,
                    lastSeen = lastSeen,
                    user = null,
                    onBack = { finish() },
                    onCall = {
                        val intent = Intent(this@DmPage, CallingPage::class.java)
                        intent.putExtra("userName", username)
                        startActivity(intent)
                    },
                    viewModel = viewModel
                )
            } else {
                // Loading state
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DmPageScreen(
    onNavigate: (String) -> Unit,
    username: String,
    lastSeen: String,
    user: ChatUser?,
    onBack: () -> Unit,
    onCall: () -> Unit,
    viewModel: ChatViewModel // Inject ViewModel here
) {
    // Collect the UI state reactively from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {},
        bottomBar = {},
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(id = R.drawable.dm_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(ThemeManager.backgroundGradient))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                DmHeader(
                    username = username,
                    lastSeen = lastSeen,
                    onBack = onBack,
                    onCall = onCall
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
                    reverseLayout = true // Reverses so newest messages are at the bottom
                ) {
                    // Use the REAL messages from the database
                    items(uiState.messages.reversed()) { messageEntity ->
                        // Determine if it's mine based on senderId (you'll need your actual user ID here eventually)
                        // For now, let's assume if it has a PENDING/SENT status, it's yours
                        val isMine = messageEntity.status != "RECEIVED"

                        // Format the timestamp
                        val timeString = Instant.ofEpochMilli(messageEntity.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("hh:mm a"))

                        MessageBubble(
                            message = messageEntity.content,
                            isMine = isMine,
                            time = timeString // Optional: Update MessageBubble to display time
                        )
                    }
                }

                DmInputBar(
                    value = uiState.inputText,
                    onValueChange = { viewModel.updateInputText(it) }, // Send text to ViewModel
                    onAttach = {},
                    onSend = { viewModel.sendMessage() } // Trigger ViewModel send
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
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp)
    ) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.friend_nameplate_background),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(412.dp)
                    .height(69.dp),
                contentScale = ContentScale.FillBounds
            )

        } else {

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(412.dp)
                    .height(69.dp)
                    .background(
                        Brush.horizontalGradient(
                            ThemeManager.headerGradient
                        )
                    )
            )

        }

        Image(
            painter = painterResource(R.drawable.back_for_dm),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 18.dp)
                .size(width = 83.09.dp, height = 31.49.dp)
                .clickable { onBack() },
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 14.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.avatar),
                contentDescription = null,
                modifier = Modifier.size(width = 39.54.dp, height = 31.81.dp),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(R.drawable.call_for_dm),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 73.dp, height = 46.dp)
                    .clickable { onCall() },
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StrokeText(
                text = username,
                fontFamily = ThemeManager.fontFamily,
                fontSize = 24.sp,
                fillColor = Color.White,
                strokeColor = ThemeManager.titleStroke,
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            StrokeText(
                text = lastSeen,
                fontFamily = ThemeManager.fontFamily,
                fontSize = 12.sp,
                fillColor = Color.White,
                strokeColor = ThemeManager.titleStroke,
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: String,
    isMine: Boolean,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // This is your background bubble image
            Image(
                painter = painterResource(R.drawable.message_bubble_background),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = if (isMine) -1f else 1f
                    },
                contentScale = ContentScale.FillBounds
            )

            // We use a Column now to stack the message and the time
            Column(
                modifier = Modifier.padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // The timestamp display
                Text(
                    text = time,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
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
            .height(74.dp)
    ) {
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

                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    placeholder = {
                        Text(
                            "Type a message...",
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    },
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
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onAttach() },
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(10.dp))

            Image(
                painter = painterResource(R.drawable.enter_button),
                contentDescription = null,
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onSend() },
                contentScale = ContentScale.Fit
            )
        }
    }
}





