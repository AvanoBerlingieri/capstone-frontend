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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager

class EditRole : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val roleName = intent.getStringExtra("role_name")
        val serverName = intent.getStringExtra("server_name") ?: ""
        val userName = intent.getStringExtra("user_name") ?: ""

        setContent {
            EditRoleScreen(
                serverName = serverName,
                roleName = roleName,
                userName = userName,
                onBack = { finish() },
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
}

@Composable
fun EditRoleScreen(
    serverName: String,
    roleName: String?,
    userName: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {

    val context = LocalContext.current

    val permissionsKeys = listOf(
        "Manage Server",
        "Manage Roles",
        "Manage Users",
        "Access Channels",
        "Send Files",
        "Send Messages",
        "Unremovable for User"
    )

    val serverRoles = CommunityData.rolesMap.getOrPut(serverName) { mutableMapOf() }
    val roles = serverRoles.getOrPut(userName) { mutableStateListOf<CommunityData.Role>() }

    val existingRole = roles.find { it.name == roleName }

    var name by remember { mutableStateOf(roleName ?: "") }

    val permissions = remember {
        mutableStateMapOf<String, String>().apply {
            permissionsKeys.forEach {
                put(it, existingRole?.permissions?.get(it) ?: "N/A")
            }
        }
    }

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
                        text = "MANAGE ROLES",
                        fontFamily = ThemeManager.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White
                    )

                    Text(
                        text = "MANAGE ROLES",
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
                            text = "ROLE'S NAME",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 24.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

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

                        Spacer(modifier = Modifier.height(20.dp))

                        StrokeText(
                            text = "ROLE'S PERMISSIONS",
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
                                        ThemeManager.communityInnerGradient
                                    )
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, ThemeManager.communityStroke, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {

                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {

                                    permissionsKeys.forEach { key ->
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
                                                    text = key,
                                                    color = Color.White,
                                                    fontFamily = ThemeManager.fontFamily,
                                                    modifier = Modifier.padding(start = 16.dp),
                                                    fontSize = 14.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .width(80.dp)
                                                    .height(36.dp)
                                                    .clickable {
                                                        val current = permissions[key] ?: "N/A"
                                                        permissions[key] = when (current) {
                                                            "Yes" -> "No"
                                                            "No" -> "N/A"
                                                            else -> "Yes"
                                                        }
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
                                                    permissions[key] ?: "N/A",
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

                                            if (roleName != null) {
                                                val index = roles.indexOfFirst { it.name == roleName }
                                                if (index != -1) {
                                                    roles[index] = CommunityData.Role(name, permissions.toMutableMap())
                                                }
                                            } else {
                                                roles.add(CommunityData.Role(name, permissions.toMutableMap()))
                                            }

                                            (context as Activity).finish()
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
                        .clickable {
                            roles.removeIf { it.name == roleName || it.name == name }
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
                        "Delete Role",
                        color = Color.White,
                        fontFamily = ThemeManager.fontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}