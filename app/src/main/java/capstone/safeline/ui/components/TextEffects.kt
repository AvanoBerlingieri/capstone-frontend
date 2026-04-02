package capstone.safeline.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun StrokeTitle(
    text: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
    strokeWidth: Float = 4f
) {
    val strokeBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF002BFF), Color(0xFFB30FFF))
    )

    Box(modifier = modifier) {
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(Color.Black, blurRadius = 6f)
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = Color.Transparent,
            style = TextStyle(
                brush = strokeBrush,
                drawStyle = Stroke(strokeWidth)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StrokeText(
    text: String,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit? = null
) {
    val strokeBrush = Brush.linearGradient(listOf(strokeColor, strokeColor))

    Box(modifier = modifier) {
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = fillColor,
            textAlign = textAlign,
            lineHeight = lineHeight ?: TextUnit.Unspecified
        )

        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = Color.Transparent,
            textAlign = textAlign,
            lineHeight = lineHeight ?: TextUnit.Unspecified,
            style = TextStyle(
                brush = strokeBrush,
                drawStyle = Stroke(strokeWidth)
            )
        )
    }
}

@Composable
fun StrokeText(
    text: String,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    fillColor: Color,
    strokeColors: List<Color>,
    strokeWidth: Float,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit? = null
) {
    val strokeBrush = Brush.linearGradient(strokeColors)

    Box(modifier = modifier) {
        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = fillColor,
            textAlign = textAlign,
            lineHeight = lineHeight ?: TextUnit.Unspecified
        )

        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = Color.Transparent,
            textAlign = textAlign,
            lineHeight = lineHeight ?: TextUnit.Unspecified,
            style = TextStyle(
                brush = strokeBrush,
                drawStyle = Stroke(strokeWidth)
            )
        )
    }
}

