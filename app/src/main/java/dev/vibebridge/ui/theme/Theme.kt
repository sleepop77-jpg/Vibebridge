package dev.vibebridge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val scheme = darkColorScheme(
    primary = GhBtnGreen,
    onPrimary = PureWhite,
    primaryContainer = GhBorderMuted,
    onPrimaryContainer = GhTextPrimary,
    secondary = GhAccent,
    onSecondary = PureWhite,
    background = Bg,
    onBackground = Text,
    surface = Surface,
    onSurface = Text,
    surfaceVariant = Card,
    onSurfaceVariant = TextDim,
    outline = Border,
    outlineVariant = GhBorderMuted,
    error = GhDangerBright,
    onError = PureWhite,
    errorContainer = GhBorderMuted,
    onErrorContainer = GhDangerBright
)

@Composable
fun VbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = VbType, shapes = VbShapes, content = content)
}
