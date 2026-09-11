package dev.vibebridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val scheme = darkColorScheme(
    primary = ButtonPrimary,
    onPrimary = Text,
    primaryContainer = SurfaceHigh,
    onPrimaryContainer = Text,
    secondary = TextDim,
    onSecondary = Bg,
    background = Bg,
    onBackground = Text,
    surface = Surface,
    onSurface = Text,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextDim,
    outline = Border,
    outlineVariant = Border,
    error = Danger,
    onError = PureWhite,
    errorContainer = SurfaceHigh,
    onErrorContainer = Danger
)

@Composable
fun VbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = VbType, shapes = VbShapes, content = content)
}
