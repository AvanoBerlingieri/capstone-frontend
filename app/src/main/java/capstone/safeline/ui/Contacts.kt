package capstone.safeline.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R
import capstone.safeline.ui.components.BottomNavBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.components.BackButton
import capstone.safeline.ui.theme.ThemeManager

import capstone.safeline.ui.components.TopBar
import capstone.safeline.ui.components.StrokeText
import capstone.safeline.ui.components.StrokeTitle
import capstone.safeline.ui.components.BackButton

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

private data class UiContactItem(
    val name: String
)

private val Vampiro = FontFamily(Font(R.font.vampiro_one_regular))

private data class UiContactItem(
    val name: String
)

class Contacts : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contacts = listOf(
            UiContactItem("Friend 1"),
            UiContactItem("Friend 2"),
            UiContactItem("Friend 3"),
            UiContactItem("Friend 4"),
            UiContactItem("Friend 5"),
            UiContactItem("Friend 6"),
            UiContactItem("Friend 7"),
            UiContactItem("Friend 8")
        )

        setContent {
            Scaffold(
                topBar = { TopBar(title = "Contacts") },
                bottomBar = {
                    BottomNavBar(
                        currentScreen = "contacts",
                        onNavigate = {}
                    )
            ContactsScreen(
                contacts = contacts,
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onContactClick = { contact ->
                    val intent = Intent(this, ContactProfile::class.java)
                    intent.putExtra("contactName", contact.name)
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
    contacts: List<UiContactItem>,
    onBack: () -> Unit,
    onContactClick: (UiContactItem) -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {},
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
                            .background(Color.White)
                    )
                }

                StrokeTitle(
                    text = "CONTACTS",
                    fontFamily = Vampiro,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(contacts) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Existing new contact button
                Image(
                    painter = painterResource(R.drawable.new_contact_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 127.dp, height = 76.dp)
                        .padding(bottom = 10.dp)
                )

                // NEW — Group Call button
                Image(
                    painter = painterResource(R.drawable.calls_make_call_btn),
                    contentDescription = "Group Call",
                    modifier = Modifier
                        .size(width = 127.dp, height = 76.dp)
                        .padding(bottom = 10.dp)
                        .clickable { onGroupCall() },
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(4.dp))
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
                fontFamily = Vampiro,
                fontSize = 24.sp,
                fillColor = Color.White,
                strokeColor = Color(0xFF002BFF),
                strokeWidth = 1f
            )
        }
            ContactsScreen(
                contacts = contacts,
                onBack = { startActivity(Intent(this, Home::class.java)) },
                onContactClick = { contact ->
                    val intent = Intent(this, ContactProfile::class.java)
                    intent.putExtra("contactName", contact.name)
                    startActivity(intent)
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
    contacts: List<UiContactItem>,
    onBack: () -> Unit,
    onContactClick: (UiContactItem) -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {},
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
            Image(
                painter = painterResource(R.drawable.contacts_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            HomeTitle(
                text = "CONTACTS",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 22.dp)
            )

            Image(
                painter = painterResource(R.drawable.back_btn),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 6.dp, top = 14.dp)
                    .size(width = 78.55.dp, height = 36.45.dp)
                    .clickable { onBack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 75.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(contacts) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = { onContactClick(contact) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painter = painterResource(R.drawable.new_contact_btn),
                    contentDescription = null,
                    modifier = Modifier
                        .size(width = 127.dp, height = 76.dp)
                        .padding(bottom = 10.dp)
                )
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
        if (ThemeManager.currentTheme == ThemeManager.Theme.CLASSIC) {

            Image(
                painter = painterResource(R.drawable.friend_contact_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            ThemeManager.buttonGradient
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                    )
            )

        }

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
                strokeColor = Color(0xFF002BFF),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun HomeTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val strokeBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF002BFF), Color(0xFFB30FFF))
    )

    Box(modifier = modifier) {
        Text(
            text = text,
            fontFamily = Vampiro,
            fontSize = 28.sp,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    blurRadius = 6f
                )
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = text,
            fontFamily = Vampiro,
            fontSize = 28.sp,
            color = Color.Transparent,
            style = TextStyle(
                brush = strokeBrush,
                drawStyle = Stroke(width = 4f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StrokeText(
    text: String,
    fontFamily: FontFamily,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float
) {
    Box {
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = fillColor
        )
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = Color.Transparent,
            style = TextStyle(
                brush = Brush.linearGradient(listOf(strokeColor, strokeColor)),
                drawStyle = Stroke(strokeWidth)
            )
        )
    }
}

