package capstone.safeline.ui

import android.content.Intent
<<<<<<< HEAD
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
=======
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
>>>>>>> origin/master
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
<<<<<<< HEAD
    val context = LocalContext.current
=======
>>>>>>> origin/master

    NavHost(navController = nav, startDestination = "start") {

        composable("start") {
            StartScreen(
                onLogin = { nav.navigate("login") },
                onRegister = { nav.navigate("register") }
            )
        }

        composable("login") {
<<<<<<< HEAD
            LoginScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
                    context.startActivity(Intent(context, Home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
=======

            val context = LocalContext.current

            LoginScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
                    val intent = Intent(context, Home::class.java)
                    context.startActivity(intent)

                    if (context is ComponentActivity) {
                        context.finish()
                    }
>>>>>>> origin/master
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onSuccess = {
<<<<<<< HEAD
                    nav.navigate("login") {
                        popUpTo("register") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

=======
                    nav.navigate("login") { popUpTo("start") { inclusive = false } }
                }
            )
        }

    }
}
>>>>>>> origin/master
