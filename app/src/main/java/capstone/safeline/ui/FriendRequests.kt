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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private data class UiFriendRequest(
    val name: String,
    val email: String
)

class FriendRequests : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FriendRequestsScreen(
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onOpenProfile = { request ->
                    val intent = Intent(this, ContactProfile::class.java)
                    intent.putExtra("contactName", request.name)
                    intent.putExtra("contactEmail", request.email)
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
private fun FriendRequestsScreen(
    onBack: () -> Unit,
    onOpenProfile: (UiFriendRequest) -> Unit,
    onNavigate: (String) -> Unit
) {
    val requests = remember {
        mutableStateListOf(
            UiFriendRequest("Lynx_GVA", "lynx_gva@email.com"),
            UiFriendRequest("LhekDup228", "lhekdup228@email.com")
        )
    }

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
                painter = painterResource(R.drawable.requests_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "FRIENDS REQUESTS",
                fontFamily = Vampiro,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 22.dp)
            )

            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-15).dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FriendRequestsHeader(count = requests.size)

                Spacer(modifier = Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End,
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    items(requests, key = { it.name }) { request ->
                        FriendRequestRow(
                            request = request,
                            onOpenProfile = { onOpenProfile(request) },
                            onAccept = { requests.remove(request) },
                            onDeny = { requests.remove(request) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRequestsHeader(count: Int) {
    Box(
        modifier = Modifier
            .width(330.dp)
            .height(69.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.request_new),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = "You Have $count New friend Requests",
            fontFamily = Tapestry,
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FriendRequestRow(
    request: UiFriendRequest,
    onOpenProfile: () -> Unit,
    onAccept: () -> Unit,
    onDeny: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(330.dp)
            .height(69.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.requst_new_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxSize()
                    .padding(end = 25.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.request_deny_btn),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onDeny() },
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(R.drawable.request_accept_btn),
                        contentDescription = null,
                        modifier = Modifier
                            .size(33.dp)
                            .clickable { onAccept() },
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(end = 12.dp)
                    .clickable { onOpenProfile() },
                contentAlignment = Alignment.Center
            ) {
                StrokeText(
                    text = request.name,
                    fontFamily = Vampiro,
                    fontSize = 24.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF009DFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
