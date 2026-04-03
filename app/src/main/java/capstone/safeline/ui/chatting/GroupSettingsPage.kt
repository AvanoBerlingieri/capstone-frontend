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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.Home
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.community.CommunityData
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager

class GroupSettingsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""

        setContent {
            GroupSettingsScreen(
                groupId = groupId,
                onBack = { finish() },
                onNavigate = { destination ->
                    val intent = when (destination) {
                        "home" -> Intent(this, Home::class.java)
                        "calls" -> Intent(this, Call::class.java)
                        "chats" -> Intent(this, Chat::class.java)
                        "profile" -> Intent(this, Profile::class.java)
                        "communities" -> Intent(this, Community::class.java)
                        "contacts" -> Intent(this, Contacts::class.java)
                        else -> null
                    }

                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent?.let { startActivity(it) }
                }
            )
        }
    }
}

@Composable
fun GroupSettingsScreen(
    groupId: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {

    val context = LocalContext.current

    val group = CommunityData.groupChats.find { it.id == groupId }

    var name by remember(group) { mutableStateOf(group?.name?.value ?: "") }

    val users = group?.users ?: mutableStateListOf()

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
            }else {
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
                    text = "MANAGE GROUP",
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

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        StrokeText(
                            text = "GROUP NAME",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 20.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {

                                Image(
                                    painter = painterResource(R.drawable.group_name_input),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.FillBounds
                                )

                                TextField(
                                    value = name,
                                    onValueChange = { name = it },
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

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(36.dp)
                                    .clickable {
                                        group?.name?.value = name.toString()

                                        (context as Activity).finish()
                                    },
                                contentAlignment = Alignment.Center
                            ) {

                                Image(
                                    painter = painterResource(R.drawable.add_users_btn),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize()
                                )

                                Text(
                                    "Change",
                                    color = Color.White,
                                    fontFamily = ThemeManager.fontFamily,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        StrokeText(
                            text = "MEMBERS",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 20.sp,
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
                                        ThemeManager.groupInnerGradient
                                    )
                                )
                                .border(1.dp, ThemeManager.groupStroke)
                                .padding(10.dp)
                        ) {

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                LazyColumn(
                                    modifier = Modifier.weight(1f)
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
                                                .width(80.dp)
                                                .height(36.dp)
                                                .clickable {
                                                    users.remove(user)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.delete_btn),
                                                contentDescription = null,
                                                modifier = Modifier.matchParentSize()
                                            )

                                            Text(
                                                "Delete",
                                                color = Color.White,
                                                fontFamily = ThemeManager.fontFamily
                                            )
                                        }
                                    }
                                    }

                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .size(width = 146.dp, height = 47.dp)
                                    .clickable {
                                        context.startActivity(Intent(context, InviteUsersPage::class.java))
                                    },
                                contentAlignment = Alignment.Center
                            ) {

                                Image(
                                    painter = painterResource(R.drawable.add_users_btn),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize()
                                )

                                Text(
                                    "Add Users",
                                    color = Color.White,
                                    fontFamily = ThemeManager.fontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .size(width = 220.dp, height = 35.dp)
                    .clickable {
                        val index = CommunityData.groupChats.indexOfFirst { it.id == groupId }
                        if (index != -1) {
                            CommunityData.groupChats.removeAt(index)
                        }

                        (context as Activity).finish()
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.delete_server_btn),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )

                Text(
                    "LEAVE GROUP",
                    color = Color.White,
                    fontFamily = ThemeManager.fontFamily
                )
            }
        }
    }
}