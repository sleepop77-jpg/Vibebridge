package dev.vibebridge.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.PureWhite
import dev.vibebridge.ui.theme.Warning
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger == 0) return
    key(trigger) {
        val p by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(1100, easing = FastOutSlowInEasing),
            label = "burst"
        )
        Canvas(modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.35f
            val maxR = min(size.width, size.height) * 0.45f
            for (i in 0 until 26) {
                val ang = (i * (360 / 26)).toFloat() * Math.PI.toFloat() / 180f
                val d = maxR * p
                val x = cx + cos(ang) * d
                val y = cy + sin(ang) * d + 60f * p * p
                val color = when (i % 3) {
                    0 -> Accent
                    1 -> PureWhite
                    else -> Warning
                }
                drawCircle(
                    color.copy(alpha = (1f - p).coerceIn(0f, 1f)),
                    radius = 3.5f - 2f * p,
                    center = Offset(x, y)
                )
            }
        }
    }
}
