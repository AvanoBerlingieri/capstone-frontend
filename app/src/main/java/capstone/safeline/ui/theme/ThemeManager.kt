package capstone.safeline.ui.theme

import androidx.compose.ui.graphics.Color

object ThemeManager {

    enum class Theme {
        CLASSIC,
        GRAY
    }

    var currentTheme = Theme.CLASSIC

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
        }

    val titleStroke: Color
        get() = when (currentTheme) {

            Theme.CLASSIC -> Color(0xFF002BFF)

            Theme.GRAY -> Color(0xFF0B0000)
        }
}