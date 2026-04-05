package capstone.safeline.ui.chatting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import capstone.safeline.ui.Contacts
import capstone.safeline.ui.profile.Profile
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.launch

class GroupSettingsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupId = intent.getStringExtra("groupId") ?: ""
        val groupName = intent.getStringExtra("groupName") ?: ""

        setContent {
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)
            val authRepo = AuthRepository.getInstance(context)
            val friendsRepo = FriendRepository.getInstance(context)
            val messageRepo = MessageRepository.getInstance(context, db.messageDao())

            val vm: GroupSettingsViewModel = viewModel(
                factory = GroupSettingsViewModelFactory(authRepo, friendsRepo, messageRepo)
            )

            GroupSettingsScreen(
                groupId = groupId,
                initialGroupName = groupName,
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
fun GroupSettingsScreen(
    groupId: String,
    initialGroupName: String,
    viewModel: GroupSettingsViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var groupNameInput by remember(groupId, initialGroupName) { mutableStateOf(initialGroupName) }

    val activity = context as? ComponentActivity
    DisposableEffect(activity, groupId) {
        val act = activity ?: return@DisposableEffect onDispose { }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadGroupMembers(groupId)
            }
        }
        act.lifecycle.addObserver(observer)
        onDispose { act.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = { BottomNavBar(currentScreen = "chats", onNavigate = onNavigate) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background
            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(painterResource(R.drawable.dm_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(ThemeManager.backgroundGradient)))
            }

            BackButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart))

            // Header and Body
            Column(
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.width(360.dp).fillMaxHeight(0.85f).clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(ThemeManager.groupCardGradient)).padding(20.dp)) {

                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        StrokeText("GROUP NAME", ThemeManager.fontFamily, 20.sp, Color.White, Color(0xFF193DEF), 1f)

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f).height(44.dp)) {
                                Image(painterResource(R.drawable.group_name_input), null, Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
                                TextField(
                                    value = groupNameInput,
                                    onValueChange = { groupNameInput = it },
                                    modifier = Modifier.fillMaxSize(),
                                    textStyle = TextStyle(color = Color.White, fontFamily = ThemeManager.fontFamily),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(36.dp)
                                    .clickable {
                                        val name = groupNameInput.trim()
                                        if (name.isEmpty()) {
                                            Toast.makeText(context, "Enter a name", Toast.LENGTH_SHORT).show()
                                            return@clickable
                                        }
                                        viewModel.renameGroup(groupId, name) { ok ->
                                            Toast.makeText(
                                                context,
                                                if (ok) "Group name updated" else "Could not update name",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(painterResource(R.drawable.add_users_btn), null, Modifier.matchParentSize())
                                Text("Change", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        StrokeText("MEMBERS", ThemeManager.fontFamily, 20.sp, Color.White, Color(0xFF193DEF), 1f)

                        Box(modifier = Modifier.fillMaxWidth().height(320.dp).background(Brush.horizontalGradient(ThemeManager.groupInnerGradient)).border(1.dp, ThemeManager.groupStroke).padding(10.dp)) {
                            Column {
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(viewModel.memberList) { user ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.weight(1f).height(44.dp), contentAlignment = Alignment.CenterStart) {
                                                Image(painterResource(R.drawable.group_user_item), null, Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
                                                Text(user.username, color = Color.White, modifier = Modifier.padding(start = 16.dp))
                                            }
                                        }
                                    }
                                }
                                // Add Users Button
                                Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(width = 146.dp, height = 47.dp)
                                    .clickable {
                                        val intent = Intent(context, InviteUsersPage::class.java).apply {
                                            putExtra("groupId", groupId)
                                        }
                                        context.startActivity(intent)
                                    }, contentAlignment = Alignment.Center) {
                                    Image(painterResource(R.drawable.add_users_btn), null, Modifier.matchParentSize())
                                    Text("Add Users", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Leave Group Button
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp).size(220.dp, 35.dp)
                .clickable {
                    viewModel.leaveGroup(groupId) {
                        Toast.makeText(context, "Left Group", Toast.LENGTH_SHORT).show()
                        val act = context as Activity
                        act.startActivity(
                            Intent(act, Chat::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                                )
                            }
                        )
                        act.finish()
                    }
                }, contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.delete_server_btn), null, Modifier.matchParentSize())
                Text("LEAVE GROUP", color = Color.White, fontFamily = ThemeManager.fontFamily)
            }
        }
    }
}

class GroupSettingsViewModel(
    private val authRepo: AuthRepository,
    private val friendsRepo: FriendRepository,
    private val messageRepo: MessageRepository
) : ViewModel() {

    var memberList by mutableStateOf<List<GetUserByIdResponse>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                if (groupId.isBlank()) {
                    memberList = emptyList()
                    return@launch
                }

                val rows = messageRepo.getGroupMembersWithCache(groupId)
                memberList = rows.map { row ->
                    authRepo.getUserById(row.userId).getOrNull()?.let { profile ->
                        GetUserByIdResponse(
                            id = profile.id,
                            username = profile.username,
                            email = profile.email.orEmpty()
                        )
                    } ?: GetUserByIdResponse(
                        id = row.userId,
                        username = row.username.ifBlank { row.userId.take(8).ifEmpty { "?" } },
                        email = ""
                    )
                }
            } catch (e: Exception) {
                Log.d("messaging", e.toString())
            } finally {
                isLoading = false
            }
        }
    }

    fun renameGroup(groupId: String, newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = messageRepo.renameGroup(groupId, newName)
            onResult(ok)
        }
    }

    fun leaveGroup(groupId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val success = messageRepo.leaveGroup(groupId)
            if (success) onComplete()
        }
    }
    fun deleteGroup(groupId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (messageRepo.deleteGroup(groupId)) {
                onSuccess()
            }
        }
    }
}

class GroupSettingsViewModelFactory(
    private val authRepo: AuthRepository,
    private val friendsRepo: FriendRepository,
    private val messageRepo: MessageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupSettingsViewModel(authRepo, friendsRepo, messageRepo) as T
    }
}