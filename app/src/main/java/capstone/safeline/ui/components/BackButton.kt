package capstone.safeline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import capstone.safeline.R

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.back_btn),
        contentDescription = null,
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 6.dp, top = 14.dp)
            .size(width = 78.55.dp, height = 36.45.dp)
            .clickable { onClick() }
    )
}
