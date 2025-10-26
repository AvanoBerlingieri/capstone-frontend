package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.api.ApiService
import capstone.safeline.api.dto.RegisterRequest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Register : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //this url passes an emulator check
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        setContent {
            RegisterScreen(
                apiService = apiService,
                onLoginClick = {
                    startActivity(Intent(this, Login::class.java))
                }
            )
        }
    }
}

@Composable
fun RegisterScreen(apiService: ApiService, onLoginClick: () -> Unit){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B0014),
            Color(0xFF0D2244)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "SafeLine",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(60.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email", color = Color(0xFFAAAAAA)) },
                singleLine = true,
                modifier = Modifier
                    .width(280.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Username", color = Color(0xFFAAAAAA)) },
                singleLine = true,
                modifier = Modifier
                    .width(280.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color(0xFFAAAAAA)) },
                singleLine = true,
                modifier = Modifier
                    .width(280.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = TextStyle(color = Color.White),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(80.dp))

            val activity = LocalActivity.current
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response = apiService.createUser(RegisterRequest(email, password, username))
                            val registerResp = response.body()
                            //check register response
                            if (registerResp?.email != null){
                                activity?.startActivity(Intent(activity, Login::class.java))
                            } else {
                                Toast.makeText(
                                    activity,
                                    "User Registration failure",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }  catch (e: Exception) {
                            Log.e("LoginError", "Network error: ${e.message}", e)
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFF0066)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, Color(0xFFFF0066)),
                modifier = Modifier
                    .width(250.dp)
                    .height(60.dp)
            ) {
                Text("Register", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFF0066)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, Color(0xFFFF0066)),
                modifier = Modifier
                    .width(250.dp)
                    .height(60.dp)
            ) {
                Text("Log in", fontWeight = FontWeight.Bold)
            }
        }
    }

}