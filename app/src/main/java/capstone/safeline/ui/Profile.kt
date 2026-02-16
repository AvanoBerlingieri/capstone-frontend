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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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

class Profile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onGoHome = { startActivity(Intent(this, Home::class.java)) },
                onChangeUsername = { startActivity(Intent(this, ChangeUsername::class.java)) },
                onChangePassword = { startActivity(Intent(this, ChangePassword::class.java)) },
                onChangeEmail = { startActivity(Intent(this, ChangeEmail::class.java)) },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onChangeUsername: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeEmail: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "profile",
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
                painter = painterResource(R.drawable.profile_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "PROFILE SETTINGS",
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
                    .padding(top = 110.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.home_avatar_example),
                    contentDescription = null,
                    modifier = Modifier.size(width = 161.dp, height = 157.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(18.dp))

                Image(
                    painter = painterResource(R.drawable.profile_home_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 149.dp, height = 48.dp)
                        .clickable { onGoHome() },
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileStrokeText(
                        text = "USERNAME",
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                ProfileRow(
                    leftText = "Username",
                    leftSize = 40.sp,
                    buttonRes = R.drawable.profile_change_username_btn,
                    buttonSize = Pair(169.dp, 65.dp),
                    onButtonClick = onChangeUsername
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileRow(
                    leftText = "**********",
                    leftSize = 40.sp,
                    buttonRes = R.drawable.profile_change_password_btn,
                    buttonSize = Pair(169.dp, 65.dp),
                    onButtonClick = onChangePassword
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileRow(
                    leftText = "Email@email.com",
                    leftSize = 34.sp,
                    buttonRes = R.drawable.profile_change_email_btn,
                    buttonSize = Pair(195.dp, 65.dp),
                    modifier = Modifier.offset(x = 14.dp),
                    onButtonClick = onChangeEmail
                )

                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

@Composable
private fun ProfileRow(
    leftText: String,
    leftSize: androidx.compose.ui.unit.TextUnit,
    buttonRes: Int,
    buttonSize: Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp>,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProfileStrokeText(
            text = leftText,
            fontSize = leftSize,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Image(
            painter = painterResource(buttonRes),
            contentDescription = null,
            modifier = modifier
                .size(width = buttonSize.first, height = buttonSize.second)
                .clickable { onButtonClick() },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ProfileStrokeText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            fontFamily = Kaushan,
            fontSize = fontSize,
            color = Color.White,
            textAlign = TextAlign.Start,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    blurRadius = 6f
                )
            )
        )
        Text(
            text = text,
            fontFamily = Kaushan,
            fontSize = fontSize,
            color = Color.Transparent,
            textAlign = TextAlign.Start,
            style = TextStyle(
                brush = Brush.linearGradient(listOf(Color(0xFF0066FF), Color(0xFF0066FF))),
                drawStyle = Stroke(width = 1f)
            )
        )
    }
}



