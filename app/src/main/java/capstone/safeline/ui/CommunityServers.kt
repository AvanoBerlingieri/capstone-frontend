package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import capstone.safeline.ui.CommunityData.channelsMap


class CommunityServers : ComponentActivity() {
    private val serversState = CommunityData.servers
    private val channelsMap = CommunityData.channelsMap
    private var selectedServerState = mutableStateOf("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedFromChannel = intent.getStringExtra("server_name")
        if (!selectedFromChannel.isNullOrEmpty()) {
            selectedServerState.value = selectedFromChannel
        }
        if (serversState.isEmpty()) {
            serversState.addAll(listOf("A", "B", "C", "D"))
        }
        if (channelsMap.isEmpty()) {
            channelsMap["A"] = mutableStateListOf("CHANNEL 1", "CHANNEL 2")
            channelsMap["B"] = mutableStateListOf()
            channelsMap["C"] = mutableStateListOf()
            channelsMap["D"] = mutableStateListOf()
        }
        setContent {
            CommunityServersScreen(
                onOpenChannel = { channelName ->
                    val intent = Intent(this, ChannelScreen::class.java)
                    intent.putExtra("channel_name", channelName)
                    intent.putExtra("server_name", selectedServerState.value)
                    startActivity(intent)
                },
                servers = serversState,
                channels = channelsMap[selectedServerState.value] ?: emptyList(),
                onAddChannel = {
                    if (selectedServerState.value.isNotEmpty()) {
                        val intent = Intent(this, AddChannel::class.java)
                        startActivityForResult(intent, 2)
                    }
                },
                selectedServer = selectedServerState.value,
                onSelectServer = { selectedServerState.value = it },
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
                },
                onOpenSettings = {
                    val intent = Intent(this, ManageServer::class.java)
                    intent.putExtra("server_name", selectedServerState.value)

                    startActivityForResult(intent, 3)
                },
                onAddServer = {
                    val intent = Intent(this, AddServer::class.java)
                    startActivityForResult(intent, 1)
                }
            )
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 3 && resultCode == RESULT_OK) {
            val updatedName = data?.getStringExtra("updated_name")
            if (!updatedName.isNullOrEmpty()) {
                selectedServerState.value = updatedName
            }

            val deleted = data?.getBooleanExtra("deleted", false)

            if (deleted == true) {
                selectedServerState.value = ""
            }

        }

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == RESULT_OK) {
            val name = data?.getStringExtra("channel_name")

            if (!name.isNullOrEmpty()) {
                val server = selectedServerState.value

                if (!channelsMap.containsKey(server)) {
                    channelsMap[server] = mutableStateListOf()
                }

                channelsMap[server]?.add(name)
            }
        }

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val name = data?.getStringExtra("server_name")

            if (!name.isNullOrEmpty()) {
                serversState.add(name)
                selectedServerState.value = name
            }
        }
    }
}

@Composable
private fun CommunityServersScreen(
    servers: SnapshotStateList<String>,
    channels: List<String>,
    selectedServer: String,
    onSelectServer: (String) -> Unit,
    onBackToMessages: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onAddServer: (() -> Unit)?,
    onAddChannel: () -> Unit,
    onOpenChannel: (String) -> Unit
) {
    var textOpen by remember(selectedServer) { mutableStateOf(false) }
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
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                Image(
                    painter = painterResource(R.drawable.community_servers_bg),
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

            Row(modifier = Modifier.fillMaxSize()) {

                LeftServersPanel(
                    servers = servers,
                    selectedServer = selectedServer,
                    onSelectServer = onSelectServer,
                    onBackToMessages = onBackToMessages,
                    onAddServer = onAddServer,
                    backIcon = R.drawable.community_servers_backtomessages_btn
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 22.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = selectedServer,
                                fontFamily = ThemeManager.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = selectedServer,
                                fontFamily = ThemeManager.fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color.Transparent,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF0DA2FF), Color(0xFFEA00FF))
                                    ),
                                    drawStyle = Stroke(3f)
                                )
                            )
                        }


                        if (selectedServer.isNotEmpty()) {
                            Image(
                                painter = painterResource(R.drawable.community_servers_settings_btn),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable { onOpenSettings() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedServer.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.Start
                        ) {

                        SpaceHeader(
                            text = "TEXT SPACES",
                            onClick = {
                                if (selectedServer.isNotEmpty()) {
                                    textOpen = !textOpen
                                }
                            },
                            onAddChannel = onAddChannel
                        )

                            if (textOpen && selectedServer.isNotEmpty()) {
                                OverlapChannels(
                                    channels = channels,
                                    startX = 0.dp,
                                    onChannelClick = onOpenChannel,
                                    onRemoveChannel = { channel ->
                                        channelsMap[selectedServer]?.remove(channel)
                                    }
                                )
                            }

                        Spacer(modifier = Modifier.height(16.dp))

                        SpaceHeader(
                            text = "VOICE SPACES",
                            onClick = {
                                if (selectedServer.isNotEmpty()) {
                                    voiceOpen = !voiceOpen
                                }
                            },
                            onAddChannel = {}
                        )

                        if (voiceOpen) {
                            OverlapChannels(
                                channels = listOf("CHANNEL 1", "CHANNEL 2", "CHANNEL 3"),
                                startX = 0.dp,
                                onChannelClick = {},
                                onRemoveChannel = {}
                            )
                        }
                    } }
                }
            }
        }
    }
}

