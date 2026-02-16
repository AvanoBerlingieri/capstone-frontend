package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.models.ChatUser
import capstone.safeline.models.Message
import capstone.safeline.ui.components.BottomNavBar
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.style.TextAlign

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class Chat : ComponentActivity() {

    private val chatUsers = listOf(
        ChatUser("Friend 1", messages = listOf(Message("Hi", "12:49 PM"))),
        ChatUser("Friend 2", messages = listOf(Message("Yo", "12:30 PM"))),
        ChatUser("Friend 3", messages = listOf(Message("Hello", "11:58 AM"))),
        ChatUser("Friend 4", messages = listOf(Message("Hey", "10:12 AM"))),
        ChatUser("Friend 5", messages = listOf(Message("Sup", "9:40 AM"))),
        ChatUser("Friend 6", messages = listOf(Message("Ping", "8:15 AM")))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatScreen(
                chatUsers = chatUsers,
                onUserClick = { user ->
                    val intent = Intent(this, UserChat::class.java)
                    intent.putExtra("userName", user.name)
                    startActivity(intent)
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> {}
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                },
                onBack = { startActivity(Intent(this, Home::class.java)) }
            )
        }
    }
}

private enum class ChatsTab { ALL, UNREAD, FAVORITES, GROUPS }

@Composable
fun ChatScreen(
    chatUsers: List<ChatUser>,
    onUserClick: (ChatUser) -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ChatsTab.ALL) }

    val mapped = chatUsers.mapIndexed { index, user ->
        user to (index == 0)
    }

    val visible = when (selectedTab) {
        ChatsTab.UNREAD -> mapped.filter { it.second }
        else -> mapped
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
            Image(
                painter = painterResource(R.drawable.chats_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            HomeTitle(
                text = "CHATS",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
            )

            Image(
                painter = painterResource(R.drawable.back_btn),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 14.dp)
                    .size(width = 78.55.dp, height = 36.45.dp)
                    .clickable { onBack() }
            )


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
                    items(visible) { (user, hasNew) ->
                        ChatRow(
                            user = user,
                            hasNew = hasNew,
                            onClick = { onUserClick(user) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(90.dp)) }
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 393.dp, height = 93.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(R.drawable.chats_dm_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

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
                StrokeText(user.name, 24.sp, Color(0xFF002BFF))

                ReflectedText("Last seen: March 10", 9.sp)
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
private fun HomeTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val strokeBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF002BFF), Color(0xFFB30FFF))
    )

    Box(modifier = modifier) {
        Text(
            text = text,
            fontFamily = Vampiro,
            fontSize = 28.sp,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    blurRadius = 6f
                )
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = text,
            fontFamily = Vampiro,
            fontSize = 28.sp,
            color = Color.Transparent,
            style = TextStyle(
                brush = strokeBrush,
                drawStyle = Stroke(width = 4f)
            ),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun StrokeText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit,
    strokeColor: Color
) {
    Box {
        Text(
            text = text,
            fontFamily = Vampiro,
            fontSize = size,
            color = Color.White
        )
        Text(
            text = text,
            fontFamily = Vampiro,
            fontSize = size,
            color = Color.Transparent,
            style = TextStyle(
                brush = Brush.linearGradient(listOf(strokeColor, strokeColor)),
                drawStyle = Stroke(1f)
            )
        )
    }
}

@Composable
private fun ReflectedText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit
) {
    Column {
        StrokeText(text, size, Color(0xFF002BFF))
        Box(
            modifier = Modifier
                .graphicsLayer { scaleY = -1f }
                .alpha(0.2f)
        ) {
            StrokeText(text, size, Color(0xFFB30FFF))
        }
    }
}

