package capstone.safeline.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R

class CallingPage : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("userName") ?: "Friend 1"

        setContent {
            CallingFriendScreen(
                username = username,
                onEndCall = { finish() }, // goes back to DmPage
                onGoToChat = {
                    // Go straight to Chat list
                    startActivity(Intent(this, Chat::class.java))
                    finish()
                },
                onShareScreen = { /* TODO */ },
                onCamera = { /* TODO */ },
                onMic = { /* TODO */ }
            )
        }
    }
}

@Composable
fun CallingFriendScreen(
    username: String,
    onEndCall: () -> Unit,
    onGoToChat: () -> Unit,
    onShareScreen: () -> Unit,
    onCamera: () -> Unit,
    onMic: () -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top background (fills the screen nicely)
            Image(
                painter = painterResource(R.drawable.top_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Center avatar + text
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar_placeholder),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(190.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Calling",
                    color = Color.White,
                    fontSize = 22.sp
                )

                Text(
                    text = username,
                    color = Color.White,
                    fontSize = 22.sp
                )
            }

            // Bottom background panel
            Image(
                painter = painterResource(R.drawable.down_background_for_buttons),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.FillBounds
            )

            // Buttons laid on top of the bottom panel
            // Layout matches your mock: 2 left, big center, 2 right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 54.dp, start = 26.dp, end = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    IconBtn(res = R.drawable.share_screen_button, size = 58.dp, onClick = onShareScreen)
                    IconBtn(res = R.drawable.got_to_chat_button, size = 58.dp, onClick = onGoToChat)
                }

                // Big center end call
                IconBtn(
                    res = R.drawable.end_call_button,
                    size = 110.dp,
                    onClick = onEndCall
                )

                // Right column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    IconBtn(res = R.drawable.camera_button, size = 58.dp, onClick = onCamera)
                    IconBtn(res = R.drawable.microphone_button, size = 58.dp, onClick = onMic)
                }
            }
        }
    }
}

@Composable
private fun IconBtn(
    res: Int,
    size: Dp,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}