package dev.vibebridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val VbColors = darkColorScheme(
    primary = VbAmber,
    onPrimary = Color.Black,
    secondary = VbYellow,
    onSecondary = Color.Black,
    background = VbBg,
    onBackground = VbText,
    surface = VbSurface,
    onSurface = VbText,
    surfaceVariant = VbCard,
    onSurfaceVariant = VbDim,
    error = VbRed,
    onError = Color.White
)

private val Mono = FontFamily.Monospace

private val VbType = Typography(
    headlineSmall = TextStyle(fontFamily = Mono, fontSize = 20.sp, color = VbYellow),
    titleMedium = TextStyle(fontFamily = Mono, fontSize = 15.sp, color = VbText),
    bodyLarge = TextStyle(fontFamily = Mono, fontSize = 14.sp, color = VbText),
    bodyMedium = TextStyle(fontFamily = Mono, fontSize = 12.sp, color = VbText),
    labelLarge = TextStyle(fontFamily = Mono, fontSize = 12.sp, color = Color.Black),
    labelSmall = TextStyle(fontFamily = Mono, fontSize = 10.sp, color = VbDim)
)

@Composable
fun VbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = VbColors, typography = VbType, content = content)
}
