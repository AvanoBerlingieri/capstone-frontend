package capstone.safeline.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
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
import kotlin.math.roundToInt

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            repository = AuthRepository(
                DataStoreManager(context, CryptoManager()),
                ApiClient.provideApiService(context, DataStoreManager(context, CryptoManager()))
            ),
            apiService = ApiClient.provideApiService(
                context,
                DataStoreManager(context, CryptoManager())
            )
        )
    )

    val screenH = LocalConfiguration.current.screenHeightDp
    val scale = (screenH / 820f).coerceIn(0.72f, 1f)
    fun s(dp: Int) = ((dp * scale).roundToInt()).dp

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
            BackButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                GradientStrokeText(text = "REGISTER", fontSize = 28.sp, fontFamily = VampiroOne)
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

            RegistrationField("Username:", username) { username = it }
            RegistrationField("Email:", email) { email = it }
            RegistrationField("Password:", password) { password = it }
            RegistrationField("Re-Enter Password:", passwordConfirm) { passwordConfirm = it }

            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.done_register_button),
                contentDescription = "Register",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp)
                    .noRippleClickable {
                        if (password != passwordConfirm) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            authViewModel.register(username, email, password) { success ->
                                if (success) onSuccess() else Toast.makeText(
                                    context,
                                    "Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

// made this because register fields were repeated
@Composable
fun RegistrationField(label: String, value: String, onValueChange: (String) -> Unit) {
    Spacer(Modifier.height(8.dp))
    StrokeText(
        text = label,
        fontFamily = KaushanScript,
        fontSize = 32.sp,
        fillColor = Color.White,
        strokeColor = Color(0xFF0066FF),
        strokeWidth = 1f
    )
    ImageInputField(value = value, onValueChange = onValueChange)
}