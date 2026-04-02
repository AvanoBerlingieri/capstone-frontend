package capstone.safeline.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class OngoingCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callerName = intent.getStringExtra("callerName") ?: "Unknown"

        setContent {
            CallingUI(callerName)
        }
    }
}

@Composable
fun CallingUI(caller: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D2244)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = caller,
                fontSize = 34.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Calling...",
                fontSize = 20.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(onClick = { /* hang up logic */ }) {
                Text("End Call")
            }
        }
    }
}
