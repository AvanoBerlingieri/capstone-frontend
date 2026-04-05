package capstone.safeline.ui.chatting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import capstone.safeline.R
import capstone.safeline.apis.dto.auth.GetUserByIdResponse
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.repository.AuthRepository
import capstone.safeline.data.repository.FriendRepository
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.ui.Home
import capstone.safeline.ui.calling.Call
import capstone.safeline.ui.community.Community
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.friends.Contacts
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InviteUsersPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""

        setContent {
            // Initialize Repositories
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val authRepo = AuthRepository.getInstance(context)
            val friendRepo = FriendRepository.getInstance(context)
            val messageRepo = MessageRepository.getInstance(context, db.messageDao())

            // Initialize ViewModel with Factory
            val vm: InviteUsersViewModel = viewModel(
                factory = InviteUsersViewModelFactory(friendRepo, authRepo, messageRepo)
            )

            InviteUsersScreen(
                groupId = groupId,
                viewModel = vm,
                onBack = { finish() },
                onNavigate = { destination ->
                    val intent = when (destination) {
                        "home" -> Intent(this, Home::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) }
                        "calls" -> Intent(this, Call::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "chats" -> Intent(this, Chat::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "profile" -> Intent(this, Profile::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "communities" -> Intent(this, Community::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        "contacts" -> Intent(this, Contacts::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                        else -> null
                    }
                    intent?.let { startActivity(it) }
                }
            )
        }
    }
}

@Composable
fun InviteUsersScreen(
    groupId: String,
    viewModel: InviteUsersViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load friends list on entry
    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    Scaffold(
        bottomBar = { BottomNavBar(currentScreen = "chats", onNavigate = onNavigate) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(painterResource(R.drawable.dm_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(ThemeManager.backgroundGradient)))
            }

            BackButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))

            Box(modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(70.dp), contentAlignment = Alignment.Center) {
                StrokeText("ADD USERS", ThemeManager.fontFamily, 28.sp, Color.White, Color(0xFFB30FFF), 2f)
            }

            Column(modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(ThemeManager.groupCardGradient))
                    .padding(20.dp)) {

                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(viewModel.friendList) { friend ->
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                // User Nameplate
                                Box(modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp), contentAlignment = Alignment.CenterStart) {
                                    Image(painterResource(R.drawable.group_user_item), null, Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
                                    Text(text = friend.username, color = Color.White, fontFamily = ThemeManager.fontFamily, modifier = Modifier.padding(start = 16.dp), fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Invite Button
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(32.dp)
                                        .clickable {
                                            viewModel.inviteUser(groupId, friend.id) {
                                                Toast.makeText(
                                                    context,
                                                    "Invited ${friend.username}!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(painterResource(R.drawable.add_users_btn), null, Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
                                    Text("Invite", color = Color.White, fontFamily = ThemeManager.fontFamily, fontSize = 12.sp)
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

class InviteUsersViewModel(
    private val friendRepo: FriendRepository,
    private val authRepo: AuthRepository,
    private val messageRepo: MessageRepository
) : ViewModel() {

    var friendList by mutableStateOf<List<GetUserByIdResponse>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun loadFriends() {
        viewModelScope.launch {
            isLoading = true
            val myId = authRepo.userIdFlow.first() ?: return@launch
            val result = friendRepo.getAllFriends(myId)

            result.onSuccess { ids ->
                val details = ids.mapNotNull { id ->
                    authRepo.getUserById(id).getOrNull()
                }
                friendList = details
            }
            isLoading = false
        }
    }

    fun inviteUser(groupId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = messageRepo.addUserToGroup(groupId, userId)
            if (success) onSuccess()
        }
    }
}

class InviteUsersViewModelFactory(
    private val friendRepo: FriendRepository,
    private val authRepo: AuthRepository,
    private val messageRepo: MessageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InviteUsersViewModel(friendRepo, authRepo, messageRepo) as T
    }
}