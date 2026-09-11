package dev.vibebridge.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhBorderLight
import dev.vibebridge.ui.theme.GhSuccess
import dev.vibebridge.ui.theme.GhTextPrimary
import dev.vibebridge.ui.theme.GhTextSecondary

private typealias GSeg = FloatArray

object VbGlyphs {
    private fun s(x1: Float, y1: Float, x2: Float, y2: Float): GSeg = floatArrayOf(x1, y1, x2, y2)
    val MAP: Map<Char, List<GSeg>> = mapOf(
        'V' to listOf(s(0f, 0f, 2f, 6f), s(2f, 6f, 4f, 0f)),
        'I' to listOf(s(0f, 0f, 4f, 0f), s(2f, 0f, 2f, 6f), s(0f, 6f, 4f, 6f)),
        'B' to listOf(
            s(0f, 0f, 0f, 6f), s(0f, 0f, 3f, 0f), s(3f, 0f, 4f, 1.5f), s(4f, 1.5f, 3f, 3f),
            s(3f, 3f, 0f, 3f), s(3f, 3f, 4f, 4.5f), s(4f, 4.5f, 3f, 6f), s(3f, 6f, 0f, 6f)
        ),
        'E' to listOf(s(0f, 0f, 0f, 6f), s(0f, 0f, 4f, 0f), s(0f, 3f, 3f, 3f), s(0f, 6f, 4f, 6f)),
        'R' to listOf(
            s(0f, 0f, 0f, 6f), s(0f, 0f, 3f, 0f), s(3f, 0f, 4f, 1.5f), s(4f, 1.5f, 3f, 3f),
            s(3f, 3f, 0f, 3f), s(3f, 3f, 4f, 6f)
        ),
        'D' to listOf(
            s(0f, 0f, 0f, 6f), s(0f, 0f, 2.5f, 0f), s(2.5f, 0f, 4f, 1.5f),
            s(4f, 1.5f, 4f, 4.5f), s(4f, 4.5f, 2.5f, 6f), s(2.5f, 6f, 0f, 6f)
        ),
        'G' to listOf(
            s(4f, 1f, 3f, 0f), s(3f, 0f, 1f, 0f), s(1f, 0f, 0f, 1f), s(0f, 1f, 0f, 5f),
            s(0f, 5f, 1f, 6f), s(1f, 6f, 3f, 6f), s(3f, 6f, 4f, 5f), s(4f, 5f, 4f, 3.5f), s(4f, 3.5f, 2.5f, 3.5f)
        )
    )
}

@Composable
fun VbWordmark(
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    showTagline: Boolean = false
) {
    val word = "VIBEBRIDGE"
    val progress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1600), label = "wordmark")
    val dotTransition = rememberInfiniteTransition(label = "underline")
    val dotPos by dotTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200)),
        label = "dot"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.height(height).aspectRatio(59f / 8f)) {
            val s = size.height / 8f
            val cell = 6f * s
            val cascade = 0.45f
            val litCount = if (progress < cascade) (progress / cascade) * word.length else word.length.toFloat()

            word.forEachIndexed { idx, ch ->
                val ox = idx * cell
                val glyph: List<GSeg> = VbGlyphs.MAP[ch] ?: emptyList()
                val lit = idx < litCount
                if (lit) {
                    for (g in glyph) {
                        drawLine(
                            Color(0xFF1F6FEB),
                            Offset(ox + g[0] * s, g[1] * s),
                            Offset(ox + g[2] * s, g[3] * s),
                            s * 1.5f,
                            StrokeCap.Round
                        )
                    }
                }
                for (g in glyph) {
                    drawLine(
                        if (lit) GhTextPrimary else GhBorderLight,
                        Offset(ox + g[0] * s, g[1] * s),
                        Offset(ox + g[2] * s, g[3] * s),
                        s * 0.8f,
                        StrokeCap.Round
                    )
                }
            }

            if (progress in cascade..0.8f) {
                val t = (progress - cascade) / (0.8f - cascade)
                val bandW = size.width * 0.22f
                val bandX = -bandW + t * (size.width + bandW * 2f)
                rotate(-18f, pivot = Offset(bandX + bandW / 2f, size.height / 2f)) {
                    drawRect(
                        color = GhAccent,
                        topLeft = Offset(bandX, -size.height),
                        size = Size(bandW, size.height * 3f)
                    )
                }
                for (k in 0 until 6) {
                    val px = bandX + bandW + ((k * 37) % 40) * s * 0.2f
                    val py = ((k * 53) % 60) * s * 0.1f + s
                    drawLine(GhTextPrimary, Offset(px - s * 0.4f, py), Offset(px + s * 0.4f, py), s * 0.25f, StrokeCap.Round)
                    drawLine(GhTextPrimary, Offset(px, py - s * 0.4f), Offset(px, py + s * 0.4f), s * 0.25f, StrokeCap.Round)
                }
            }

            if (progress > 0.8f) {
                val uy = 7.2f * s
                drawRect(
                    brush = Brush.horizontalGradient(listOf(GhAccent, GhSuccess)),
                    topLeft = Offset(0f, uy),
                    size = Size(size.width, s * 0.45f)
                )
                val dx = dotPos * size.width
                drawCircle(Color(0xFF1F6FEB), s * 1.1f, Offset(dx, uy + s * 0.22f))
                drawCircle(GhTextPrimary, s * 0.55f, Offset(dx, uy + s * 0.22f))
            }
        }
        if (showTagline && progress > 0.85f) {
            Text(
                "COWORK FOR MOBILE",
                color = GhTextSecondary,
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
