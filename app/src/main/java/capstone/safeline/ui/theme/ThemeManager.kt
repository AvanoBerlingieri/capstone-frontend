package capstone.safeline.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object ThemeManager {

    enum class Theme {
        CLASSIC,
        GRAY,
        BLUE_GRAY
    }

    var currentTheme by mutableStateOf(Theme.CLASSIC)

    private const val PREFS = "safeline_theme"
    private const val KEY_THEME = "current_theme"

    fun saveTheme(context: Context, theme: Theme) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        currentTheme = theme
    }

    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_THEME, Theme.CLASSIC.name)
        currentTheme = Theme.valueOf(savedTheme!!)
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
        }

    val buttonStroke: Color?
        get() = when (currentTheme) {

            Theme.CLASSIC -> null

            Theme.GRAY -> null

            Theme.BLUE_GRAY -> Color(0xFF05E6FF)

            // Theme.NEW_THEME -> Color(...)
        }

    val topBarStroke: Color
        get() = when (currentTheme) {

            Theme.CLASSIC -> Color.White

            Theme.GRAY -> Color(0xFF848484)

            Theme.BLUE_GRAY -> Color(0xFF05E6FF)

            // Theme.NEW_THEME -> Color(...)
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
        }

    val titleStroke: Color
        get() = when (currentTheme) {

            Theme.CLASSIC -> Color(0xFF002BFF)

            Theme.GRAY -> Color(0xFF0B0000)

            Theme.BLUE_GRAY -> Color(0xFF0DA2FF)
        }
}