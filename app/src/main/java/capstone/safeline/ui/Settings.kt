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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeTitle

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Kaushan = FontFamily(Font(R.font.kaushan_script_regular))

class Settings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                },
                onOpenAppearance = { startActivity(Intent(this, SettingsAppearance::class.java)) },
                onOpenFontSize = { startActivity(Intent(this, SettingsFontSize::class.java)) },
                onOpenSound = { startActivity(Intent(this, SettingsSound::class.java)) },
                onOpenPrivacy = { startActivity(Intent(this, SettingsPrivacy::class.java)) },
                onOpenNotifications = { startActivity(Intent(this, SettingsNotifications::class.java)) }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenFontSize: () -> Unit,
    onOpenSound: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenNotifications: () -> Unit
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
                painter = painterResource(id = R.drawable.settings_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "SETTINGS",
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
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                SettingsButton(text = "Appearance", onClick = onOpenAppearance)
                SettingsButton(text = "Font size", onClick = onOpenFontSize)
                SettingsButton(text = "Sound", onClick = onOpenSound)
                SettingsButton(text = "Privacy", onClick = onOpenPrivacy)
                SettingsButton(text = "Notifications", onClick = onOpenNotifications)
            }
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(380.dp)
            .height(48.97.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.settings_btn),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = text,
            fontFamily = Kaushan,
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

