package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeTitle

private val HomeTitleFont = FontFamily(Font(R.font.vampiro_one_regular))
private val HomeTextFont = FontFamily(Font(R.font.tapestry_regular))

class Home : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> {}
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                },
                onOpenSettings = { startActivity(Intent(this, Settings::class.java)) },
                onOpenFriendRequests = { startActivity(Intent(this, FriendRequests::class.java)) }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFriendRequests: () -> Unit
) {
    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "home",
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
                painter = painterResource(id = R.drawable.home_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "HOME",
                fontFamily = HomeTitleFont,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 22.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 78.dp)
                    .width(412.dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_profile_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 55.dp, height = 69.dp)
                        .clickable { onNavigate("profile") }
                )

                Image(
                    painter = painterResource(id = R.drawable.home_setting_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 76.dp, height = 61.dp)
                        .clickable { onOpenSettings() }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 124.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_avatar_example),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { onNavigate("profile") },
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "WELCOME BACK\nUSERNAME",
                    fontFamily = HomeTextFont,
                    fontSize = 28.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 420.dp)
                    .width(412.dp)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.width(412.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    HomeImageButton(
                        bgRes = R.drawable.home_btn1,
                        text = "You have 4 Unread\nMessages",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .size(width = 180.dp, height = 150.dp)
                            .clickable { onNavigate("chats") }
                    )

                    HomeImageButton(
                        bgRes = R.drawable.home_btn1,
                        text = "You Have 1 Missed\nCall",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .size(width = 180.dp, height = 150.dp)
                            .clickable { onNavigate("calls") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                HomeImageButton(
                    bgRes = R.drawable.home_btn2,
                    text = "You Have 2 New friend Requests",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .size(width = 400.dp, height = 50.dp)
                        .clickable { onOpenFriendRequests() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                HomeImageButton(
                    bgRes = R.drawable.home_btn3,
                    text = "Community 1: You Have 36 Notifications!\nCommunity 2: You Have 5 Notifications!",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .size(width = 400.dp, height = 80.dp)
                        .clickable { onNavigate("communities") }
                )
            }
        }
    }
}

@Composable
private fun HomeImageButton(
    bgRes: Int,
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = text,
            fontFamily = HomeTextFont,
            fontSize = fontSize,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = (fontSize.value + 2).sp,
            modifier = Modifier.padding(horizontal = 14.dp)
        )
    }
}


