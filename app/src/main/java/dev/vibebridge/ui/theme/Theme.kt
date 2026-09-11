package dev.vibebridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = darkColorScheme(
    primary = Amber,
    onPrimary = PureBlack,
    primaryContainer = CardAlt,
    onPrimaryContainer = AmberHi,
    secondary = AmberHi,
    onSecondary = PureBlack,
    background = Bg,
    onBackground = Text,
    surface = Surface,
    onSurface = Text,
    surfaceVariant = Card,
    onSurfaceVariant = TextDim,
    outline = Border,
    outlineVariant = Border,
    error = Red,
    onError = PureWhite,
    errorContainer = Card,
    onErrorContainer = RedHi
)

@Composable
fun VbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = VbType, shapes = VbShapes, content = content)
}
