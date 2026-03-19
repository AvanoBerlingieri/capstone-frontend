package capstone.safeline.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import capstone.safeline.R
import capstone.safeline.ui.components.*
import capstone.safeline.ui.theme.*
import capstone.safeline.ui.viewmodel.AuthViewModelFactory
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.apis.network.ApiClient
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            repository = AuthRepository(
                DataStoreManager(context, CryptoManager()),
                ApiClient.provideApiService(context, DataStoreManager(context, CryptoManager()))
            ),
            apiService = ApiClient.provideApiService(context, DataStoreManager(context, CryptoManager()))
        )
    )

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onSuccess()
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
                        colors = listOf(Color(0xFF6A2CFF).copy(alpha = 0.85f), Color.Transparent)
                    )
                )
        ) {
            BackButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                GradientStrokeText(text = "LOGIN", fontSize = 28.sp, fontFamily = VampiroOne)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(125.dp))
            StrokeText(
                text = "Please Enter Your", fontFamily = KaushanScript, fontSize = 48.sp,
                fillColor = Color.White, strokeColor = Color(0xFF002BFF), strokeWidth = 1f, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            StrokeText(
                text = "Username/Email:", fontFamily = KaushanScript, fontSize = 40.sp,
                fillColor = Color.White, strokeColor = Color(0xFF0066FF), strokeWidth = 1f, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            ImageInputField(value = usernameOrEmail, onValueChange = { usernameOrEmail = it })
            Spacer(Modifier.height(22.dp))
            StrokeText(
                text = "Password:", fontFamily = KaushanScript, fontSize = 40.sp,
                fillColor = Color.White, strokeColor = Color(0xFF0066FF), strokeWidth = 1f, textAlign = TextAlign.Center
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
                            authViewModel.login(usernameOrEmail, password)
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}