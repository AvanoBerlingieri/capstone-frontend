package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.FriendRepository
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.InitializeSocket
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import java.util.UUID


private data class UiContactItem(
    val friendId: String,
    val name: String,
    val email: String
)

class Contacts : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContactsScreen(
                onBack = { finish() },

                onContactClick = { contact ->
                    val intent = Intent(this, ContactProfile::class.java)
                    intent.putExtra("contactName", contact.name)
                    intent.putExtra("contactEmail", contact.email)
                    intent.putExtra("friendId", contact.friendId)
                    startActivity(intent)
                },

                onGroupCall = {
                    startActivity(Intent(this, GroupCallSetup::class.java))
                },

                onNavigate = { destination ->
                    when (destination) {
                        "home" -> startActivity(Intent(this, Home::class.java))
                        "calls" -> startActivity(Intent(this, Call::class.java))
                        "chats" -> startActivity(Intent(this, Chat::class.java))
                        "profile" -> startActivity(Intent(this, Profile::class.java))
                        "communities" -> startActivity(Intent(this, Community::class.java))
                        "contacts" -> {}
                    }
                }
            )
        }
    }
}

@Composable
private fun ContactsScreen(
    onBack: () -> Unit,
    onContactClick: (UiContactItem) -> Unit,
    onGroupCall: () -> Unit,
    onNavigate: (String) -> Unit
) {

    InitializeSocket()

    val context = LocalContext.current
    val dsManager = remember { DataStoreManager.getInstance(context) }
    val friendRepo = remember { FriendRepository.getInstance(context) }
    val authRepo = remember { AuthRepository.getInstance(context) }

    var contacts by remember { mutableStateOf<List<UiContactItem>>(emptyList()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val token = dsManager.tokenFlow.first()
        android.util.Log.d("CONTACTS", "Token: $token")
        android.util.Log.d("CONTACTS", "UserId: ${token?.let { extractUserIdFromJwt(it) }}")
        loadError = null
        try {
            val token = dsManager.tokenFlow.first()
            val userId = token?.let { extractUserIdFromJwt(it) }

            if (userId.isNullOrBlank()) {
                loadError = "Sign in required to load contacts."
                return@LaunchedEffect
            }

            friendRepo.getAllFriends(userId)
                .onSuccess { friendIds ->
                    val resolved = mutableListOf<UiContactItem>()

                    friendIds.forEach { fid ->
                        authRepo.getUserById(UUID.fromString(fid))
                            .onSuccess { user ->
                                resolved.add(
                                    UiContactItem(
                                        friendId = fid,
                                        name = user.username,
                                        email = user.email
                                    )
                                )
                            }
                            .onFailure {
                                resolved.add(
                                    UiContactItem(
                                        friendId = fid,
                                        name = "Unknown user",
                                        email = ""
                                    )
                                )
                            }
                    }
                    contacts = resolved
                }
                .onFailure { e ->
                    Log.e("Contacts", "getAllFriends failed", e)
                    loadError = e.message ?: "Failed to load contacts."
                    contacts = emptyList()
                }
        } catch (e: Exception) {
            Log.e("Contacts", "Failed to load friends", e)
            loadError = e.message ?: "Failed to load contacts."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentScreen = "contacts",
                onNavigate = onNavigate
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(R.drawable.contacts_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                ThemeManager.backgroundGradient
                            )
                        )
                )
            }

            StrokeTitle(
                text = "CONTACTS",
                fontFamily = ThemeManager.fontFamily,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 75.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Loading contacts…",
                                color = Color.White
                            )
                        }
                    }
                    loadError != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            StrokeText(
                                text = loadError!!,
                                fontFamily = ThemeManager.fontFamily,
                                fontSize = 16.sp,
                                fillColor = Color.White,
                                strokeColor = Color(0xFF002BFF),
                                strokeWidth = 1f
                            )
                        }
                    }
                    contacts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            StrokeText(
                                text = "No friends yet.",
                                fontFamily = ThemeManager.fontFamily,
                                fontSize = 18.sp,
                                fillColor = Color.White,
                                strokeColor = Color(0xFF002BFF),
                                strokeWidth = 1f
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 0.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(contacts, key = { it.friendId }) { contact ->
                                ContactRow(
                                    contact = contact,
                                    onClick = { onContactClick(contact) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(R.drawable.new_contact_btn),
                    contentDescription = null,
                    modifier = Modifier.size(127.dp, 76.dp)
                )

                Image(
                    painter = painterResource(R.drawable.calls_make_call_btn),
                    contentDescription = "Group Call",
                    modifier = Modifier
                        .size(127.dp, 76.dp)
                        .clickable { onGroupCall() }
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: UiContactItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() }
            .padding(horizontal = 0.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.friend_contact_bg),
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
                fontFamily = ThemeManager.fontFamily,
                fontSize = 24.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF002BFF),
                strokeWidth = 1f
            )
        }
    }
}
