package capstone.safeline.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager

class AddChannel : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AddChannelScreen(
                onBack = { finish() },
                onConfirm = { name ->
                    val intent = Intent()
                    intent.putExtra("channel_name", name)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun AddChannelScreen(
    onBack: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var channelName by remember { mutableStateOf("") }

    Scaffold(containerColor = Color.Transparent) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

                Image(
                    painter = painterResource(R.drawable.add_server_bg),
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

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0x80002BFF),
                                    Color(0x80B30FFF)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        StrokeText(
                            text = "CHANNEL NAME",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 22.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TextField(
                            value = channelName,
                            onValueChange = { channelName = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = ThemeManager.fontFamily
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(width = 140.dp, height = 45.dp)
                                .clickable { onConfirm(channelName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Confirm",
                                color = Color.White,
                                fontFamily = ThemeManager.fontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}