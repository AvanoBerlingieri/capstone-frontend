package capstone.safeline.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import capstone.safeline.Global
import capstone.safeline.R
import capstone.safeline.apis.dto.messaging.IncomingMessage
import capstone.safeline.apis.network.ApiClientAuth
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DmPage : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("userName") ?: "Unknown"
        val lastSeen = intent.getStringExtra("lastSeen") ?: "Last seen: March 10"

        val global = Global()
        val usersList = global.loadUsersChats(this)
        val user = usersList.firstOrNull { it.name == username }

        setContent {
            DmPageScreen(
                onNavigate = {},
                username = username,
                lastSeen = lastSeen,
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
    lastSeen: String,
    user: ChatUser?,
    onBack: () -> Unit,
    onCall: () -> Unit
) {
    val context = LocalContext.current
    val dsManager = remember { DataStoreManager(context, CryptoManager()) }
    val authRepo = remember { AuthRepository(dsManager, ApiClientAuth.provideApiService(context, dsManager)) }
    val scope = rememberCoroutineScope()
    val ws = remember { WebSocketManager.getInstance() }

    var partnerUserId by remember { mutableStateOf<String?>(null) }
    var text by remember { mutableStateOf("") }

    // Initialize chatRows with the user's existing history
    val chatRows = remember(user) {
        mutableStateListOf<Pair<Message, Boolean>>().apply {
            user?.messages?.forEach { add(it to false) }
        }
    }

    // 1. Fetch the Partner ID from the username
    LaunchedEffect(username) {
        authRepo.getIdByUsername(username).onSuccess { id ->
            partnerUserId = id
        }
    }

    DisposableEffect(partnerUserId) {
        val currentPartnerId = partnerUserId ?: return@DisposableEffect onDispose {}

        val listener: (String) -> Unit = listener@{ rawPayload ->
            try {
                val payload = rawPayload.trim().removeSurrounding("\"").replace("\\\"", "\"")
                val obj = JsonParser.parseString(payload).asJsonObject

                val messageContent = obj.get("content")?.asString ?: return@listener
                val sender = obj.get("sender")?.asString ?: return@listener
                val mid = obj.get("messageId")?.asString

                mid?.let { ws.acknowledgeMessage(it) }

                if (sender.equals(currentPartnerId, ignoreCase = true)) {
                    scope.launch(Dispatchers.Main.immediate) {
                        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))

                        chatRows.add(Message(messageContent, time) to false)

                        Log.d("DM_UPDATE", "Successfully displayed message: $messageContent")
                    }
                } else {
                    Log.d("DM_UPDATE", "Message ignored: Sender $sender does not match partner $currentPartnerId")
                }
            } catch (e: Exception) {
                Log.e("STOMP_UI", "Failed to parse: $rawPayload", e)
            }
        }

        ws.onPrivateMessagePayload = listener

        onDispose {
            if (ws.onPrivateMessagePayload == listener) {
                ws.onPrivateMessagePayload = null
            }
        }
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Logic
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(id = R.drawable.dm_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(ThemeManager.backgroundGradient)))
            }

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
                DmHeader(username, lastSeen, onBack, onCall)

                // 3. Using itemsIndexed to avoid key collision crashes
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    itemsIndexed(chatRows) { _, (msg, isMine) ->
                        MessageBubble(message = msg.text, isMine = isMine)
                    }
                }

                DmInputBar(
                    value = text,
                    onValueChange = { text = it },
                    onAttach = {},
                    onSend = {
                        if (text.isBlank()) return@DmInputBar
                        val body = text.trim()
                        val rid = partnerUserId

                        if (rid != null) {
                            ws.sendPrivateMessage(IncomingMessage(rid, body))
                            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
                            chatRows.add(Message(body, currentTime) to true)
                            text = ""
                        } else {
                            Log.e("DM_SEND", "Cannot send message: Partner ID is null")
                        }
                    }
                )
            }
        }
    }
}

// UI Components (Header, Bubble, Input) remain as they were...
@Composable
private fun DmHeader(username: String, lastSeen: String, onBack: () -> Unit, onCall: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp)) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
            Image(
                painter = painterResource(R.drawable.friend_nameplate_background),
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopCenter).width(412.dp).height(69.dp),
                contentScale = ContentScale.FillBounds
            )
        } else {
            Box(modifier = Modifier.align(Alignment.TopCenter).width(412.dp).height(69.dp).background(Brush.horizontalGradient(ThemeManager.headerGradient)))
        }

        Image(
            painter = painterResource(R.drawable.back_for_dm),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 18.dp).size(width = 83.dp, height = 31.dp).clickable { onBack() },
            contentScale = ContentScale.FillBounds
        )

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

@Composable
private fun MessageBubble(message: String, isMine: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Box(modifier = Modifier.widthIn(max = 280.dp)) {
            Image(painter = painterResource(R.drawable.message_bubble_background), contentDescription = null, modifier = Modifier.matchParentSize().graphicsLayer { scaleX = if (isMine) -1f else 1f }, contentScale = ContentScale.FillBounds)
            Text(text = message, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp))
        }
    }
}

@Composable
private fun DmInputBar(value: String, onValueChange: (String) -> Unit, onAttach: () -> Unit, onSend: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(74.dp)) {
        Image(painter = painterResource(R.drawable.input_background), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).height(46.dp)) {
                Image(painter = painterResource(R.drawable.input_box), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
                TextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), placeholder = { Text("Type a message...", color = Color.White.copy(alpha = 0.55f)) }, singleLine = true, colors = androidx.compose.material3.TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Image(painter = painterResource(R.drawable.attach), contentDescription = null, modifier = Modifier.size(34.dp).clickable { onAttach() }, contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.width(10.dp))
            Image(painter = painterResource(R.drawable.enter_button), contentDescription = null, modifier = Modifier.size(46.dp).clickable { onSend() }, contentScale = ContentScale.Fit)
        }
    }
}