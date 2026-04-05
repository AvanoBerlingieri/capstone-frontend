package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.calling.IncomingCallActivity
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.friends.FriendRequests
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.settings.Settings
import capstone.safeline.ui.theme.ThemeManager
import capstone.safeline.webrtc.SignalingClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val HomeTitleFont = FontFamily(Font(R.font.vampiro_one_regular))
private val HomeTextFont = FontFamily(Font(R.font.tapestry_regular))

class Home : ComponentActivity() {

    private var signalingClient: SignalingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.loadTheme(this)
        super.onCreate(savedInstanceState)

        // Setup signaling for incoming calls
        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)
        signalingClient = SignalingClient("http://10.0.2.2:8093/ws-call/websocket")

        lifecycleScope.launch {
            try {
                android.util.Log.d("HOME", "Starting signaling setup...")
                val token = dataStoreManager.tokenFlow.first()
                android.util.Log.d("HOME", "Token exists: ${token != null}")

                val currentUserId = token?.let {
                    extractUserIdFromJwt(it)
                } ?: dataStoreManager.usernameFlow.first()

                signalingClient?.onSignalReceived = { signal ->
                    android.util.Log.d("HOME", "Signal received: ${signal.type}")
                    when (signal.type) {
                        "offer" -> {
                            runOnUiThread {
                                android.util.Log.d("HOME", "Incoming call from: ${signal.senderId}")
                                val intent = Intent(this@Home, IncomingCallActivity::class.java)
                                intent.putExtra("callerName", "Incoming Call")
                                intent.putExtra("targetUserId", signal.senderId)
                                intent.putExtra("incomingSdp", signal.sdp)
                                startActivity(intent)
                            }
                        }
                    }
                }

                android.util.Log.d("HOME", "Connecting as: $currentUserId")
                signalingClient?.connect(currentUserId, token ?: "")
                android.util.Log.d("HOME", "connect() called (handshake is async)")
            } catch (e: Exception) {
                android.util.Log.e("HOME", "Signaling error: ${e.message}")
            }
        }

        setContent {
            HomeScreen(
                onNavigate = { destination ->
                    val intent = when (destination) {
                        "home" -> null
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
                },
                onOpenSettings = { startActivity(Intent(this, Settings::class.java)) },
                onOpenFriendRequests = { startActivity(Intent(this, FriendRequests::class.java)) }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        signalingClient?.disconnect()
    }
}

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFriendRequests: () -> Unit
) {
    InitializeSocket()
    val context = LocalContext.current
    val authRepo = remember { AuthRepository.getInstance(context) }
    // ✅ Fixed username display
    val username by authRepo.usernameFlow.collectAsState(initial = "Loading...")

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
                    painter = painterResource(id = R.drawable.home_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(ThemeManager.backgroundGradient))
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
                            .background(Brush.horizontalGradient(ThemeManager.headerGradient))
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
                    text = "HOME",
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

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

                // ✅ Fixed username
                Text(
                    text = "WELCOME BACK $username",
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
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
            Image(
                painter = painterResource(id = bgRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        } else {
            val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(ThemeManager.buttonGradient),
                        shape = shape
                    )
                    .then(
                        ThemeManager.buttonStroke?.let {
                            Modifier.border(1.dp, it, shape)
                        } ?: Modifier
                    )
            )
        }

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