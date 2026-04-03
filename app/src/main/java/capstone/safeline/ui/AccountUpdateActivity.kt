package capstone.safeline.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.dto.auth.UpdateEmailDto
import capstone.safeline.apis.dto.auth.UpdatePasswordDto
import capstone.safeline.apis.dto.auth.UpdateUsernameDto
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.apis.network.ApiClientAuth
import capstone.safeline.ui.components.*
import capstone.safeline.ui.theme.KaushanScript
import kotlinx.coroutines.launch
import capstone.safeline.ui.theme.ThemeManager



class AccountUpdateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mode = intent.getStringExtra("UPDATE_MODE") ?: "username"

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val dsManager = remember { DataStoreManager(context, CryptoManager()) }
            val repo = remember {
                AuthRepository(dsManager, ApiClientAuth.provideApiService(context, dsManager))
            }

            var field1 by remember { mutableStateOf("") }
            var field2 by remember { mutableStateOf("") }

            val title = when(mode) {
                "email" -> "CHANGE EMAIL"
                "password" -> "CHANGE PASSWORD"
                else -> "CHANGE USERNAME"
            }

            val label1 = when(mode) {
                "email" -> "Current Email"
                "password" -> "Current Password"
                else -> "Current Username"
            }

            val label2 = when(mode) {
                "email" -> "New Email"
                "password" -> "New Password"
                else -> "New Username"
            }

            UpdateWrapper(title = title, onBack = { finish() }) {
                Spacer(Modifier.height(120.dp))

                Field(label = label1, value = field1, onValueChange = { field1 = it })
                Spacer(Modifier.height(20.dp))
                Field(label = label2, value = field2, onValueChange = { field2 = it })

                Spacer(Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.done_login_button), // Reuse your done button
                    contentDescription = "Submit",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(bottom = 30.dp)
                        .noRippleClickable {
                            if (field1.isBlank() || field2.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                return@noRippleClickable
                            }

                            scope.launch {
                                val success = when(mode) {
                                    "username" -> repo.changeUsername(
                                        UpdateUsernameDto(
                                            field1,
                                            field2
                                        )
                                    )
                                    "email" -> repo.changeEmail(UpdateEmailDto(field1, field2))
                                    "password" -> repo.updatePassword(
                                        UpdatePasswordDto(
                                            field1,
                                            field2
                                        )
                                    )
                                    else -> false
                                }

                                if (success) {
                                    Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun UpdateWrapper(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Image(
                painter = painterResource(R.drawable.profile_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 22.dp)) {
                    BackButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.TopStart).offset(x = (-15).dp)
                    )
                    StrokeTitle(text = title, fontFamily = ThemeManager.fontFamily, modifier = Modifier.align(Alignment.TopCenter))
                }
                content()
            }
        }
    }
}

@Composable
fun Field(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StrokeText(
            text = label,
            fontFamily = KaushanScript,
            fontSize = 32.sp,
            fillColor = Color.White,
            strokeColor = Color(0xFF0066FF),
            strokeWidth = 1f
        )
        Spacer(Modifier.height(8.dp))
        ImageInputField(value = value, onValueChange = onValueChange)
    }
}