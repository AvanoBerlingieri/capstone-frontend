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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.StrokeText

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class CallingPage : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("userName") ?: "Friend 1"

        setContent {
            CallingFriendScreen(
                username = username,
                onEndCall = { finish() },
                onGoToChat = {
                    val intent = Intent(this, DmPage::class.java)
                    intent.putExtra("userName", username)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                },
                onShareScreen = {},
                onCamera = {},
                onMic = {}
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
            Image(
                painter = painterResource(R.drawable.top_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(241.26.dp, 253.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(0.dp))

                StrokeText(
                    text = "Calling",
                    fontFamily = Vampiro,
                    fontSize = 20.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-34).dp)
                        .width(106.56.dp)
                        .height(39.51.dp)
                )

                StrokeText(
                    text = username,
                    fontFamily = Vampiro,
                    fontSize = 20.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF0066FF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = 10.dp, y = (-44).dp)
                        .width(106.56.dp)
                        .height(39.51.dp)
                )
            }

            Image(
                painter = painterResource(R.drawable.down_background_for_buttons),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(412.dp)
                    .height(367.dp),
                contentScale = ContentScale.FillBounds
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(412.dp)
                    .height(367.dp)
                    .padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .width(110.dp)
                        .offset(x = (-25).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    IconBtn(R.drawable.share_screen_button, 92.dp, 94.dp, onShareScreen)
                    IconBtn(R.drawable.go_to_chat_button, 92.dp, 94.dp, onGoToChat)
                }

                Box(
                    modifier = Modifier.offset(x = (-15).dp)
                ) {
                    IconBtn(R.drawable.end_call_button, 181.dp, 159.dp, onEndCall, true)
                }


                Column(
                    modifier = Modifier.width(110.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    IconBtn(R.drawable.camera_button, 92.dp, 94.dp, onCamera)
                    IconBtn(R.drawable.microphone_button, 92.dp, 94.dp, onMic)
                }
            }
        }
    }
}

@Composable
private fun IconBtn(
    res: Int,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    big: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(width, height)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.size(if (big) width else 72.dp),
            contentScale = ContentScale.Fit
        )
    }
}














