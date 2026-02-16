package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.network.CallingApiClient
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.security.CryptoManager
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BottomNavBar


private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private enum class CallType { ANSWERED, MISSED }

private data class UiCallItem(
    val type: CallType,
    val name: String,
    val date: String,
    val time: String,
    val duration: String? = null
)
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.components.BackButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private enum class CallType { ANSWERED, MISSED }

private data class UiCallItem(
    val type: CallType,
    val name: String,
    val date: String,
    val time: String,
    val duration: String? = null
)

class Call : ComponentActivity() {

    private val callLog = listOf(
        CallEntry("John Doe", "Incoming", "12:30 PM"),
        CallEntry("Jane Smith", "Missed", "11:45 AM"),
        CallEntry("Mike Lee", "Missed", "10:20 AM"),
        CallEntry("Avano", "Missed", "5:00 PM"),
        CallEntry("Nima", "Missed", "7:00 AM"),
        CallEntry("Lhek", "Missed", "10:00 PM")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create DataStoreManager with CryptoManager as required
        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)

        val callItems = listOf(
            UiCallItem(CallType.MISSED, "Matthew", "11/21/2025", "1:21 PM"),
            UiCallItem(CallType.MISSED, "Matthew", "11/22/2025", "6:29 PM"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/22/2025", "8:06 PM", "Call Duration: 32 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/22/2025", "10:10 PM", "Call Duration: 45 minutes"),
            UiCallItem(CallType.MISSED, "Matthew", "11/24/2025", "6:59 PM"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/25/2025", "9:59 PM", "Call Duration: 50 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/26/2025", "11:39 PM", "Call Duration: 52 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/27/2025", "5:58 AM", "Call Duration: 20 minutes"),
            UiCallItem(CallType.ANSWERED, "Matthew", "11/27/2025", "5:58 AM", "Call Duration: 20 minutes")
        )

        setContent {
            val scope = rememberCoroutineScope()
            var callItems by remember { mutableStateOf<List<UiCallItem>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        // Use username as the user identifier
                        val username = dataStoreManager.usernameFlow.first()

                        val response = CallingApiClient.service.getCallHistory(username)
                        if (response.isSuccessful) {
                            callItems = response.body()?.map { record ->
                                val isMissed = record.status == "missed"
                                        || record.status == "declined"
                                        || record.status == "failed"

                                UiCallItem(
                                    type = if (isMissed) CallType.MISSED else CallType.ANSWERED,
                                    name = if (record.callerId == username)
                                        record.receiverId else record.callerId,
                                    date = record.startTime
                                        ?.substring(0, 10)
                                        ?.replace("-", "/") ?: "",
                                    time = record.startTime
                                        ?.substring(11, 16) ?: "",
                                    duration = record.duration
                                        ?.let { "Call Duration: $it seconds" }
                                )
                            } ?: emptyList()
                        }
                    } catch (e: Exception) {
                        // keep empty list on error
                    } finally {
                        isLoading = false
                    }
                }
            }

            CallScreen(
                callItems = callItems,
                onBack = { startActivity(Intent(this, Home::class.java)) },
                callItems = callItems,
                isLoading = isLoading,
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onCallClick = { callerName ->
                    val intent = Intent(this, ContactCall::class.java)
                    intent.putExtra("callerName", callerName)
                    startActivity(intent)
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> {}
                        "calls" -> { }
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "calls" -> {}
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> startActivity(Intent(this, Contacts::class.java))
                    }
                }
            )
        }
    }
}

@Composable
fun CallScreen(
    callLog: List<CallEntry>,
    onMakeCallClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0B0014), Color(0xFF0D2244))
    )

    Scaffold(
        topBar = { TopBar(title = "Calls") },
        bottomBar = {
            BottomNavBar(
                currentScreen = "calls",
                onNavigate = onNavigate
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(callLog) { call ->
                        CallCard(
                            call = call,
                            onClick = { onCallClick(call.name) }
                        )
                    }
                }
            }
        }
    }
}
