package dev.vibebridge.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.vibebridge.ui.theme.CloudPink
import dev.vibebridge.ui.theme.CloudWhite
import dev.vibebridge.ui.theme.SkyMid
import dev.vibebridge.ui.theme.SkyPeach
import dev.vibebridge.ui.theme.SkyTop
import kotlin.math.sin

private data class Cloud(
    val bx: Float,
    val by: Float,
    val r: Float,
    val speed: Float,
    val pink: Boolean
)

private val CLOUDS = listOf(
    Cloud(0.10f, 0.14f, 0.17f, 0.05f, false),
    Cloud(0.34f, 0.08f, 0.12f, 0.08f, true),
    Cloud(0.62f, 0.18f, 0.19f, 0.04f, false),
    Cloud(0.86f, 0.10f, 0.13f, 0.07f, true),
    Cloud(0.18f, 0.48f, 0.15f, 0.06f, true),
    Cloud(0.55f, 0.58f, 0.21f, 0.03f, false),
    Cloud(0.84f, 0.52f, 0.14f, 0.05f, true),
    Cloud(0.06f, 0.84f, 0.23f, 0.04f, false),
    Cloud(0.45f, 0.92f, 0.18f, 0.06f, true),
    Cloud(0.78f, 0.86f, 0.25f, 0.03f, false)
)

@Composable
fun CloudSky(still: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "sky")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(90000, easing = LinearEasing)),
        label = "drift"
    )
    val t = if (still) 0.25f else drift
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(SkyTop, SkyMid, SkyPeach)))
        for (c in CLOUDS) {
            val x = ((c.bx + t * c.speed * 6f) % 1.3f - 0.15f) * w
            val y = (c.by + 0.015f * sin(t * 50f + c.bx * 80f)) * h
            val r = c.r * w
            val core = if (c.pink) CloudPink else CloudWhite
            val soft = core.copy(alpha = 0f)
            drawCircle(Brush.radialGradient(listOf(core, soft), center = Offset(x, y), radius = r), center = Offset(x, y), radius = r)
            drawCircle(Brush.radialGradient(listOf(core, soft), center = Offset(x - r * 0.55f, y + r * 0.18f), radius = r * 0.7f), center = Offset(x - r * 0.55f, y + r * 0.18f), radius = r * 0.7f)
            drawCircle(Brush.radialGradient(listOf(core, soft), center = Offset(x + r * 0.5f, y + r * 0.22f), radius = r * 0.6f), center = Offset(x + r * 0.5f, y + r * 0.22f), radius = r * 0.6f)
        }
        drawRect(
            Brush.verticalGradient(listOf(SkyPeach.copy(alpha = 0f), SkyPeach)),
            topLeft = Offset(0f, h * 0.72f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.28f)
        )
    }
}
