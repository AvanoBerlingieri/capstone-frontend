package capstone.safeline.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.Home
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.theme.ThemeManager



class SettingsNotifications : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsPlaceholder(
                title = "NOTIFICATIONS",
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
private fun SettingsPlaceholder(
    title: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    InitializeSocket()

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

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                Image(
                    painter = painterResource(R.drawable.settings_bg),
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
                    text = title,
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}