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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.network.ApiClient
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.GradientStrokeText
import capstone.safeline.ui.components.ImageInputField
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.noRippleClickable
import capstone.safeline.ui.theme.KaushanScript
import capstone.safeline.ui.theme.VampiroOne
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dsManager = remember { DataStoreManager(context, CryptoManager()) }
    val repo =
        remember { AuthRepository(dsManager, ApiClient.provideApiService(context, dsManager)) }

    val isLoggedIn by repo.isLoggedIn.collectAsState(initial = false)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn)
            onSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                StrokeText(text = "Logging in...",
                    fontFamily = KaushanScript,
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
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A2CFF).copy(alpha = 0.85f), Color.Transparent)
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
                GradientStrokeText(text = "LOGIN", fontSize = 28.sp, fontFamily = VampiroOne)
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
                text = "Username/Email:",
                fontFamily = KaushanScript,
                fontSize = 40.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF0066FF),
                strokeWidth = 1f,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = usernameOrEmail, onValueChange = { usernameOrEmail = it })
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

                                val success = repo.login(usernameOrEmail, password)

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