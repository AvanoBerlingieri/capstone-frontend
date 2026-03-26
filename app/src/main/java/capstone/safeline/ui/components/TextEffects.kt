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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import capstone.safeline.ui.theme.ThemeManager

@Composable
fun StrokeTitle(
    text: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
    strokeWidth: Float = ThemeManager.titleStrokeWidth
) {

    val strokeBrush = Brush.linearGradient(
        listOf(
            ThemeManager.titleStroke,
            ThemeManager.titleStroke
        )
    )

    Box(modifier = modifier) {

        Text(
            text = text,
            fontFamily = fontFamily,
            fontWeight = if (ThemeManager.currentFont == ThemeManager.FontType.DEFAULT)
                FontWeight.Normal
            else
                FontWeight.Bold,
            fontSize = fontSize,
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(Color.Black, blurRadius = 6f)
            ),
            textAlign = TextAlign.Center
        )

        if (strokeWidth > 0f) {
            Text(
                text = text,
                fontFamily = fontFamily,
                fontWeight = if (ThemeManager.currentFont == ThemeManager.FontType.DEFAULT)
                    FontWeight.Normal
                else
                    FontWeight.Bold,
                fontSize = fontSize,
                color = Color.Transparent,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = strokeBrush,
                    drawStyle = Stroke(strokeWidth)
                )
            )
        }
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

    val strokeBrush = Brush.linearGradient(
        listOf(strokeColor, strokeColor)
    )

    Box(modifier = modifier) {

        Text(
            text = text,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = fillColor,
            textAlign = textAlign,
            lineHeight = lineHeight ?: TextUnit.Unspecified
        )

        if (strokeWidth > 0f) {
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

        if (strokeWidth > 0f) {
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
}