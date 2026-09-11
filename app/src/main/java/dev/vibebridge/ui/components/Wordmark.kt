package dev.vibebridge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.BorderStrong
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim

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
    height: Dp = 26.dp,
    showTagline: Boolean = false
) {
    val word = "VIBEBRIDGE"
    val progress by animateFloatAsState(targetValue = 1f, animationSpec = tween(900), label = "wm")
    val dotTransition = rememberInfiniteTransition(label = "wmDot")
    val dotPos by dotTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400)),
        label = "wmDotPos"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Canvas(Modifier.height(height).aspectRatio(59f / 8f)) {
            val s = size.height / 8f
            val cell = 6f * s
            val litCount = progress * word.length
            word.forEachIndexed { idx, ch ->
                val ox = idx * cell
                val glyph = VbGlyphs.MAP[ch] ?: emptyList()
                val lit = idx < litCount
                for (g in glyph) {
                    drawLine(
                        if (lit) Text else BorderStrong,
                        Offset(ox + g[0] * s, g[1] * s),
                        Offset(ox + g[2] * s, g[3] * s),
                        s * 0.8f,
                        StrokeCap.Round
                    )
                }
            }
            if (progress > 0.75f) {
                val uy = 7.2f * s
                drawRect(
                    brush = Brush.horizontalGradient(listOf(BorderStrong, Accent)),
                    topLeft = Offset(0f, uy),
                    size = Size(size.width, s * 0.4f)
                )
                val dx = dotPos * size.width
                drawCircle(Accent, s * 0.7f, Offset(dx, uy + s * 0.2f))
            }
        }
        if (showTagline && progress > 0.85f) {
            Text(
                "COWORK FOR MOBILE",
                color = TextDim,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
