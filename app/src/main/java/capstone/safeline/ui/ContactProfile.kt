package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class ContactProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("contactName") ?: "USERNAME"
        val email = intent.getStringExtra("contactEmail") ?: "email@email.com"

        setContent {
            ContactProfileScreen(
                username = username,
                email = email,
                onBack = { startActivity(Intent(this, Contacts::class.java)) },
                onCall = {
                    val intent = Intent(this, ContactCall::class.java)
                    intent.putExtra("callerName", username)
                    intent.putExtra("targetUserId", username)
                    startActivity(intent)
                },
                onChat = {
                    val intent = Intent(this, DmPage::class.java)
                    intent.putExtra("userName", username)
                    startActivity(intent)
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
}

@Composable
private fun ContactProfileScreen(
    username: String,
    email: String,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onChat: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "contacts",
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
                painter = painterResource(R.drawable.profile_bg),
                painter = painterResource(R.drawable.contacts_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "PROFILE",
                fontFamily = Vampiro,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 22.dp)
            )

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.home_avatar_example),
                    contentDescription = null,
                    modifier = Modifier.size(width = 161.dp, height = 157.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                StrokeText(
                    text = username,
                    fontFamily = Vampiro,
                    fontSize = 32.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF002BFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                StrokeText(
                    text = email,
                    fontFamily = Vampiro,
                    fontSize = 22.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF002BFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Call and Chat buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chat button
                    Image(
                        painter = painterResource(R.drawable.call_for_dm),
                        contentDescription = "Chat",
                        modifier = Modifier
                            .size(width = 92.dp, height = 94.dp)
                            .clickable { onChat() },
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.width(32.dp))

                    // Call button
                    Image(
                        painter = painterResource(R.drawable.calls_make_call_btn),
                        contentDescription = "Call",
                        modifier = Modifier
                            .size(width = 92.dp, height = 94.dp)
                            .clickable { onCall() },
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.back_btn),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 14.dp)
                    .size(width = 78.55.dp, height = 36.45.dp)
                    .clickable { onBack() }
            )
        }
    }
}
