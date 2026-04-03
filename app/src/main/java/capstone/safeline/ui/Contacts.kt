package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.extractUserIdFromJwt
import capstone.safeline.apis.network.ApiClientAuth
import capstone.safeline.apis.network.ApiClientFriends
import capstone.safeline.data.local.DataStoreManager
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.FriendRepository
import capstone.safeline.data.security.CryptoManager
import capstone.safeline.ui.components.*
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
    val context = LocalContext.current
    val dsManager = remember { DataStoreManager(context, CryptoManager()) }
    val friendRepo = remember { FriendRepository(ApiClientFriends.provideService(context, dsManager)) }
    val authRepo = remember { AuthRepository(dsManager, ApiClientAuth.provideApiService(context, dsManager)) }

    var contacts by remember { mutableStateOf<List<UiContactItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = dsManager.tokenFlow.first()
            val userId = token?.let { extractUserIdFromJwt(it) }

            if (userId.isNullOrBlank()) {
                loadError = "Not logged in"
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
                    }

                    contacts = resolved
                }
                .onFailure {
                    loadError = "Failed to load contacts"
                }

        } catch (e: Exception) {
            Log.e("Contacts", "Error", e)
            loadError = e.message
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
                        .background(Brush.verticalGradient(ThemeManager.backgroundGradient))
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

                if (isLoading) {
                    Text("Loading...", color = Color.White)
                } else if (loadError != null) {
                    Text(loadError!!, color = Color.Red)
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(contacts) { contact ->
                            ContactRow(contact) {
                                onContactClick(contact)
                            }
                        }
                    }
                }

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
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.chats_icon),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            StrokeText(
                text = contact.name,
                fontFamily = ThemeManager.fontFamily,
                fontSize = 24.sp,
                fillColor = Color.White,
                strokeColor = Color.Blue,
                strokeWidth = 1f
            )
        }
    }
}