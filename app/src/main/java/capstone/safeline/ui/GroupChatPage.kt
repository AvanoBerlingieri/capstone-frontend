package capstone.safeline.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
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
import capstone.safeline.models.Message
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GroupChatPage : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""
        val group = CommunityData.groupChats.find { it.id == groupId }
        val currentName = group?.name ?: ""

        setContent {
            GroupChatScreen(
                name = groupId,
                onBack = { finish() }
            )
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GroupChatScreen(
    name: String,
    onBack: () -> Unit
) {
    val group = CommunityData.groupChats.find { it.id == name }
    if (group == null) {
        val context = LocalContext.current
        (context as Activity).finish()
        return
    }
    val currentName by group.name

    val context = LocalContext.current

    var chatMessages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(R.drawable.dm_background),
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
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

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 14.dp, top = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.group_settings),
                            contentDescription = null,
                            modifier = Modifier
                                .size(46.dp)
                                .clickable {
                                    val intent = Intent(context, GroupSettingsPage::class.java)
                                    intent.putExtra("groupId", group?.id)
                                    context.startActivity(intent)
                                },
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
                            text = currentName,
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 24.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFFB30FFF),
                            strokeWidth = 1f,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        StrokeText(
                            text = "Group Chat",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 12.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFFB30FFF),
                            strokeWidth = 1f,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(chatMessages.withIndex().toList()) { indexed ->
                        val msg = indexed.value
                        val isMine = indexed.index % 2 == 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
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

                                Text(
                                    text = msg.text,
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
                }

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
                                value = text,
                                onValueChange = { text = it },
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
                            modifier = Modifier.size(34.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Image(
                            painter = painterResource(R.drawable.enter_button),
                            contentDescription = null,
                            modifier = Modifier
                                .size(46.dp)
                                .clickable {
                                    if (text.isBlank()) return@clickable

                                    val currentTime = LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("hh:mm a"))

                                    chatMessages = chatMessages + Message(text, currentTime)
                                    text = ""
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }

}
