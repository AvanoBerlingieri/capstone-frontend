package capstone.safeline.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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
            LoginScreen(
                onBack = { nav.popBackStack() },
                onDone = { email, pass ->

                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onDone = { username, email, pass, confirm ->

                }
            )
        }
    }
}
