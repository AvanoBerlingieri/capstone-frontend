package capstone.safeline.ui.chatting

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.ui.calling.CallingPage
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import java.util.UUID

class DmPage : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("userName") ?: "Unknown"
        val lastSeen = intent.getStringExtra("lastSeen") ?: "Last seen recently"

        setContent {
            DmPageScreen(
                username = username,
                lastSeen = lastSeen,
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
    username: String,
    lastSeen: String,
    onBack: () -> Unit,
    onCall: () -> Unit
) {
    InitializeSocket()

    val context = LocalContext.current
    val authRepo = remember { AuthRepository.getInstance(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    val messageDao = database.messageDao()
    val ws = WebSocketManager.getInstance()

    // Real-time connection observation
    val isConnected by ws.connectionStatus.collectAsState()

    var partnerUserId by remember { mutableStateOf<String?>(null) }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(username) {
        authRepo.getIdByUsername(username).onSuccess { id -> partnerUserId = id }
    }

    val chatMessages by if (partnerUserId != null) {
        messageDao.getMessagesForUser(partnerUserId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val sortedMessages = remember(chatMessages) {
        chatMessages.sortedWith(
            compareByDescending<capstone.safeline.data.local.entity.MessageEntity> { it.timestamp }
                .thenByDescending { it.messageId }
        )
    }

    LaunchedEffect(sortedMessages.size) {
        if (sortedMessages.isNotEmpty()) listState.animateScrollToItem(0)
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(painter = painterResource(id = R.drawable.dm_background), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(ThemeManager.backgroundGradient)))
            }

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
                DmHeader(username, lastSeen, onBack, onCall)

                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
                ) {
                    items(items = sortedMessages, key = { it.messageId }) { entity ->
                        MessageBubble(message = entity.content, isMine = entity.isMine)
                    }
                }

                DmInputBar(
                    value = text,
                    onValueChange = { text = it },
                    isConnected = isConnected,
                    onSend = {
                        val body = text.trim()
                        if (body.isNotEmpty() && partnerUserId != null) {
                            ws.sendPrivateMessage(IncomingMessage(messageId = UUID.randomUUID().toString(), receiver = partnerUserId!!, content = body))
                            text = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DmInputBar(value: String, onValueChange: (String) -> Unit, isConnected: Boolean, onSend: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(74.dp)) {
        Image(painter = painterResource(R.drawable.input_background), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).height(46.dp)) {
                Image(painter = painterResource(R.drawable.input_box), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.55f)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Image(painter = painterResource(R.drawable.attach), contentDescription = null, modifier = Modifier.size(34.dp), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.width(10.dp))

            if (isConnected) {
                Image(
                    painter = painterResource(R.drawable.enter_button),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp).clickable { onSend() },
                    contentScale = ContentScale.Fit
                )
            } else {
                // Show white spinner inside the input bar during the 7s delay
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: String, isMine: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Box(modifier = Modifier.widthIn(max = 280.dp)) {
            Image(painter = painterResource(R.drawable.message_bubble_background), contentDescription = null, modifier = Modifier.matchParentSize().graphicsLayer { scaleX = if (isMine) -1f else 1f }, contentScale = ContentScale.FillBounds)
            Text(text = message, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp))
        }
    }
}

@Composable
private fun DmHeader(username: String, lastSeen: String, onBack: () -> Unit, onCall: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp)) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
            Image(painter = painterResource(R.drawable.friend_nameplate_background), contentDescription = null, modifier = Modifier.align(Alignment.TopCenter).width(412.dp).height(69.dp), contentScale = ContentScale.FillBounds)
        } else {
            Box(modifier = Modifier.align(Alignment.TopCenter).width(412.dp).height(69.dp).background(Brush.horizontalGradient(ThemeManager.headerGradient)))
        }
        Image(painter = painterResource(R.drawable.back_for_dm), contentDescription = null, modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 18.dp).size(width = 83.dp, height = 31.dp).clickable { onBack() }, contentScale = ContentScale.FillBounds)
        Row(modifier = Modifier.align(Alignment.TopEnd).padding(end = 14.dp, top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(R.drawable.avatar), contentDescription = null, modifier = Modifier.size(width = 39.dp, height = 31.dp), contentScale = ContentScale.Crop)
            Image(painter = painterResource(R.drawable.call_for_dm), contentDescription = null, modifier = Modifier.size(width = 73.dp, height = 46.dp).clickable { onCall() }, contentScale = ContentScale.Fit)
        }
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            StrokeText(text = username, fontFamily = ThemeManager.fontFamily, fontSize = 24.sp, fillColor = Color.White, strokeColor = ThemeManager.titleStroke, strokeWidth = 1f, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            StrokeText(text = lastSeen, fontFamily = ThemeManager.fontFamily, fontSize = 12.sp, fillColor = Color.White, strokeColor = ThemeManager.titleStroke, strokeWidth = 1f, textAlign = TextAlign.Center)
        }
    }
}