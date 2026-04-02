package capstone.safeline.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager

class ManageServer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serverName = intent.getStringExtra("server_name") ?: ""

        setContent {
            ManageServerScreen(
                serverName = serverName,
                onBack = { finish() },
                onDelete = {
                    CommunityData.servers.remove(serverName)
                    CommunityData.channelsMap.remove(serverName)

                    val intent = Intent()
                    intent.putExtra("deleted", true)
                    setResult(Activity.RESULT_OK, intent)

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

        if (requestCode == 4 && resultCode == RESULT_OK) {
            val deletedUser = data?.getStringExtra("deleted_user")

            if (!deletedUser.isNullOrEmpty()) {
                val serverName = intent.getStringExtra("server_name") ?: ""
                val list = CommunityData.usersMap[serverName]
                list?.remove(deletedUser)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}

@Composable
fun ManageServerScreen(
    serverName: String,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var name by remember { mutableStateOf(serverName) }
    var currentServerName by remember { mutableStateOf(serverName) }

    val users = CommunityData.usersMap.getOrPut(currentServerName) {
        mutableStateListOf(
            "$currentServerName Alex",
            "$currentServerName John",
            "$currentServerName Maria"
        )
    }
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
                        text = "MANAGE SERVER",
                        fontFamily = ThemeManager.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "MANAGE SERVER",
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
                            text = "NAME",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 24.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {

                                Image(
                                    painter = painterResource(R.drawable.add_server_name_input),
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
                                    .width(90.dp)
                                    .height(36.dp)
                                    .clickable {
                                        val oldName = currentServerName

                                        if (oldName != name) {
                                            CommunityData.servers.indexOfFirst { it == oldName }.let { index ->
                                                if (index != -1) {
                                                    CommunityData.servers[index] = name

                                                    val channels = CommunityData.channelsMap[oldName]
                                                    CommunityData.channelsMap.remove(oldName)
                                                    if (channels != null) {
                                                        CommunityData.channelsMap[name] = channels
                                                    }
                                                    val usersList = CommunityData.usersMap[oldName]
                                                    CommunityData.usersMap.remove(oldName)
                                                    if (usersList != null) {
                                                        CommunityData.usersMap[name] = usersList
                                                    }
                                                    currentServerName = name

                                                    val keysToMove = CommunityData.rolesMap.keys.filter { it.startsWith("${oldName}_") }
                                                    keysToMove.forEach { key ->
                                                        val newKey = key.replaceFirst("${oldName}_", "${name}_")
                                                        CommunityData.rolesMap[newKey] = CommunityData.rolesMap[key]!!
                                                        CommunityData.rolesMap.remove(key)
                                                    }

                                                    val intent = Intent()
                                                    intent.putExtra("updated_name", name)
                                                    (context as Activity).setResult(Activity.RESULT_OK, intent)
                                                }

                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {

                                Image(
                                    painter = painterResource(R.drawable.add_server_invite_btn),
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize()
                                )

                                Text(
                                    "Change",
                                    color = Color.White,
                                    fontFamily = ThemeManager.fontFamily,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        StrokeText(
                            text = "USERS",
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

                            Column(modifier = Modifier.fillMaxSize()) {

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
                                                    painter = painterResource(R.drawable.add_server_contact_item),
                                                    contentDescription = null,
                                                    modifier = Modifier.matchParentSize()
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
                                                        val intent = Intent(context, ManageUser::class.java)
                                                        intent.putExtra("user_name", user)
                                                        intent.putExtra("server_name", name)
                                                        (context as Activity).startActivityForResult(intent, 4)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {

                                                Image(
                                                    painter = painterResource(R.drawable.add_server_invite_btn),
                                                    contentDescription = null,
                                                    modifier = Modifier.matchParentSize()
                                                )

                                                Text(
                                                    "Manage",
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
                                        .padding(top = 10.dp)
                                        .size(width = 146.dp, height = 47.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .clickable {
                                            val intent = Intent(context, InviteUsers::class.java)
                                            intent.putExtra("server_name", name)
                                            context.startActivity(intent)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.add_server_confirm_btn),
                                        contentDescription = null,
                                        modifier = Modifier.matchParentSize()
                                    )

                                    Text(
                                        "Invite Users",
                                        color = Color.White,
                                        fontFamily = ThemeManager.fontFamily,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
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
                    "Delete Server",
                    color = Color.White,
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}