@Composable
fun LeftServersPanel(
    servers: List<String>,
    selectedServer: String,
    onSelectServer: (String) -> Unit,
    onBackToMessages: () -> Unit,
    onAddServer: (() -> Unit)?,
    backIcon: Int
) {
    Box(
        modifier = Modifier
            .width(75.dp)
            .fillMaxSize()
    ) {

        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.community_servers_leftside_bg),
                contentDescription = null,
                modifier = Modifier
                    .width(75.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

        } else {

            Box(
                modifier = Modifier
                    .width(75.dp)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            ThemeManager.buttonGradient
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(75.dp)
                .height(57.dp)
        ) {

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                Image(
                    painter = painterResource(R.drawable.community_servers_backtomessages_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                ThemeManager.buttonGradient
                            )
                        )
                )
            }

            Image(
                painter = painterResource(backIcon),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 38.dp, height = 36.dp)
                    .clickable { onBackToMessages() }
            )
        }

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 76.dp)
        ) {

            items(servers, key = { it }) { serverName ->

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onSelectServer(serverName) },
                    contentAlignment = Alignment.Center
                ) {

                    if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                        Image(
                            painter = painterResource(R.drawable.community_servers_icon_btn),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        val shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape)
                                .background(
                                    Brush.verticalGradient(
                                        ThemeManager.buttonGradient
                                    )
                                )
                                .then(
                                    ThemeManager.buttonStroke?.let {
                                        Modifier.border(1.dp, it, shape)
                                    } ?: Modifier
                                )
                        )
                    }

                    StrokeText(
                        serverName.firstOrNull()?.uppercase() ?: "?",
                        fontFamily = ThemeManager.fontFamily,
                        fontSize = 24.sp,
                        fillColor = Color.White,
                        strokeColor = Color(0xFF0251C7),
                        strokeWidth = 1f,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (onAddServer != null) {
            Image(
                painter = painterResource(R.drawable.community_servers_add_btn),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(50.dp)
                    .clickable { onAddServer() }
            )
        }
    }
}

@Composable
private fun SpaceHeader(
    text: String,
    onClick: () -> Unit,
    onAddChannel: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(297.dp)
            .height(57.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.community_servers_voice_text_space),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

        } else {

            val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            ThemeManager.buttonGradient
                        ),
                        shape = shape
                    )
                    .then(
                        ThemeManager.buttonStroke?.let {
                            Modifier.border(
                                1.dp,
                                it,
                                shape
                            )
                        } ?: Modifier
                    )
            )

        }

        Text(
            text = "+",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .clickable { onAddChannel() }
        )

        StrokeText(
            text = text,
            fontFamily = ThemeManager.fontFamily,
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
    startX: Dp,
    onChannelClick: (String) -> Unit,
    onRemoveChannel: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .width(297.dp)
            .height(
                if (channels.isNotEmpty())
                    (39.dp * (channels.size - 1)) + 44.dp
                else
                    0.dp
            )
    ) {
        channels.forEachIndexed { index, title ->

            key(title) {
            val x = startX + (20.dp * index)
            val y = (39.dp * index)

                Box(
                    modifier = Modifier
                        .offset(x = x, y = y)
                        .size(width = 214.dp, height = 44.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { onChannelClick(title) },
                contentAlignment = Alignment.CenterStart
            ) {
                if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                    Image(
                        painter = painterResource(R.drawable.community_servers_channel),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                } else {

                    val shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    ThemeManager.buttonGradient
                                ),
                                shape = shape
                            )
                            .then(
                                ThemeManager.buttonStroke?.let {
                                    Modifier.border(
                                        1.dp,
                                        it,
                                        shape
                                    )
                                } ?: Modifier
                            )
                    )

                }

                StrokeText(
                    text = title,
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 16.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF193DEF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 26.dp)
                )
                    Text(
                        text = "−",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .clickable { onRemoveChannel(title) }
                    )

            }
        }
        }
    }
    }


