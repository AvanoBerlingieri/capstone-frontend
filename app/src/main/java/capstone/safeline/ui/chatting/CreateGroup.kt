package capstone.safeline.ui.chatting

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.apis.dto.messaging.CreateGroupRequest
import capstone.safeline.apis.network.WebSocketManager
import capstone.safeline.data.local.AppDatabase
import capstone.safeline.data.repository.MessageRepository
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CreateGroup : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageRepo =
            MessageRepository.getInstance(this, AppDatabase.getDatabase(this).messageDao())

        setContent {
            val scope = rememberCoroutineScope()
            val newGroupId = UUID.randomUUID().toString()
            CreateGroupScreen(
                onBack = { finish() },
                onConfirm = { name ->
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            messageRepo.createGroup(
                                CreateGroupRequest(
                                    groupId = newGroupId,
                                    name = name
                                )
                            )
                        }
                        result.onSuccess {
                            WebSocketManager.getInstance().subscribeToGroups(listOf(newGroupId))
                            finish()
                        }.onFailure {
                            Toast.makeText(
                                this@CreateGroup,
                                "Error: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }

    Scaffold(containerColor = Color.Transparent) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {
                Image(
                    painter = painterResource(R.drawable.add_server_bg),
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

            BackButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.horizontalGradient(
                                ThemeManager.communityCardGradient
                            )
                        )
                        .padding(20.dp)
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        StrokeText(
                            text = "GROUP NAME",
                            fontFamily = ThemeManager.fontFamily,
                            fontSize = 22.sp,
                            fillColor = Color.White,
                            strokeColor = Color(0xFF193DEF),
                            strokeWidth = 1f
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontFamily = ThemeManager.fontFamily
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(width = 140.dp, height = 45.dp)
                                .clickable {
                                    if (groupName.isNotBlank()) {
                                        onConfirm(groupName)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Confirm",
                                color = Color.White,
                                fontFamily = ThemeManager.fontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}