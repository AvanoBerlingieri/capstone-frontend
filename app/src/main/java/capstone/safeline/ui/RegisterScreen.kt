package capstone.safeline.ui

import android.util.Log
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.api.ApiClient
import capstone.safeline.api.dto.RegisterRequest
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.GradientStrokeText
import capstone.safeline.ui.components.ImageInputField
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.noRippleClickable
import capstone.safeline.ui.theme.KaushanScript
import capstone.safeline.ui.theme.VampiroOne
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiClient.apiService }

    val screenH = LocalConfiguration.current.screenHeightDp
    val scale = (screenH / 820f).coerceIn(0.72f, 1f)
    fun s(dp: Int) = ((dp * scale).roundToInt()).dp

    fun handleRegisterClick() {
        if (email.isBlank() || username.isBlank() || password.isBlank() || confirm.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirm) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val response = apiService.createUser(RegisterRequest(email, password, username))
                val registerResp = response.body()

                if (registerResp != null) {
                    Toast.makeText(context, "Registered successfully!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Toast.makeText(context, "User Registration failure", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("RegisterError", "Network error: ${e.message}", e)
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A2CFF).copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                GradientStrokeText(
                    text = "REGISTER",
                    fontSize = 28.sp,
                    fontFamily = VampiroOne
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(s(105)))

            StrokeText(
                text = "Please Enter Your",
                fontFamily = KaushanScript,
                fontSize = 48.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF002BFF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(s(16)))

            StrokeText(
                text = "Username:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(s(8)))
            ImageInputField(value = username, onValueChange = { username = it })

            Spacer(Modifier.height(s(14)))

            StrokeText(
                text = "Email:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(s(8)))
            ImageInputField(value = email, onValueChange = { email = it })

            Spacer(Modifier.height(s(14)))

            StrokeText(
                text = "Password:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(s(8)))
            ImageInputField(value = password, onValueChange = { password = it })

            Spacer(Modifier.height(s(14)))

            StrokeText(
                text = "Re-Enter Password:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(s(8)))
            ImageInputField(value = confirm, onValueChange = { confirm = it })

            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.done_register_button),
                contentDescription = "Register",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp)
                    .noRippleClickable { handleRegisterClick() },
                contentScale = ContentScale.Fit
            )
        }
    }
}
