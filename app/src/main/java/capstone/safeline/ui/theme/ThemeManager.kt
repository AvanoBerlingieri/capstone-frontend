package capstone.safeline.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import capstone.safeline.R

object ThemeManager {

    enum class Theme {
        CLASSIC,
        GRAY,
        BLUE_GRAY,

        BLUE,

        LIGHT_BLUE
    }

    enum class FontType {
        DEFAULT,
        ROBOTO,
        NUNITO,
        OSWALD
    }

    var currentTheme by mutableStateOf(Theme.CLASSIC)

    var currentFont by mutableStateOf(FontType.DEFAULT)


    private const val PREFS = "safeline_theme"
    private const val KEY_THEME = "current_theme"
    private const val KEY_FONT = "current_font"

    fun saveTheme(context: Context, theme: Theme) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        currentTheme = theme
    }

    fun saveFont(context: Context, font: FontType) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FONT, font.name).apply()
        currentFont = font
    }

    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val savedTheme = prefs.getString(KEY_THEME, Theme.CLASSIC.name)
        currentTheme = Theme.valueOf(savedTheme!!)

        val savedFont = prefs.getString(KEY_FONT, FontType.DEFAULT.name)

        currentFont = try {
            FontType.valueOf(savedFont!!)
        } catch (e: Exception) {
            FontType.DEFAULT
        }
    }

    val backgroundGradient: List<Color>
        get() = when (currentTheme) {

            Theme.CLASSIC -> listOf(
                Color(0xFF000000),
                Color(0xFF0A0A2A)
            )

            Theme.GRAY -> listOf(
                Color(0xFF1E1E1E),
                Color(0xFFFFFFFF)
            )

            Theme.BLUE_GRAY -> listOf(
                Color(0xFF0066FF),
                Color(0xFF1E1E1E)
            )

            Theme.BLUE -> listOf(
                Color(0xFF009DFF),
                Color(0xFF1E1E1E)
            )

            Theme.LIGHT_BLUE -> listOf(
                Color(0xFF0066FF),
                Color(0xFF3CB4FF)
            )
        }

    val headerGradient: List<Color>
        get() = when (currentTheme) {

            Theme.CLASSIC -> listOf(
                Color(0xFF002BFF),
                Color(0xFFB30FFF)
            )

            Theme.GRAY -> listOf(
                Color(0xFF0B0000),
                Color(0xFF848484)
            )

            Theme.BLUE_GRAY -> listOf(
                Color(0xFF0251C7),
                Color(0xFF848484)
            )

            Theme.BLUE -> listOf(
                Color(0xFF0066FF),
                Color(0xFF848484)
            )

            Theme.LIGHT_BLUE -> listOf(
                Color(0xFF0251C7),
                Color(0xFF05E6FF)
            )
        }

    val buttonGradient: List<Color>
        get() = when (currentTheme) {

            Theme.CLASSIC -> listOf(
                Color(0xFF002BFF),
                Color(0xFFB30FFF)
            )

            Theme.GRAY -> listOf(
                Color(0xFF0B0000),
                Color(0xFF848484)
            )

            Theme.BLUE_GRAY -> listOf(
                Color(0xFF0251C7),
                Color(0xFF848484)
            )

            Theme.BLUE -> listOf(
                Color(0xFF0066FF),
                Color(0xFF848484)
            )

            Theme.LIGHT_BLUE -> listOf(
                Color(0xFF0251C7),
                Color(0xFF05E6FF)
            )
        }

    val buttonStroke: Color?
        get() = when (currentTheme) {

            Theme.CLASSIC -> null

            Theme.GRAY -> null

            Theme.BLUE_GRAY -> Color(0xFF05E6FF)

            Theme.BLUE -> Color.White

            Theme.LIGHT_BLUE -> Color(0xFFFFFFFF)
        }

    val topBarStroke: Color
        get() = when (currentTheme) {

            Theme.CLASSIC -> Color.White

            Theme.GRAY -> Color(0xFF848484)

            Theme.BLUE_GRAY -> Color(0xFF05E6FF)

            Theme.BLUE -> Color.White

            Theme.LIGHT_BLUE -> Color(0xFF05E6FF)
        }

    val navbarGradient: List<Color>
        get() = when (currentTheme) {

            Theme.CLASSIC -> listOf(
                Color(0xFF002BFF),
                Color(0xFFB30FFF)
            )

            Theme.GRAY -> listOf(
                Color(0xFF0B0000),
                Color(0xFF848484)
            )

            Theme.BLUE_GRAY -> listOf(
                Color(0xFF0251C7),
                Color(0xFF848484)
            )

            Theme.BLUE -> listOf(
                Color(0xFF0066FF),
                Color(0xFF848484)
            )

            Theme.LIGHT_BLUE -> listOf(
                Color(0xFF05E6FF),
                Color(0xFF0251C7)
            )
        }

    val titleStroke: Color
        get() = when (currentTheme) {

            Theme.CLASSIC -> Color(0xFF002BFF)

            Theme.GRAY -> Color(0xFF0B0000)

            Theme.BLUE_GRAY -> Color(0xFF0DA2FF)

            Theme.BLUE -> Color(0xFF0DA2FF)

            Theme.LIGHT_BLUE -> Color(0xFF0DA2FF)
        }

    val communityCardGradient: List<Color>
        get() = when (currentTheme) {

            Theme.CLASSIC -> listOf(
                Color(0x80002BFF),
                Color(0x80B30FFF)
            )

            Theme.GRAY -> listOf(
                Color(0x801E1E1E),
                Color(0x80848484)
            )

            Theme.BLUE_GRAY -> listOf(
                Color(0x800251C7),
                Color(0x80848484)
            )

            Theme.BLUE -> listOf(
                Color(0x800066FF),
                Color(0x80848484)
            )

            Theme.LIGHT_BLUE -> listOf(
                Color(0x800251C7),
                Color(0x8005E6FF)
            )
        }

    val communityInnerGradient: List<Color>
        get() = when (currentTheme) {

            Theme.CLASSIC -> listOf(
                Color(0xFF0251C7),
                Color(0xFF893990)
            )

            Theme.GRAY -> listOf(
                Color(0xFF1E1E1E),
                Color(0xFF848484)
            )

            Theme.BLUE_GRAY -> listOf(
                Color(0xFF0251C7),
                Color(0xFF848484)
            )

            Theme.BLUE -> listOf(
                Color(0xFF0066FF),
                Color(0xFF848484)
            )

            Theme.LIGHT_BLUE -> listOf(
                Color(0xFF0251C7),
                Color(0xFF05E6FF)
            )
        }

    val communityStroke: Color
        get() = when (currentTheme) {

            Theme.CLASSIC -> Color(0xFF05E6FF)

            Theme.GRAY -> Color(0xFF848484)

            Theme.BLUE_GRAY -> Color(0xFF05E6FF)

            Theme.BLUE -> Color.White

            Theme.LIGHT_BLUE -> Color(0xFF05E6FF)
        }

    val fontFamily
        get() = when (currentFont) {

            FontType.DEFAULT -> FontFamily(Font(R.font.vampiro_one_regular))

            FontType.OSWALD -> FontFamily(
                Font(R.font.oswald_regular, FontWeight.Normal),
                Font(R.font.oswald_bold, FontWeight.Bold)
            )

            FontType.ROBOTO -> FontFamily(
                Font(R.font.roboto_regular, FontWeight.Normal),
                Font(R.font.roboto_bold, FontWeight.Bold)
            )

            FontType.NUNITO -> FontFamily(
                Font(R.font.nunito_regular, FontWeight.Normal),
                Font(R.font.nunito_bold, FontWeight.Bold)
            )
        }

    val titleStrokeWidth
        get() = when (currentFont) {
            FontType.DEFAULT -> 4f
            else -> 3f
        }
}