package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class CommunityServers : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommunityServersScreen(
                onBackToMessages = { startActivity(Intent(this, Chat::class.java)) },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> {}
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                }
            )
        }
    }
}

@Composable
private fun CommunityServersScreen(
    onBackToMessages: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val servers = listOf("A", "B", "C", "D")
    var selectedServer by remember { mutableStateOf("A") }
    var textOpen by remember { mutableStateOf(false) }
    var voiceOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "communities",
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
                painter = painterResource(R.drawable.community_servers_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Row(modifier = Modifier.fillMaxSize()) {
                LeftServersPanel(
                    servers = servers,
                    selectedServer = selectedServer,
                    onSelectServer = { selectedServer = it },
                    onBackToMessages = onBackToMessages
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 22.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StrokeText(
                        text = "SERVER $selectedServer",
                        fontFamily = Vampiro,
                        fontSize = 32.sp,
                        fillColor = Color.White,
                        strokeColors = listOf(Color(0xFF0DA2FF), Color(0xFFEA00FF)),
                        strokeWidth = 3f,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        SpaceHeader(
                            text = "TEXT SPACES",
                            onClick = { textOpen = !textOpen }
                        )

                        if (textOpen) {
                            OverlapChannels(
                                channels = listOf("CHANNEL 1", "CHANNEL 2", "CHANNEL 3", "CHANNEL 4"),
                                startX = 0.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SpaceHeader(
                            text = "VOICE SPACES",
                            onClick = { voiceOpen = !voiceOpen }
                        )

                        if (voiceOpen) {
                            OverlapChannels(
                                channels = listOf("CHANNEL 1", "CHANNEL 2", "CHANNEL 3"),
                                startX = 0.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeftServersPanel(
    servers: List<String>,
    selectedServer: String,
    onSelectServer: (String) -> Unit,
    onBackToMessages: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(75.dp)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.community_servers_leftside_bg),
            contentDescription = null,
            modifier = Modifier
                .width(75.dp)
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(75.dp)
                .height(57.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.community_servers_backtomessages_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Image(
                painter = painterResource(R.drawable.community_servers_backtomessages_btn),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 38.dp, height = 36.dp)
                    .clickable { onBackToMessages() },
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 76.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            servers.forEachIndexed { index, letter ->
                if (index != 0) Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(width = 84.dp, height = 46.dp)
                        .clickable { onSelectServer(letter) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.community_servers_icon_btn),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    StrokeText(
                        text = "SERVER\n$letter",
                        fontFamily = Vampiro,
                        fontSize = 12.sp,
                        fillColor = Color.White,
                        strokeColor = Color(0xFF0251C7),
                        strokeWidth = 1f,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaceHeader(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(297.dp)
            .height(57.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(R.drawable.community_servers_voice_text_space),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        StrokeText(
            text = text,
            fontFamily = Vampiro,
            fontSize = 24.sp,
            fillColor = Color.White,
            strokeColor = Color(0xFF193DEF),
            strokeWidth = 1f,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

@Composable
private fun OverlapChannels(
    channels: List<String>,
    startX: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .width(297.dp)
    ) {
        channels.forEachIndexed { index, title ->
            val x = startX + (20.dp * index)
            val y = (39.dp * index)

            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(width = 214.dp, height = 44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    painter = painterResource(R.drawable.community_servers_channel),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                StrokeText(
                    text = title,
                    fontFamily = Vampiro,
                    fontSize = 16.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF193DEF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height((39.dp * (channels.size - 1)) + 44.dp))
    }
}


