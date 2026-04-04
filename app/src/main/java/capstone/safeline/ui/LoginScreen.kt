package capstone.safeline.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.GradientStrokeText
import capstone.safeline.ui.components.ImageInputField
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.noRippleClickable
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    val Kaushan = FontFamily(Font(R.font.kaushan_script_regular))
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository.getInstance(context) }

    val isLoggedIn by authRepo.isLoggedIn.collectAsState(initial = false)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn)
            onSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.background),
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                StrokeText(text = "Logging in...",
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 48.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF002BFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC)
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF6A2CFF).copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    else
                        Brush.horizontalGradient(
                            ThemeManager.headerGradient
                        )
                )
        ) {
            BackButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                GradientStrokeText(text = "LOGIN", fontSize = 28.sp, fontFamily = ThemeManager.fontFamily)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(125.dp))
            StrokeText(
                text = "Please Enter Your",
                fontFamily = Kaushan,
                fontSize = 48.sp,
                fillColor = Color.White,
                strokeColor = ThemeManager.titleStroke,
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            StrokeText(
                text = "Username/Email:",
                fontFamily = Kaushan,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = ThemeManager.titleStroke,
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = usernameOrEmail, onValueChange = { usernameOrEmail = it })
            Spacer(Modifier.height(22.dp))
            StrokeText(
                text = "Password:",
                fontFamily = Kaushan,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = ThemeManager.titleStroke,
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = password, onValueChange = { password = it })
            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.done_login_button),
                contentDescription = "Login",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp)
                    .noRippleClickable {
                        if (usernameOrEmail.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            scope.launch {
                                isLoading = true

                                val success = authRepo.login(usernameOrEmail, password)

                                if (success) {
                                    kotlinx.coroutines.delay(500)
                                    onSuccess()
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
            )
        }
    }
}