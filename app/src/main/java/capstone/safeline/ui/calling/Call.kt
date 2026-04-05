package capstone.safeline.ui.calling

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.network.CallingApiClient
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.ContactCall
import capstone.safeline.ui.Contacts
import capstone.safeline.ui.Home
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private enum class CallType { ANSWERED, MISSED }

private data class UiCallItem(
    val type: CallType,
    val name: String,
    /** Shown for non-completed rows (missed / declined / failed). */
    val outcomeLabel: String,
    val date: String,
    val time: String,
    val duration: String? = null
)

class Call : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)

        setContent {
            val scope = rememberCoroutineScope()
            var callItems by remember { mutableStateOf<List<UiCallItem>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var loadError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        loadError = null
                        val username = dataStoreManager.usernameFlow.first()

                        val response = CallingApiClient.service.getCallHistory(username)
                        if (response.isSuccessful) {
                            callItems = response.body()?.map { record ->
                                val status = record.status.trim().lowercase()
                                val isMissed = status == "missed"
                                        || status == "declined"
                                        || status == "failed"

                                val outcomeLabel = when (status) {
                                    "declined" -> "Declined"
                                    "failed" -> "Failed"
                                    "missed" -> "Missed"
                                    else -> ""
                                }

                                UiCallItem(
                                    type = if (isMissed) CallType.MISSED else CallType.ANSWERED,
                                    name = if (record.callerId == username)
                                        record.receiverId else record.callerId,
                                    outcomeLabel = outcomeLabel,
                                    date = record.startTime
                                        ?.substring(0, 10)
                                        ?.replace("-", "/") ?: "",
                                    time = record.startTime
                                        ?.substring(11, 16) ?: "",
                                    duration = record.duration
                                        ?.let { "Call Duration: $it seconds" }
                                )
                            } ?: emptyList()
                        } else {
                            loadError = "Could not load call history (HTTP ${response.code()})"
                        }
                    } catch (e: Exception) {
                        loadError = "Could not load call history: ${e.message ?: "unknown error"}"
                    } finally {
                        isLoading = false
                    }
                }
            }

            CallScreen(
                callItems = callItems,
                isLoading = isLoading,
                loadError = loadError,
                onBack = { finish() },
                onCallClick = { peerId ->
                    val intent = Intent(this, ContactCall::class.java)
                    intent.putExtra("callerName", peerId)
                    intent.putExtra("targetUserId", peerId)
                    startActivity(intent)
                },
                onMakeCall = {
                    startActivity(Intent(this, Contacts::class.java))
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> {}
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
private fun CallScreen(
    callItems: List<UiCallItem>,
    isLoading: Boolean,
    loadError: String?,
    onBack: () -> Unit,
    onCallClick: (peerId: String) -> Unit,
    onMakeCall: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "calls",
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
            // ThemeManager background from master
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(R.drawable.calls_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(ThemeManager.backgroundGradient)
                        )
                )
            }

            // ThemeManager header from master
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
                                Brush.horizontalGradient(ThemeManager.headerGradient)
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
                    text = "CALLS HISTORY",
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
                // Real API loading states from your branch
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading...",
                                color = Color.White,
                                fontFamily = Tapestry,
                                fontSize = 18.sp
                            )
                        }
                    }
                    loadError != null -> {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = loadError,
                                color = Color(0xFFFFB4B4),
                                fontFamily = Tapestry,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                    callItems.isEmpty() -> {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No call history yet",
                                color = Color.White,
                                fontFamily = Tapestry,
                                fontSize = 18.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(callItems) { item ->
                                CallRow(
                                    item = item,
                                    onClick = { onCallClick(item.name) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(R.drawable.calls_make_call_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 127.dp, height = 76.dp)
                        .padding(bottom = 10.dp)
                        .clickable { onMakeCall() },  // ADD THIS
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CallRow(
    item: UiCallItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 379.97.dp, height = 48.97.dp)
            .clickable { onClick() }
    ) {
        // ThemeManager styling from master
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
            val bg = if (item.type == CallType.ANSWERED)
                R.drawable.calls_anwsered_bg else R.drawable.calls_missed_bg

            Image(
                painter = painterResource(bg),
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
                        Brush.horizontalGradient(ThemeManager.buttonGradient),
                        shape = shape
                    )
                    .then(
                        ThemeManager.buttonStroke?.let {
                            Modifier.border(
                                width = 1.dp,
                                color = it,
                                shape = shape
                            )
                        } ?: Modifier
                    )
            )
        }

        when (item.type) {
            CallType.MISSED -> MissedRow(item)
            CallType.ANSWERED -> AnsweredRow(item)
        }
    }
}

@Composable
private fun MissedRow(item: UiCallItem) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.date,
                fontFamily = Tapestry,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = Color(0xFFD9D9D9),
                textAlign = TextAlign.Center
            )
            Text(
                text = item.time,
                fontFamily = Tapestry,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = Color(0xFFD9D9D9),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            StrokeText(
                text = item.name,
                fontFamily = ThemeManager.fontFamily,
                fontSize = 32.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFFFF0099),
                strokeWidth = 1f
            )
            if (item.outcomeLabel.isNotEmpty()) {
                Text(
                    text = item.outcomeLabel,
                    fontFamily = Tapestry,
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Image(
            painter = painterResource(R.drawable.calls_missed_icon),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun AnsweredRow(item: UiCallItem) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.calls_anwsered_icon),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StrokeText(
                text = item.name,
                fontFamily = ThemeManager.fontFamily,
                fontSize = 24.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF002BFF),
                strokeWidth = 1f
            )

            item.duration?.let { d ->
                StrokeText(
                    text = d,
                    fontFamily = Tapestry,
                    fontSize = 16.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0251C7),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier.width(110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.date,
                fontFamily = Tapestry,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = Color(0xFFD9D9D9),
                textAlign = TextAlign.Center
            )
            Text(
                text = item.time,
                fontFamily = Tapestry,
                fontSize = 20.sp,
                lineHeight = 25.sp,
                color = Color(0xFFD9D9D9),
                textAlign = TextAlign.Center
            )
        }
    }
}