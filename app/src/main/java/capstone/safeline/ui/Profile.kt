package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.launch


private val Kaushan = FontFamily(Font(R.font.kaushan_script_regular))

class Profile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val authRepo = remember { AuthRepository.getInstance(context) }

            val scope = rememberCoroutineScope()

            ProfileScreen(
                repo = authRepo,
                onBack = { finish() },
                onChangeUsername = { openUpdate("username") },
                onChangePassword = { openUpdate("password") },
                onChangeEmail = { openUpdate("email") },
                onDeleteAccount = {
                    scope.launch {
                        val success = authRepo.deleteAccount()
                        if (success) {
                            val intent = Intent(this@Profile, StartPage::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
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

    private fun openUpdate(mode: String) {
        val intent = Intent(this, AccountUpdateActivity::class.java).apply {
            putExtra("UPDATE_MODE", mode)
        }
        startActivity(intent)
    }
}

@Composable
private fun ProfileScreen(
    repo: AuthRepository,
    onBack: () -> Unit,
    onChangeUsername: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeEmail: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigate: (String) -> Unit
) {
    InitializeSocket()


    val username by repo.usernameFlow.collectAsState(initial = "Loading...")
    val email by repo.emailFlow.collectAsState(initial = "Loading...")

    Scaffold(
        bottomBar = { BottomNavBar(currentScreen = "profile", onNavigate = onNavigate) },
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
                    text = "PROFILE SETTINGS",
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

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

                Spacer(modifier = Modifier.height(18.dp))

                ProfileStrokeText(text = username, fontSize = 40.sp)

                Spacer(modifier = Modifier.height(22.dp))

                ProfileRow(
                    username,
                    40.sp,
                    R.drawable.profile_change_username_btn,
                    Pair(169.dp, 65.dp),
                    onChangeUsername
                )
                Spacer(modifier = Modifier.height(18.dp))
                ProfileRow(
                    "**********",
                    40.sp,
                    R.drawable.profile_change_password_btn,
                    Pair(169.dp, 65.dp),
                    onChangePassword
                )
                Spacer(modifier = Modifier.height(18.dp))
                ProfileRow(
                    email,
                    34.sp,
                    R.drawable.profile_change_email_btn,
                    Pair(195.dp, 65.dp),
                    onChangeEmail,
                    Modifier.offset(x = 14.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                DeleteAccountButton(onClick = onDeleteAccount)

                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

@Composable
fun DeleteAccountButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(250.dp)
            .height(55.dp)
            .background(
                Color(0xFFFF3B30),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Delete Account",
            fontFamily = Kaushan,
            fontSize = 20.sp,
            color = Color.White
        )
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



