package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    NavHost(navController = nav, startDestination = "start") {

        composable("start") {
            StartScreen(
                onLogin = { nav.navigate("login") },
                onRegister = { nav.navigate("register") }
            )
        }

        composable("login") {

            val context = LocalContext.current

            LoginScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
                    val intent = Intent(context, Home::class.java)
                    context.startActivity(intent)

                    if (context is ComponentActivity) {
                        context.finish()
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
                    nav.navigate("login") { popUpTo("start") { inclusive = false } }
                }
            )
        }

    }
}
