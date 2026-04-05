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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.dto.messaging.IncomingGroupMessage
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.local.entity.GroupMessageEntity
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import java.util.UUID

class GroupChatPage : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""
        val groupName = intent.getStringExtra("groupName") ?: "Group Chat"

        setContent {
            val context = LocalContext.current
            val messageRepo = remember {
                MessageRepository.getInstance(
                    context,
                    AppDatabase.getDatabase(context).messageDao()
                )
            }
            val wsManager = WebSocketManager.getInstance()

            // Observe Repository Flow
            val chatMessages by messageRepo.getGroupChatMessagesFlow(groupId)
                .collectAsState(initial = emptyList())

            // sort handles ISO strings better
            val sortedMessages = remember(chatMessages) {
                chatMessages.sortedBy { it.timestamp }
            }

            // Sync history on entry
            LaunchedEffect(groupId) {
                messageRepo.fetchGroupHistory(groupId)
            }

            GroupChatScreen(
                groupId = groupId,
                groupName = groupName,
                messages = sortedMessages,
                onBack = { finish() },
                onSendMessage = { text ->
                    wsManager.sendGroupMessage(
                        IncomingGroupMessage(
                            messageId = UUID.randomUUID().toString(),
                            groupId = groupId,
                            content = text
                        )
                    )
                },
                onSettings = {
                    val intent = Intent(this, GroupSettingsPage::class.java).apply {
                        putExtra("groupId", groupId)
                        putExtra("groupName", groupName)
                    }
                    startActivity(intent)
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GroupChatScreen(
    groupId: String,
    groupName: String,
    messages: List<GroupMessageEntity>,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LocalContext.current

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painterResource(R.drawable.dm_background),
                    null,
                    Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(ThemeManager.backgroundGradient))
                )
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .imePadding()) {
                GroupHeader(groupName, onBack, onSettings)

                LazyColumn(
                    state = listState,
                    reverseLayout = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
                ) {
                    items(messages, key = { it.messageId }) { msg ->
                        MessageBubble(message = msg.content, isMine = msg.isMine)
                    }
                }

                DmInputBar(
                    value = text,
                    onValueChange = { text = it },
                    onSend = {
                        if (text.isNotBlank()) {
                            onSendMessage(text)
                            text = ""
                        }
                    },
                    onAttach = {}
                )
            }
        }
    }
}

@Composable
fun GroupHeader(username: String, onBack: () -> Unit, onSettings: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(top = 10.dp)) {
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
                    .background(Brush.horizontalGradient(ThemeManager.headerGradient))
            )
        }
        Image(
            painter = painterResource(R.drawable.back_for_dm),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 18.dp)
                .size(width = 83.dp, height = 31.dp)
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
                painter = painterResource(R.drawable.group_settings),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 39.dp, height = 31.dp)
                    .clickable { onSettings() },
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
        }
    }
}