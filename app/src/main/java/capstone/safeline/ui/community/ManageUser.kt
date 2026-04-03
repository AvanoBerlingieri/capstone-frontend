package capstone.safeline.ui.community

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.Home
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager

class ManageUser : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("user_name") ?: ""
        val serverName = intent.getStringExtra("server_name") ?: ""

        setContent {
            ManageUserScreen(
                userName = userName,
                serverName = serverName,
                onBack = { finish() },
                onDelete = {
                    val intent = Intent()
                    intent.putExtra("deleted_user", userName)
                    setResult(RESULT_OK, intent)
                    finish()
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                }
            )
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 5) {

        }
    }
}

@Composable
fun ManageUserScreen(
    userName: String,
    serverName: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: (String) -> Unit
) {

    val serverRoles = CommunityData.rolesMap.getOrPut(serverName) { mutableMapOf() }
    val roles = serverRoles.getOrPut(userName) { mutableStateListOf<CommunityData.Role>() }
    val context = LocalContext.current

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
                modifier = Modifier.align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(70.dp)
            ) {

                Box(modifier = Modifier.align(Alignment.Center)) {

                    Text(
                        text = "MANAGE $userName",
                        fontFamily = ThemeManager.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "MANAGE $userName",
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
                        .fillMaxHeight()
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
                            text = "ROLES",
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
                                items(roles) { role ->

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
                                                text = role.name,
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
                                                    val intent = Intent(context, EditRole::class.java)
                                                    intent.putExtra("role_name", role.name)
                                                    intent.putExtra("server_name", serverName)
                                                    intent.putExtra("user_name", userName)
                                                    (context as Activity).startActivityForResult(intent, 5)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.add_server_invite_btn),
                                                contentDescription = null,
                                                modifier = Modifier.matchParentSize(),
                                                contentScale = ContentScale.FillBounds
                                            )

                                            Text(
                                                "Edit",
                                                color = Color.White,
                                                fontFamily = ThemeManager.fontFamily,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .size(width = 146.dp, height = 47.dp)
                                    .clickable {
                                        val intent = Intent(context, EditRole::class.java)
                                        intent.putExtra("server_name", serverName)
                                        intent.putExtra("user_name", userName)
                                        (context as Activity).startActivityForResult(intent, 5)
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
                                    "Add Role",
                                    color = Color.White,
                                    fontFamily = ThemeManager.fontFamily,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(width = 220.dp, height = 35.dp)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.delete_server_btn),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                    Text(
                        "Ban User",
                        color = Color.White,
                        fontFamily = ThemeManager.fontFamily,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(width = 220.dp, height = 35.dp)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.delete_server_btn),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize()
                    )
                    Text(
                        "Delete User",
                        color = Color.White,
                        fontFamily = ThemeManager.fontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }

    }
}