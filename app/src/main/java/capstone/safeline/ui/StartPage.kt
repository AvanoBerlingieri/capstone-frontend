package capstone.safeline.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class StartPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SafeLineNav() }
    }
}

@Composable
fun SafeLineNav() {
    val nav = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = nav, startDestination = "start") {

        composable("start") {
            StartScreen(
                onLogin = { nav.navigate("login") },
                onRegister = { nav.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
                    context.startActivity(Intent(context, Home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
                    nav.navigate("login") {
                        popUpTo("register") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

