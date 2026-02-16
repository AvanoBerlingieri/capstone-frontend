package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.components.BackButton


private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private enum class CallType { ANSWERED, MISSED }

private data class UiCallItem(
    val type: CallType,
    val name: String,
    val date: String,
    val time: String,
    val duration: String? = null
)

class Call : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callItems = listOf(
            UiCallItem(CallType.MISSED, "Matthew", "11/21/2025", "1:21 PM"),
            UiCallItem(CallType.MISSED, "Matthew", "11/22/2025", "6:29 PM"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/22/2025", "8:06 PM", "Call Duration: 32 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/22/2025", "10:10 PM", "Call Duration: 45 minutes"),
            UiCallItem(CallType.MISSED, "Matthew", "11/24/2025", "6:59 PM"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/25/2025", "9:59 PM", "Call Duration: 50 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/26/2025", "11:39 PM", "Call Duration: 52 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/27/2025", "5:58 AM", "Call Duration: 20 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/27/2025", "5:58 AM", "Call Duration: 20 minutes")
        )

        setContent {
            CallScreen(
                callItems = callItems,
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onCallClick = { callerName ->
                    val intent = Intent(this, ContactCall::class.java)
                    intent.putExtra("callerName", callerName)
                    startActivity(intent)
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
    onBack: () -> Unit,
    onCallClick: (String) -> Unit,
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
            Image(
                painter = painterResource(R.drawable.calls_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "CALLS HISTORY",
                fontFamily = Vampiro,
                modifier = Modifier
                    .align(Alignment.TopCenter)
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

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = painterResource(R.drawable.calls_make_call_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 127.dp, height = 76.dp)
                        .padding(bottom = 10.dp)
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
        val bg = if (item.type == CallType.ANSWERED) R.drawable.calls_anwsered_bg else R.drawable.calls_missed_bg

        Image(
            painter = painterResource(bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

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

        StrokeText(
            text = item.name,
            fontFamily = Vampiro,
            fontSize = 32.sp,
            fillColor = Color.White,
            strokeColor = Color(0xFFFF0099),
            strokeWidth = 1f
        )

        Spacer(modifier = Modifier.weight(1f))

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
                fontFamily = Vampiro,
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


