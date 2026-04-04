package capstone.safeline.ui.chatting

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.Home
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager

class InviteUsersPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            InviteUsersScreen(
                onBack = { finish() },
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
fun InviteUsersScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {

    val users = remember {
        listOf("Alex", "John", "Maria", "Chris", "Daniel")
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
            currentScreen = "chats",
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

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.Center
            ) {
                StrokeText(
                    text = "ADD USERS",
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 28.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFFB30FFF),
                    strokeWidth = 2f
                )
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
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                ThemeManager.groupCardGradient
                            )
                        )
                        .padding(20.dp)
                ) {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        items(users) { user ->

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
                                        painter = painterResource(R.drawable.group_user_item),
                                        contentDescription = null,
                                        modifier = Modifier.matchParentSize(),
                                        contentScale = ContentScale.FillBounds
                                    )

                                    Text(
                                        text = user,
                                        color = Color.White,
                                        fontFamily = ThemeManager.fontFamily,
                                        modifier = Modifier.padding(start = 16.dp),
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(32.dp)
                                        .clickable { },
                                    contentAlignment = Alignment.Center
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.add_users_btn),
                                        contentDescription = null,
                                        modifier = Modifier.matchParentSize(),
                                        contentScale = ContentScale.FillBounds
                                    )

                                    Text(
                                        text = "Invite",
                                        color = Color.White,
                                        fontFamily = ThemeManager.fontFamily,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}