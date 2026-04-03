package capstone.safeline.ui.community

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import capstone.safeline.R
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.Home
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.theme.ThemeManager


class Community : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("safeline_prefs", MODE_PRIVATE)
        val seen = prefs.getBoolean("seen_communities_intro", false)

        if (seen) {
            startActivity(Intent(this, CommunityServers::class.java))
            finish()
            return
        }

        setContent {
            CommunityIntroScreen(
                onBack = { finish() },
                onContinue = {
                    prefs.edit().putBoolean("seen_communities_intro", true).apply()
                    startActivity(Intent(this, CommunityServers::class.java))
                    finish()
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> {}
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                }
            )
        }
    }
}

@Composable
private fun CommunityIntroScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onNavigate: (String) -> Unit
) {

    InitializeSocket()

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "communities",
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
                painter = painterResource(R.drawable.community_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(70.dp)
                    .statusBarsPadding()
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
                    text = "Your\nCOMMUNITY PAGE",
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Image(
                painter = painterResource(R.drawable.community_continue_btn),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-70).dp)
                    .size(width = 123.dp, height = 121.79.dp)
                    .clickable { onContinue() },
                contentScale = ContentScale.Fit
            )
        }
    }
}



