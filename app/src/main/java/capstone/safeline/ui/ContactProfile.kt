package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.*
import capstone.safeline.ui.theme.ThemeManager

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

            // Background
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(R.drawable.profile_bg),
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

            // Header
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

                StrokeTitle(
                    text = "PROFILE",
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

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
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 32.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF002BFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                StrokeText(
                    text = email,
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 22.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF002BFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Chat
                    Image(
                        painter = painterResource(R.drawable.call_for_dm),
                        contentDescription = "Chat",
                        modifier = Modifier
                            .size(width = 92.dp, height = 94.dp)
                            .clickable { onChat() },
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.width(32.dp))

                    // Call
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
        }
    }
}