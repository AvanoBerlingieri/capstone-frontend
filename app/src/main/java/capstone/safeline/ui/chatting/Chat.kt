package capstone.safeline.ui.chatting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.Home
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager


class Chat : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatScreen(
                onUserClick = { id, name, isGroup ->
                    if (isGroup) {
                        val intent = Intent(this, GroupChatPage::class.java).apply {
                            putExtra("groupId", id) // Pass the raw UUID
                            putExtra("groupName", name)
                        }
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, DmPage::class.java).apply {
                            putExtra("userId", id)
                            putExtra("userName", name)
                        }
                        startActivity(intent)
                    }
                },
                onNavigate = { destination ->
                    val intent = when (destination) {
                        "home" -> Intent(this, Home::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) }
                        "calls" -> Intent(this, Call::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "chats" -> null
                        "profile" -> Intent(this, Profile::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "communities" -> Intent(this, Community::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "contacts" -> Intent(this, Contacts::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        else -> null
                    }
                    intent?.let { startActivity(it) }
                },
                onBack = { finish() }
            )
        }
    }
}

private enum class ChatsTab { ALL, UNREAD, FAVORITES, GROUPS }

@Composable
fun ChatScreen(
    onUserClick: (String, String, Boolean) -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {

    InitializeSocket()

    val context = LocalContext.current
    val messageRepo = remember {
        MessageRepository.getInstance(
            context,
            AppDatabase.getDatabase(context).messageDao()
        )
    }

    // Observe the combined flow
    val allChats by messageRepo.getAllChatsFlow().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(ChatsTab.ALL) }

    // Filter based on tab
    val visibleItems = remember(allChats, selectedTab) {
        when (selectedTab) {
            ChatsTab.GROUPS -> allChats.filterIsInstance<MessageRepository.ChatSummary.Group>()
            ChatsTab.ALL -> allChats
            // Add other filters when read receipts are created
            else -> allChats
        }
    }




    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "chats",
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
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                Image(
                    painter = painterResource(R.drawable.chats_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                ThemeManager.backgroundGradient
                            )
                        )
                )

            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(70.dp)
            ) {

                if (ThemeManager.currentTheme != ThemeManager.Theme.CLASSIC) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    ThemeManager.headerGradient
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(ThemeManager.topBarStroke)
                    )
                }

                StrokeTitle(
                    text = "CHATS",
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 0.dp, end = 12.dp)
                    .clickable {
                        val intent = Intent(context, CreateGroup::class.java)
                        (context as Activity).startActivityForResult(intent, 1001)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter = painterResource(R.drawable.community_servers_add_btn),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .offset(y = 6.dp)
                    )

                    StrokeText(
                        text = "CREATE GROUP",
                        fontFamily = ThemeManager.fontFamily,
                        fontSize = 10.sp,
                        fillColor = Color.White,
                        strokeColor = Color(0xFF002BFF),
                        strokeWidth = 1f
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChatFilters(selectedTab) { selectedTab = it }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visibleItems) { chat ->
                        val isGroup = chat is MessageRepository.ChatSummary.Group
                        ChatRow(
                            user = ChatUser(
                                name = chat.displayName,
                                messages = listOf(Message("Tap to chat", ""))
                            ),
                            hasNew = false,
                            isGroup = isGroup,
                            onClick = {
                                // Pass the actual ID and Name from the ChatSummary object
                                onUserClick(chat.id, chat.displayName, isGroup)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatFilters(
    selected: ChatsTab,
    onSelect: (ChatsTab) -> Unit
) {
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.chats_filters_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterBtn(
                res = R.drawable.chats_all_btn,
                selected = selected == ChatsTab.ALL,
                modifier = Modifier.size(56.dp, 18.dp)
            ) { onSelect(ChatsTab.ALL) }

            FilterBtn(
                res = R.drawable.chats_unread_btn,
                selected = selected == ChatsTab.UNREAD,
                modifier = Modifier.size(56.dp, 18.dp)
            ) { onSelect(ChatsTab.UNREAD) }

            FilterBtn(
                res = R.drawable.chats_favorites_btn,
                selected = selected == ChatsTab.FAVORITES,
                modifier = Modifier.size(70.dp, 18.dp)
            ) { onSelect(ChatsTab.FAVORITES) }

            FilterBtn(
                res = R.drawable.chats_groups_btn,
                selected = selected == ChatsTab.GROUPS,
                modifier = Modifier.size(70.dp, 18.dp)
            ) { onSelect(ChatsTab.GROUPS) }

        }
    }
}

@Composable
private fun FilterBtn(
    res: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier
            .alpha(if (selected) 1f else 0.55f)
            .clickable { onClick() },
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun ChatRow(
    user: ChatUser,
    hasNew: Boolean,
    isGroup: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 393.dp, height = 93.dp)
            .clickable { onClick() }
    ) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.chats_dm_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

        } else {

            val shape = RoundedCornerShape(18.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            ThemeManager.buttonGradient
                        ),
                        shape = shape
                    )
                    .then(
                        (if (isGroup) Color(0xFFB30FFF) else ThemeManager.buttonStroke)?.let {
                            Modifier.border(
                                width = 1.dp,
                                color = it,
                                shape = shape
                            )
                        } ?: Modifier
                    )
            )

        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.chats_icon),
                contentDescription = null,
                modifier = Modifier.size(width = 57.63.dp, height = 50.67.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                StrokeText(
                    text = user.name.substringAfter("|"),
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 24.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF002BFF),
                    strokeWidth = 1f
                )

                ReflectedText(if (isGroup) "Group Chat" else "Last seen: March 10", 9.sp)
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                if (hasNew) {
                    Image(
                        painter = painterResource(R.drawable.new_message_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(bottom = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(50.dp))
                }

                ReflectedText(user.messages.first().time, 9.sp)
            }
        }
    }
}

@Composable
private fun ReflectedText(
    text: String,
    size: TextUnit
) {
    Column {
        StrokeText(
            text = text,
            fontFamily = ThemeManager.fontFamily,
            fontSize = size,
            fillColor = Color.White,
            strokeColor = Color(0xFF002BFF),
            strokeWidth = 1f
        )
        Box(
            modifier = Modifier
                .graphicsLayer { scaleY = -1f }
                .alpha(0.2f)
        ) {
            StrokeText(
                text = text,
                fontFamily = ThemeManager.fontFamily,
                fontSize = size,
                fillColor = Color.White,
                strokeColor = Color(0xFFB30FFF),
                strokeWidth = 1f
            )
        }
    }
}
