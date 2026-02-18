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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.api.ApiClient
import capstone.safeline.api.dto.LoginRequest
import capstone.safeline.ui.components.GradientStrokeText
import capstone.safeline.ui.components.ImageInputField
import capstone.safeline.ui.components.noRippleClickable
import capstone.safeline.ui.theme.KaushanScript
import capstone.safeline.ui.theme.VampiroOne
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { ApiClient.apiService }

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
                    text = "LOGIN",
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

            Text(
                text = "Please Enter Your",
                fontSize = 48.sp,
                fontFamily = KaushanScript,
                color = Color.White,
                style = TextStyle(
                    drawStyle = Stroke(width = 2f),
                    color = Color(0xFF002BFF)
                )
            )

            Spacer(Modifier.height(24.dp))

            GradientStrokeText("Email:", 28.sp, VampiroOne)
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = email, onValueChange = { email = it })

            Spacer(Modifier.height(22.dp))

            GradientStrokeText("Password:", 28.sp, VampiroOne)
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
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            return@noRippleClickable
                        }

                        scope.launch {
                            try {
                                val response = apiService.loginUser(LoginRequest(email, password))
                                if (response.isSuccessful) {

                                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                } else {
                                    Toast.makeText(context, "Username or Password Incorrect", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("LoginError", "Network error: ${e.message}", e)
                                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}