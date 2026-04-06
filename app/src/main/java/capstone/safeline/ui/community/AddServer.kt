package capstone.safeline.ui.community

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.Home
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager

class AddServer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AddServerScreen(
                onBack = { finish() },
                onConfirm = { serverName ->

                    val resultIntent = Intent()
                    resultIntent.putExtra("server_name", serverName)

                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
                onNavigate = { destination ->
                    val intent = when (destination) {
                        "home" -> Intent(this, Home::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        "calls" -> Intent(this, Call::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        "chats" -> Intent(this, Chat::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        "profile" -> Intent(this, Profile::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        "communities" -> Intent(this, Community::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        "contacts" -> Intent(this, Contacts::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        else -> null
                    }

                    intent?.let { startActivity(it) }
                }
            )
        }
    }
}

@Composable
fun AddServerScreen(
    onBack: () -> Unit,
    onConfirm: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    var serverName by remember { mutableStateOf("") }

    val contacts = listOf("Alex", "John", "Maria", "Kate", "Chris", "David")

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentScreen = "communities",
                onNavigate = onNavigate
            )
        },
        containerColor = Color.Transparent
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                Image(
                    painter = painterResource(R.drawable.add_server_bg),
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

            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
            )

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

                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {

                    Text(
                        text = "ADD SERVER",
                        fontFamily = ThemeManager.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "ADD SERVER",
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
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(858.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                ThemeManager.communityCardGradient
                            )
                        )
                        .padding(20.dp)
                ) {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        StrokeText(
                            text = "NAME",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 24.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {

                            Image(
                                painter = painterResource(R.drawable.add_server_name_input),
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            TextField(
                                value = serverName,
                                onValueChange = { serverName = it },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontFamily = ThemeManager.fontFamily
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        StrokeText(
                            text = "INVITE USERS",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 24.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        ThemeManager.communityInnerGradient
                                    )
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ThemeManager.communityStroke, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {

                            LazyColumn {
                                items(contacts) { name ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.add_server_contact_item),
                                                contentDescription = null,
                                                modifier = Modifier.matchParentSize(),
                                                contentScale = ContentScale.FillBounds
                                            )

                                            Text(
                                                text = name,
                                                color = Color.White,
                                                fontFamily = ThemeManager.fontFamily,
                                                modifier = Modifier.padding(start = 16.dp),
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .width(80.dp)
                                                .height(36.dp)
                                                .clickable { },
                                            contentAlignment = Alignment.Center
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.add_server_invite_btn),
                                                contentDescription = null,
                                                modifier = Modifier.matchParentSize(),
                                                contentScale = ContentScale.FillBounds
                                            )

                                            Text(
                                                "Invite",
                                                color = Color.White,
                                                fontFamily = ThemeManager.fontFamily,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .size(width = 146.dp, height = 47.dp)
                                .clickable {
                                    if (serverName.isNotBlank()) {
                                        onConfirm(serverName)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Image(
                                painter = painterResource(R.drawable.add_server_confirm_btn),
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            Text(
                                "Confirm",
                                color = Color.White,
                                fontFamily = ThemeManager.fontFamily,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}