package capstone.safeline.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun GradientStrokeText(
    text: String,
    fontSize: TextUnit,
    fontFamily: FontFamily
) {
    Box {

        // Stroke layer
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF002BFF),
                        Color(0xFFB30FFF)
                    )
                ),
                drawStyle = Stroke(width = 6f)
            )
        )

        // Fill layer
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            color = Color.White
        )
    }
}
