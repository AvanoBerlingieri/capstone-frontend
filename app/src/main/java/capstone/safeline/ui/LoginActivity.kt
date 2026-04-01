package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import capstone.safeline.ui.theme.ThemeManager

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.loadTheme(this)

        setContent {
            LoginScreen(
                onBack = {
                    finish() // Closes LoginActivity and returns to StartActivity
                },
                onSuccess = {
                    val intent = Intent(this, Home::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}