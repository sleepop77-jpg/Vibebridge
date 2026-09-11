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

private val BUNNY_ROWS = listOf(
    ".##....##.",
    ".##....##.",
    ".###..###.",
    ".########.",
    "##R####R##",
    "##########",
    "####PP####",
    ".########.",
    ".########.",
    "##########",
    "##########",
    "..##..##.."
)

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
    Canvas(modifier.aspectRatio(10f / 12f)) {
        val cell = size.width / 10f
        val off = (bob - 0.5f) * cell * 0.8f
        BUNNY_ROWS.forEachIndexed { ry, row ->
            row.forEachIndexed { cx, ch ->
                val color = when {
                    ch == '#' -> Color.White
                    ch == 'R' -> if (blinking) Color.White else Color(0xFFFF4D4D)
                    ch == 'P' -> Color(0xFFFFB6C1)
                    else -> null
                }
                if (color != null) {
                    drawRect(color, Offset(cx * cell, ry * cell + off), Size(cell + 0.5f, cell + 0.5f))
                }
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
