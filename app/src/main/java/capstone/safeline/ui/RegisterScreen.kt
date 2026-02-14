package capstone.safeline.ui

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.GradientStrokeText
import capstone.safeline.ui.components.ImageDoneButton
import capstone.safeline.ui.components.ImageInputField
import capstone.safeline.ui.components.noRippleClickable
import capstone.safeline.ui.theme.KaushanScript
import capstone.safeline.ui.theme.VampiroOne

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onDone: (username: String, email: String, password: String, confirm: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

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

            GradientStrokeText(
                text = "Username:",
                fontSize = 28.sp,
                fontFamily = VampiroOne
            )

            Spacer(Modifier.height(10.dp))

            ImageInputField(
                value = username,
                onValueChange = { username = it }
            )

            Spacer(Modifier.height(22.dp))

            GradientStrokeText(
                text = "Email:",
                fontSize = 28.sp,
                fontFamily = VampiroOne
            )

            Spacer(Modifier.height(10.dp))

            ImageInputField(
                value = email,
                onValueChange = { email = it }
            )

            Spacer(Modifier.height(22.dp))

            GradientStrokeText(
                text = "Password:",
                fontSize = 28.sp,
                fontFamily = VampiroOne
            )

            Spacer(Modifier.height(10.dp))

            ImageInputField(
                value = password,
                onValueChange = { password = it }
            )

            Spacer(Modifier.height(22.dp))

            GradientStrokeText(
                text = "Re-Enter Password:",
                fontSize = 28.sp,
                fontFamily = VampiroOne
            )

            Spacer(Modifier.height(10.dp))

            ImageInputField(
                value = confirm,
                onValueChange = { confirm = it }
            )

            Spacer(Modifier.weight(1f))

            ImageDoneButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp),
                onClick = { onDone(username, email, password, confirm) }
            )
        }
    }
}
