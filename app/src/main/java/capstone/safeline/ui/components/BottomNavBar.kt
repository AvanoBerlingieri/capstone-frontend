package capstone.safeline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import capstone.safeline.R

private val NavFont = FontFamily(Font(R.font.reem_kufi_fun_regular))

@Composable
fun BottomNavBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.navbar_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.navbar_ic_home,
                label = "Home",
                iconSize = 30.dp,
                selected = currentScreen == "home",
                onClick = { onNavigate("home") }
            )

            NavBarItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.navbar_ic_chats,
                label = "Chats",
                iconSize = 30.dp,
                selected = currentScreen == "chats",
                onClick = { onNavigate("chats") }
            )

            NavBarItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.navbar_ic_calls,
                label = "Calls",
                iconSize = 30.dp,
                selected = currentScreen == "calls",
                onClick = { onNavigate("calls") }
            )

            NavBarItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.navbar_ic_communities,
                label = "Communities",
                iconSize = 40.dp,
                selected = currentScreen == "communities",
                onClick = { onNavigate("communities") }
            )

            NavBarItem(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.navbar_ic_contacts,
                label = "Contacts",
                iconSize = 35.dp,
                selected = currentScreen == "contacts",
                onClick = { onNavigate("contacts") }
            )
        }
    }
}

@Composable
private fun NavBarItem(
    modifier: Modifier,
    iconRes: Int,
    label: String,
    iconSize: Dp,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = Color(0xFF05E6FF)
    val defaultColor = Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable { onClick() }
            .padding(top = 6.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(iconSize)
            )
        }

        Box(
            modifier = Modifier
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = NavFont,
                    fontSize = 12.sp,
                    lineHeight = 12.sp,
                    color = if (selected) selectedColor else defaultColor
                )
            )
        }
    }
}


