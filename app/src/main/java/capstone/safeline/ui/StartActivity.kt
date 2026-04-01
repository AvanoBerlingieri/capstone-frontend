package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import capstone.safeline.R
import capstone.safeline.apis.network.ApiClient
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.theme.ThemeManager

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.loadTheme(this)

        setContent {
            val context = LocalContext.current
            val dsManager = remember { DataStoreManager(context, CryptoManager()) }
            val repo = remember { AuthRepository(dsManager, ApiClient.provideApiService(context, dsManager)) }

            val isLoggedIn by repo.isLoggedIn.collectAsState(initial = null)

            // Auto-login check
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn == true) {
                    val intent = Intent(context, Home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                    finish()
                }
            }

            // Show StartScreen if user is NOT logged in
            if (isLoggedIn == false) {
                StartScreen(
                    onLogin = {
                        startActivity(Intent(context, LoginActivity::class.java))
                    },
                    onRegister = {
                        startActivity(Intent(context, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun StartScreen(
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.start_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            Image(
                painter = painterResource(R.drawable.login_button),
                contentDescription = "Log In",
                modifier = Modifier
                    .width(220.dp)
                    .height(84.dp)
                    .noRippleClickable { onLogin() },
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(14.dp))

            Image(
                painter = painterResource(R.drawable.register_button),
                contentDescription = "Register",
                modifier = Modifier
                    .width(340.dp)
                    .height(44.dp)
                    .noRippleClickable { onRegister() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }