package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.models.Message
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChannelScreen : ComponentActivity() {

    private val serversState = CommunityData.servers
    private var selectedServerState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelName = intent.getStringExtra("channel_name") ?: ""
        selectedServerState.value = intent.getStringExtra("server_name") ?: ""

        setContent {
            ChannelScreenUI(
                servers = serversState,
                selectedServer = selectedServerState.value,
                onSelectServer = { server ->
                    val intent = Intent(this, CommunityServers::class.java)
                    intent.putExtra("server_name", server)
                    startActivity(intent)
                    finish()
                },
                channelName = channelName,
                onBack = { finish() }
            )
        }
    }
}

@Composable
private fun ChannelScreenUI(
    servers: List<String>,
    selectedServer: String,
    onSelectServer: (String) -> Unit,
    channelName: String,
    onBack: () -> Unit
) {

    var messages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

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
                onBackToMessages = {},
                onAddServer = null,
                backIcon = R.drawable.back_btn
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 22.dp, start = 4.dp, end = 4.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = channelName,
                            fontFamily = ThemeManager.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.White
                        )

                        Text(
                            text = channelName,
                            fontFamily = ThemeManager.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.Transparent,
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF0DA2FF), Color(0xFFEA00FF))
                                ),
                                drawStyle = Stroke(3f)
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages.withIndex().toList()) { indexed ->
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
                                    modifier = Modifier.padding(18.dp)
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
                            .padding(horizontal = 0.dp),
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
                                modifier = Modifier.fillMaxSize()
                            )

                            androidx.compose.material3.TextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp),
                                singleLine = true,
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(0.dp))

                        Image(
                            painter = painterResource(R.drawable.attach),
                            contentDescription = null,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { },
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

                                    val time = LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("hh:mm a"))

                                    messages = messages + Message(text, time)
                                    text = ""
                                }
                        )
                    }
                }
            }
        }

        BackButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}