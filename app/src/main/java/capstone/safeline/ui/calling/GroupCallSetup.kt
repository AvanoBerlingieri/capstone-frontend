package capstone.safeline.ui.calling

import androidx.compose.foundation.layout.statusBarsPadding
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.apis.network.CallingApiClient
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.Home
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.chatting.Chat
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.profile.Profile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))
private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private data class GroupContact(
    val userId: String,
    val name: String,
    var isSelected: Boolean = false
)

class GroupCallSetup : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""

        val cryptoManager = CryptoManager()
        val dataStoreManager = DataStoreManager(this, cryptoManager)
        val db = AppDatabase.getDatabase(this)
        val authRepo = AuthRepository.getInstance(this)
        val messageRepo = MessageRepository.getInstance(this, db.messageDao())

        setContent {
            val scope = rememberCoroutineScope()
            var contacts by remember { mutableStateOf<List<GroupContact>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var statusMessage by remember { mutableStateOf("") }

            // Load real group members
            LaunchedEffect(groupId) {
                try {
                    val token = dataStoreManager.tokenFlow.first()
                    val myUserId = token?.let { extractUserIdFromJwt(it) } ?: ""

                    if (groupId.isNotBlank()) {
                        val rows = messageRepo.getGroupMembersWithCache(groupId)
                        val members = rows.mapNotNull { row ->
                            // Skip current user
                            if (row.userId == myUserId) return@mapNotNull null
                            authRepo.getUserById(row.userId).getOrNull()?.let { user ->
                                GroupContact(
                                    userId = user.id.toString(),
                                    name = user.username
                                )
                            } ?: GroupContact(
                                userId = row.userId,
                                name = row.username.ifBlank { row.userId.take(8) }
                            )
                        }
                        contacts = members
                    }
                } catch (e: Exception) {
                    statusMessage = "Failed to load members"
                } finally {
                    isLoading = false
                }
            }

            val selectedContacts = contacts.filter { it.isSelected }

            GroupCallSetupScreen(
                contacts = contacts,
                selectedCount = selectedContacts.size,
                statusMessage = statusMessage,
                isLoading = isLoading,
                onContactToggle = { contact ->
                    contacts = contacts.map {
                        if (it.userId == contact.userId) it.copy(isSelected = !it.isSelected)
                        else it
                    }
                },
                onBack = { finish() },
                onStartGroupCall = {
                    if (selectedContacts.isEmpty()) {
                        statusMessage = "Select at least 1 contact"
                        return@GroupCallSetupScreen
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            val token = dataStoreManager.tokenFlow.first()
                            val currentUserId = token?.let {
                                extractUserIdFromJwt(it)
                            } ?: dataStoreManager.usernameFlow.first()

                            val response = CallingApiClient.service.createGroupRoom(currentUserId)
                            if (response.isSuccessful) {
                                val room = response.body()
                                val roomId = room?.roomId ?: ""

                                val intent = Intent(this@GroupCallSetup, GroupCallRoom::class.java)
                                intent.putExtra("roomId", roomId)
                                intent.putExtra("currentUserId", currentUserId)
                                intent.putStringArrayListExtra(
                                    "participants",
                                    ArrayList(selectedContacts.map { it.name })
                                )
                                startActivity(intent)
                                finish()
                            } else {
                                statusMessage = "Failed to create room"
                            }
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
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
private fun GroupCallSetupScreen(
    contacts: List<GroupContact>,
    selectedCount: Int,
    statusMessage: String,
    isLoading: Boolean,
    onContactToggle: (GroupContact) -> Unit,
    onBack: () -> Unit,
    onStartGroupCall: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(currentScreen = "contacts", onNavigate = onNavigate)
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(R.drawable.contacts_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            StrokeTitle(
                text = "GROUP CALL",
                fontFamily = Vampiro,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 22.dp)
            )

            BackButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 75.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedCount > 0) {
                    StrokeText(
                        text = "$selectedCount selected",
                        fontFamily = Tapestry,
                        fontSize = 16.sp,
                        fillColor = Color.White,
                        strokeColor = Color(0xFF0066FF),
                        strokeWidth = 1f,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        color = Color(0xFFFF0099),
                        fontFamily = Tapestry,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        StrokeText(
                            text = "No other members in this group",
                            fontFamily = Tapestry,
                            fontSize = 16.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF0066FF),
                            strokeWidth = 1f
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(contacts, key = { it.userId }) { contact ->
                            GroupContactRow(
                                contact = contact,
                                onClick = { onContactToggle(contact) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(width = 127.dp, height = 76.dp)
                        .padding(bottom = 10.dp)
                        .clickable { if (!isLoading) onStartGroupCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.calls_make_call_btn),
                        contentDescription = "Start Group Call",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    if (isLoading) {
                        Text(text = "...", color = Color.White, fontFamily = Vampiro, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun GroupContactRow(
    contact: GroupContact,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(
                if (contact.isSelected) R.drawable.calls_anwsered_bg
                else R.drawable.friend_contact_bg
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.chats_icon),
                contentDescription = null,
                modifier = Modifier.size(width = 60.dp, height = 56.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.size(10.dp))

            StrokeText(
                text = contact.name,
                fontFamily = Vampiro,
                fontSize = 24.sp,
                fillColor = Color.White,
                strokeColor = if (contact.isSelected) Color(0xFF0066FF) else Color(0xFF002BFF),
                strokeWidth = 1f,
                modifier = Modifier.weight(1f)
            )

            if (contact.isSelected) {
                Image(
                    painter = painterResource(R.drawable.calls_anwsered_icon),
                    contentDescription = "Selected",
                    modifier = Modifier.size(30.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}