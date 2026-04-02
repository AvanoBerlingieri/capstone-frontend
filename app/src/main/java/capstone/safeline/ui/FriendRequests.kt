package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


private val Tapestry = FontFamily(Font(R.font.tapestry_regular))

private data class UiFriendRequest(
    val requesterId: String,
    val name: String,
    val email: String
)

class FriendRequests : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FriendRequestsScreen(
                onBack = { finish() },
                onOpenProfile = { request ->
                    val intent = Intent(this, ContactProfile::class.java)
                    intent.putExtra("contactName", request.name)
                    intent.putExtra("contactEmail", request.email)
                    startActivity(intent)
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
private fun FriendRequestsScreen(
    onBack: () -> Unit,
    onOpenProfile: (UiFriendRequest) -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val dsManager = remember { DataStoreManager(context, CryptoManager()) }
    val friendRepo = remember { FriendRepository(ApiClientFriends.provideService(context, dsManager)) }
    val authRepo = remember { AuthRepository(dsManager, ApiClientAuth.provideApiService(context, dsManager)) }
    val scope = rememberCoroutineScope()

    val requests = remember { mutableStateListOf<UiFriendRequest>() }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var showSearchDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var addFriendStatus by remember { mutableStateOf<String?>(null) }
    var searchedDbUser by remember { mutableStateOf<UiFriendRequest?>(null) }
    val dropdownWidth = (LocalConfiguration.current.screenWidthDp - 20).dp

    LaunchedEffect(Unit) {
        requests.clear()
        loadError = null

        val token = dsManager.tokenFlow.first()
        val userId = token?.let { extractUserIdFromJwt(it) }
        currentUserId = userId

        if (userId.isNullOrBlank()) {
            loadError = "Missing user id for pending requests."
            return@LaunchedEffect
        }

        friendRepo
            .getPendingRequests(userId)
            .onSuccess { pendingUserIds ->
                pendingUserIds.forEach { pendingId ->
                    authRepo.getUserById(java.util.UUID.fromString(pendingId))
                        .onSuccess { user ->
                            requests.add(
                                UiFriendRequest(
                                    requesterId = pendingId,
                                    name = user.username,
                                    email = user.email
                                )
                            )
                        }
                        .onFailure {
                            requests.add(
                                UiFriendRequest(
                                    requesterId = pendingId,
                                    name = "Unknown user",
                                    email = "email unavailable"
                                )
                            )
                        }
                }
            }
            .onFailure { e ->
                val raw = e.message.orEmpty()
                loadError = when {
                    raw.contains(": 403") || raw.contains(": 403 ") ->
                        "Access denied (403). The friends service rejected this request—" +
                            "often the user id in your token must match the account on that server."
                    raw.contains(": 401") ->
                        "Session expired. Please sign in again."
                    else -> raw.ifBlank { "Failed to load pending requests." }
                }
                Log.e("FriendRequests", "Pending requests load failed: $loadError", e)
            }
    }

    Scaffold(
        topBar = {},
        bottomBar = {
            BottomNavBar(
                currentScreen = "home",
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
                    painter = painterResource(R.drawable.requests_bg),
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

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(70.dp)
            ) {

                if (ThemeManager.currentTheme != ThemeManager.Theme.CLASSIC) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    ThemeManager.headerGradient
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(ThemeManager.topBarStroke)
                    )
                }

                StrokeTitle(
                    text = "FRIENDS REQUESTS",
                    fontFamily = ThemeManager.fontFamily,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-15).dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 12.dp)
            ) {
                SearchToggleButton(
                    expanded = showSearchDropdown,
                    onClick = { showSearchDropdown = !showSearchDropdown }
                )

                DropdownMenu(
                    expanded = showSearchDropdown,
                    onDismissRequest = { showSearchDropdown = false },
                    modifier = Modifier
                        .width(dropdownWidth)
                        .background(
                            Brush.verticalGradient(ThemeManager.buttonGradient)
                        )
                ) {
                    Text(
                        text = "Search users database",
                        color = Color.White,
                        fontFamily = ThemeManager.fontFamily,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        singleLine = true,
                        label = { Text("Username", color = Color.White) }
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                val username = searchQuery.trim()

                                if (username.isBlank()) {
                                    addFriendStatus = "Enter a username."
                                    searchedDbUser = null
                                    return@launch
                                }

                                authRepo.getIdByUsername(username)
                                    .onSuccess { foundId ->
                                        Log.d(
                                            "FriendRequests",
                                            "getIdByUsername success: username=$username id=$foundId"
                                        )
                                        authRepo.getUserById(java.util.UUID.fromString(foundId))
                                            .onSuccess { user ->
                                                Log.d(
                                                    "FriendRequests",
                                                    "getUserById success: id=$foundId username=${user.username}"
                                                )
                                                searchedDbUser = UiFriendRequest(
                                                    requesterId = foundId,
                                                    name = user.username,
                                                    email = user.email
                                                )
                                                addFriendStatus = "User found."
                                            }
                                            .onFailure {
                                                Log.e(
                                                    "FriendRequests",
                                                    "getUserById failed for id=$foundId; using fallback display"
                                                )
                                                searchedDbUser = UiFriendRequest(
                                                    requesterId = foundId,
                                                    name = username,
                                                    email = "email unavailable"
                                                )
                                                addFriendStatus = "User id found."
                                            }
                                    }
                                    .onFailure { e ->
                                        Log.e(
                                            "FriendRequests",
                                            "getIdByUsername failed: username=$username message=${e.message}",
                                            e
                                        )
                                        searchedDbUser = null
                                        addFriendStatus = e.message ?: "Username not found."
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Search user")
                    }

                    searchedDbUser?.let { foundUser ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = foundUser.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = foundUser.email,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            onClick = { onOpenProfile(foundUser) }
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    val senderId = currentUserId
                                    if (senderId.isNullOrBlank()) {
                                        addFriendStatus = "Missing current user id."
                                        return@launch
                                    }
                                    if (foundUser.requesterId == senderId) {
                                        addFriendStatus = "You cannot add yourself."
                                        return@launch
                                    }

                                    val sent = friendRepo.sendFriendRequest(
                                        senderId = senderId,
                                        receiverId = foundUser.requesterId
                                    )
                                    addFriendStatus = if (sent) {
                                        "Friend request sent to ${foundUser.name}."
                                    } else {
                                        "Failed to send friend request."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Add friend")
                        }
                    }

                    addFriendStatus?.let { status ->
                        Text(
                            text = status,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FriendRequestsHeader(count = requests.size)

                if (loadError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = loadError ?: "", color = Color.White, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End,
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    items(requests, key = { it.requesterId }) { request ->
                        FriendRequestRow(
                            request = request,
                            onOpenProfile = { onOpenProfile(request) },
                            onAccept = {
                                scope.launch {
                                    val receiverId = currentUserId
                                    if (receiverId.isNullOrBlank()) {
                                        loadError = "Missing current user id."
                                        return@launch
                                    }
                                    // Match backend/Postman pattern first: current user as userId.
                                    val successDirect = friendRepo.handleFriendRequest(
                                        senderId = receiverId,
                                        receiverId = request.requesterId,
                                        status = "ACCEPTED"
                                    )

                                    val success = if (successDirect) {
                                        true
                                    } else {
                                        friendRepo.handleFriendRequest(
                                            senderId = request.requesterId,
                                            receiverId = receiverId,
                                            status = "ACCEPTED"
                                        )
                                    }
                                    if (success) requests.remove(request)
                                    else loadError = "Failed to accept request."
                                }
                            },
                            onDeny = {
                                scope.launch {
                                    val receiverId = currentUserId
                                    if (receiverId.isNullOrBlank()) {
                                        loadError = "Missing current user id."
                                        return@launch
                                    }
                                    // Match backend
                                    val successDirect = friendRepo.handleFriendRequest(
                                        senderId = receiverId,
                                        receiverId = request.requesterId,
                                        status = "DECLINED"
                                    )
                                    val success = if (successDirect) {
                                        true
                                    } else {
                                        friendRepo.handleFriendRequest(
                                            senderId = request.requesterId,
                                            receiverId = receiverId,
                                            status = "DECLINED"
                                        )
                                    }
                                    if (success) requests.remove(request)
                                    else loadError = "Failed to decline request."
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchToggleButton(
    expanded: Boolean,
    onClick: () -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(ThemeManager.buttonGradient),
                shape = shape
            )
            .then(
                ThemeManager.buttonStroke?.let {
                    Modifier.border(1.dp, it, shape)
                } ?: Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (expanded) "Close" else "Add",
            color = Color.White,
            fontFamily = ThemeManager.fontFamily,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun FriendRequestsHeader(count: Int) {
    Box(
        modifier = Modifier
            .width(330.dp)
            .height(69.dp),
        contentAlignment = Alignment.Center
    ) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.request_new),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

        } else {

            val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            ThemeManager.buttonGradient
                        ),
                        shape = shape
                    )
                    .then(
                        ThemeManager.buttonStroke?.let {
                            Modifier.border(
                                1.dp,
                                it,
                                shape
                            )
                        } ?: Modifier
                    )
            )

        }

        Text(
            text = "You Have $count New friend Requests",
            fontFamily = Tapestry,
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FriendRequestRow(
    request: UiFriendRequest,
    onOpenProfile: () -> Unit,
    onAccept: () -> Unit,
    onDeny: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(330.dp)
            .height(69.dp)
    ) {
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.requst_new_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

        } else {

            val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            ThemeManager.buttonGradient
                        ),
                        shape = shape
                    )
                    .then(
                        ThemeManager.buttonStroke?.let {
                            Modifier.border(
                                1.dp,
                                it,
                                shape
                            )
                        } ?: Modifier
                    )
            )

        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxSize()
                    .padding(end = 25.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.request_deny_btn),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onDeny() },
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(R.drawable.request_accept_btn),
                        contentDescription = null,
                        modifier = Modifier
                            .size(33.dp)
                            .clickable { onAccept() },
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(end = 12.dp)
                    .clickable { onOpenProfile() },
                contentAlignment = Alignment.Center
            ) {
                StrokeText(
                    text = request.name,
                    fontFamily = ThemeManager.fontFamily,
                    fontSize = 24.sp,
                    fillColor = Color.White,
                    strokeColor = Color(0xFF009DFF),
                    strokeWidth = 1f,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
