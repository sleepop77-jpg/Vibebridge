package dev.vibebridge.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.VbMono
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

private data class StarSpec(
    val x: Float,
    val y0: Float,
    val speed: Int,
    val wobble: Int,
    val tw: Int,
    val phase: Float,
    val r: Float,
    val cross: Boolean
)

@Composable
fun StarField(modifier: Modifier = Modifier, count: Int = 42) {
    val stars = remember {
        val rnd = Random(7)
        List(count) {
            StarSpec(
                x = rnd.nextFloat(),
                y0 = rnd.nextFloat(),
                speed = 1 + rnd.nextInt(3),
                wobble = 1 + rnd.nextInt(2),
                tw = 2 + rnd.nextInt(4),
                phase = rnd.nextFloat(),
                r = 0.8f + rnd.nextFloat() * 1.6f,
                cross = rnd.nextInt(6) == 0
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "starfield")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "starT"
    )
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (s in stars) {
            val y = ((s.y0 + t * s.speed) % 1f) * h
            val wobbleArg = (t * s.wobble + s.phase).toDouble()
            val wobbleOffset = (sin(2.0 * PI * wobbleArg) * 5.0).toFloat()
            val x = s.x * w + wobbleOffset
            val twArg = (t * s.tw + s.phase).toDouble()
            val twinkle = 0.45f + 0.55f * abs(sin(PI * twArg)).toFloat()
            val c = Color.White.copy(alpha = twinkle)
            if (s.cross) {
                val l = s.r * 3f
                drawLine(c, Offset(x - l, y), Offset(x + l, y), 1.2f)
                drawLine(c, Offset(x, y - l), Offset(x, y + l), 1.2f)
            } else {
                drawCircle(c, s.r, Offset(x, y))
            }
        }
    }
}

private const val BUNNY_COLS = 104
private const val BUNNY_ROWS = 120

private fun ellipse(u: Float, v: Float, cx: Float, cy: Float, rx: Float, ry: Float): Boolean {
    val dx = (u - cx) / rx
    val dy = (v - cy) / ry
    return dx * dx + dy * dy <= 1f
}

private fun capsule(u: Float, v: Float, cx: Float, yTop: Float, yBot: Float, r: Float): Boolean {
    val ay = yTop + r
    val by = yBot - r
    val py = v.coerceIn(ay, by)
    val dx = u - cx
    val dy = v - py
    return dx * dx + dy * dy <= r * r
}

private fun bunnyColorAt(u: Float, v: Float, blinking: Boolean): Color? {
    if (!blinking) {
        if (ellipse(u, v, 0.375f, 0.415f, 0.055f, 0.045f)) return Color(0xFFFF4D4D)
        if (ellipse(u, v, 0.625f, 0.415f, 0.055f, 0.045f)) return Color(0xFFFF4D4D)
    }
    if (ellipse(u, v, 0.5f, 0.50f, 0.030f, 0.022f)) return Color(0xFFFFB6C1)
    if (ellipse(u, v, 0.30f, 0.16f, 0.030f, 0.09f)) return Color(0xFFFFD9E0)
    if (ellipse(u, v, 0.70f, 0.16f, 0.030f, 0.09f)) return Color(0xFFFFD9E0)
    if (capsule(u, v, 0.30f, 0.03f, 0.30f, 0.075f)) return Color.White
    if (capsule(u, v, 0.70f, 0.03f, 0.30f, 0.075f)) return Color.White
    if (ellipse(u, v, 0.5f, 0.42f, 0.30f, 0.17f)) return Color.White
    if (ellipse(u, v, 0.5f, 0.74f, 0.28f, 0.20f)) return Color.White
    if (ellipse(u, v, 0.36f, 0.945f, 0.10f, 0.045f)) return Color.White
    if (ellipse(u, v, 0.64f, 0.945f, 0.10f, 0.045f)) return Color.White
    return null
}

@Composable
fun PixelBunny(modifier: Modifier = Modifier) {
    val bobT = rememberInfiniteTransition(label = "bunnyBob")
    val bob by bobT.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val blinkT = rememberInfiniteTransition(label = "bunnyBlink")
    val blink by blinkT.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "blink"
    )
    val blinking = blink > 0.96f
    Canvas(modifier.aspectRatio(BUNNY_COLS.toFloat() / BUNNY_ROWS.toFloat())) {
        val cell = size.width / BUNNY_COLS
        val off = (bob - 0.5f) * cell * 0.4f
        for (ry in 0 until BUNNY_ROWS) {
            for (cx in 0 until BUNNY_COLS) {
                val u = (cx + 0.5f) / BUNNY_COLS
                val v = (ry + 0.5f) / BUNNY_ROWS
                val color = bunnyColorAt(u, v, blinking) ?: continue
                drawRect(color, Offset(cx * cell, ry * cell + off), Size(cell + 0.5f, cell + 0.5f))
            }
        }
    }
}

private val TIPS = listOf(
    "type an idea, get a bridge prompt, paste it into any free AI chat",
    "paste the AI reply back here — FILE / EDIT / DELETE blocks parse themselves",
    "PUSH TO GITHUB uploads blobs, commits, and polls CI without leaving the chat",
    "CI red? COPY ERRORS grabs only the e: lines — paste them back for a fast fix",
    "CI green? SAVE APK drops the built installer straight into Downloads",
    "the pills under the composer: model, PASTE, TEMPLATES, FILE attach, STRICT"
)

@Composable
fun TipLine(modifier: Modifier = Modifier) {
    var i by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            i = (i + 1) % TIPS.size
        }
    }
    Row(modifier = modifier) {
        Text("TIP // ", color = Accent, style = VbMono.Label)
        Text(
            TIPS[i],
            color = TextDim,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}
