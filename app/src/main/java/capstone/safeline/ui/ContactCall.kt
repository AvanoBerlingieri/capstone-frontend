package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.StrokeText

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

class ContactCall : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("callerName") ?: "Contact"

        setContent {
            ContactCallScreen(
                name = name,
                onEndCall = {
                    startActivity(Intent(this, Contacts::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
private fun ContactCallScreen(
    name: String,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.contactcall_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        StrokeText(
            text = "Calling $name...",
            fontFamily = Vampiro,
            fontSize = 36.sp,
            fillColor = Color.White,
            strokeColor = Color(0xFF0066FF),
            strokeWidth = 2f,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 90.dp)
        )

        Image(
            painter = painterResource(R.drawable.contactcall_endcall_btn),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .size(width = 126.dp, height = 118.dp)
                .clickable { onEndCall() },
            contentScale = ContentScale.Fit
        )
    }
}

