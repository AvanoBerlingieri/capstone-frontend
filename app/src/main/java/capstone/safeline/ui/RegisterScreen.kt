package capstone.safeline.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.api.ApiClient
import capstone.safeline.api.dto.RegisterRequest
import capstone.safeline.ui.components.GradientStrokeText
import capstone.safeline.ui.components.ImageInputField
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.noRippleClickable
import capstone.safeline.ui.theme.KaushanScript
import capstone.safeline.ui.theme.VampiroOne
import kotlinx.coroutines.launch

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

    Box(Modifier.fillMaxSize()) {

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
            Image(
                painter = painterResource(R.drawable.back_button),
                contentDescription = "Back",
                modifier = Modifier
                    .padding(start = 12.dp, top = 18.dp)
                    .size(64.dp)
                    .noRippleClickable { onBack() }
                    .align(Alignment.TopStart),
                contentScale = ContentScale.Fit
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

            Spacer(Modifier.height(125.dp))

            StrokeText(
                text = "Please Enter Your",
                fontFamily = KaushanScript,
                fontSize = 48.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF002BFF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )


            Spacer(Modifier.height(24.dp))

            StrokeText(
                text = "Username:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = username, onValueChange = { username = it })

            Spacer(Modifier.height(22.dp))

            StrokeText(
                text = "Email:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = email, onValueChange = { email = it })

            Spacer(Modifier.height(22.dp))

            StrokeText(
                text = "Password:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))
            ImageInputField(value = password, onValueChange = { password = it })

            Spacer(Modifier.height(22.dp))

            StrokeText(
                text = "Re-Enter Password:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
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
