package capstone.safeline.ui.friends

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import capstone.safeline.ui.Home
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager
import capstone.safeline.ui.chatting.DmPage
import capstone.safeline.ui.calling.ContactCall


class ContactProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contactId = intent.getStringExtra("contactId") ?: ""
        val username = intent.getStringExtra("contactName") ?: "USERNAME"
        val email = intent.getStringExtra("contactEmail") ?: "email@email.com"

        setContent {
            ContactProfileScreen(
                contactId = contactId,
                username = username,
                email = email,
                onBack = { finish() },
                onMessage = {
                    val intent = Intent(this, DmPage::class.java)
                    intent.putExtra("userName", username)
                    startActivity(intent)
                },
                onCall = {
                    val intent = Intent(this, ContactCall::class.java)
                    intent.putExtra("callerName", username)
                    startActivity(intent)
                },
                onDelete = {
                    val intent = Intent()
                    intent.putExtra("deleted_contact_id", contactId)
                    setResult(RESULT_OK, intent)
                    finish()
                },
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
private fun ContactProfileScreen(
    contactId: String,
    username: String,
    email: String,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onCall: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: (String) -> Unit
) {

    InitializeSocket()

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

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(width = 120.dp, height = 44.dp)
                            .clickable { onMessage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.contact_profile_btn),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        StrokeText(
                            text = "MESSAGE",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 14.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF002BFF),
                            strokeWidth = 1f
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 120.dp, height = 44.dp)
                            .clickable { onCall() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.contact_profile_btn),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        StrokeText(
                            text = "CALL",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 14.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF002BFF),
                            strokeWidth = 1f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .size(width = 220.dp, height = 44.dp)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.delete_server_btn),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    StrokeText(
                        text = "DELETE CONTACT",
                        fontFamily = ThemeManager.fontFamily,
                        fontSize = 14.sp,
                        fillColor = Color.White,
                        strokeColor = Color(0xFF002BFF),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

