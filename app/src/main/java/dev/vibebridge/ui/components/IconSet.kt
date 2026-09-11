package dev.vibebridge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.cos
import kotlin.math.sin

sealed interface IconOp
data class Seg(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : IconOp
data class Dot(val x: Float, val y: Float, val r: Float) : IconOp
data class Ring(val x: Float, val y: Float, val r: Float) : IconOp
data class ArcOp(val l: Float, val t: Float, val w: Float, val h: Float, val start: Float, val sweep: Float) : IconOp

class VbIconKind(val ops: List<IconOp>)

object VbIcon {
    val HOME = VbIconKind(
        listOf(
            Seg(4f, 11f, 12f, 4f), Seg(12f, 4f, 20f, 11f),
            Seg(6f, 10f, 6f, 20f), Seg(6f, 20f, 18f, 20f), Seg(18f, 20f, 18f, 10f),
            Seg(10f, 20f, 10f, 14f), Seg(10f, 14f, 14f, 14f), Seg(14f, 14f, 14f, 20f)
        )
    )
    val CODE = VbIconKind(
        listOf(Seg(9f, 8f, 5f, 12f), Seg(5f, 12f, 9f, 16f), Seg(15f, 8f, 19f, 12f), Seg(19f, 12f, 15f, 16f))
    )
    val GRAB = VbIconKind(
        listOf(
            Seg(6f, 5f, 6f, 20f), Seg(6f, 20f, 18f, 20f), Seg(18f, 20f, 18f, 5f),
            Seg(9f, 3f, 15f, 3f), Seg(15f, 3f, 15f, 6f), Seg(15f, 6f, 9f, 6f), Seg(9f, 6f, 9f, 3f),
            Seg(9f, 10f, 15f, 10f), Seg(9f, 14f, 15f, 14f)
        )
    )
    val FOLDER = VbIconKind(
        listOf(
            Seg(4f, 7f, 10f, 7f), Seg(10f, 7f, 12f, 9f), Seg(12f, 9f, 20f, 9f),
            Seg(20f, 9f, 20f, 18f), Seg(20f, 18f, 4f, 18f), Seg(4f, 18f, 4f, 7f)
        )
    )
    val PUSH = VbIconKind(
        listOf(
            Seg(12f, 16f, 12f, 5f), Seg(8f, 9f, 12f, 5f), Seg(12f, 5f, 16f, 9f),
            Seg(5f, 16f, 5f, 19f), Seg(5f, 19f, 19f, 19f), Seg(19f, 19f, 19f, 16f)
        )
    )
    val BOOK = VbIconKind(
        listOf(
            Seg(6f, 4f, 18f, 4f), Seg(18f, 4f, 18f, 20f), Seg(18f, 20f, 6f, 20f), Seg(6f, 20f, 6f, 4f),
            Seg(9f, 4f, 9f, 20f), Seg(12f, 9f, 15f, 9f), Seg(12f, 13f, 15f, 13f)
        )
    )
    val GEAR = VbIconKind(
        buildList {
            add(Ring(12f, 12f, 3.4f))
            for (i in 0 until 8) {
                val a = i * Math.PI / 4.0
                add(
                    Seg(
                        (12 + cos(a) * 5.6).toFloat(), (12 + sin(a) * 5.6).toFloat(),
                        (12 + cos(a) * 8.2).toFloat(), (12 + sin(a) * 8.2).toFloat()
                    )
                )
            }
        }
    )
    val LINK = VbIconKind(
        listOf(
            Seg(8f, 12f, 6f, 14f), Seg(6f, 14f, 8f, 16f), Seg(8f, 16f, 10f, 14f), Seg(10f, 14f, 8f, 12f),
            Seg(16f, 12f, 18f, 10f), Seg(18f, 10f, 16f, 8f), Seg(16f, 8f, 14f, 10f), Seg(14f, 10f, 16f, 12f),
            Seg(10f, 14f, 14f, 10f)
        )
    )
    val COPY = VbIconKind(
        listOf(
            Seg(9f, 9f, 19f, 9f), Seg(19f, 9f, 19f, 19f), Seg(19f, 19f, 9f, 19f), Seg(9f, 19f, 9f, 9f),
            Seg(6f, 15f, 6f, 5f), Seg(6f, 5f, 16f, 5f)
        )
    )
    val CHECK = VbIconKind(listOf(Seg(5f, 13f, 10f, 18f), Seg(10f, 18f, 19f, 7f)))
    val CROSS = VbIconKind(listOf(Seg(6f, 6f, 18f, 18f), Seg(18f, 6f, 6f, 18f)))
    val WARN = VbIconKind(
        listOf(
            Seg(12f, 4f, 21f, 19f), Seg(21f, 19f, 3f, 19f), Seg(3f, 19f, 12f, 4f),
            Seg(12f, 10f, 12f, 14f), Dot(12f, 16.5f, 0.9f)
        )
    )
    val QR = VbIconKind(
        listOf(
            Seg(4f, 4f, 9f, 4f), Seg(9f, 4f, 9f, 9f), Seg(9f, 9f, 4f, 9f), Seg(4f, 9f, 4f, 4f),
            Seg(15f, 4f, 20f, 4f), Seg(20f, 4f, 20f, 9f), Seg(20f, 9f, 15f, 9f), Seg(15f, 9f, 15f, 4f),
            Seg(4f, 15f, 9f, 15f), Seg(9f, 15f, 9f, 20f), Seg(9f, 20f, 4f, 20f), Seg(4f, 20f, 4f, 15f),
            Dot(16.5f, 16.5f, 1.1f), Dot(19.5f, 16.5f, 1.1f), Dot(16.5f, 19.5f, 1.1f), Dot(19.5f, 19.5f, 1.1f)
        )
    )
    val ZIP = VbIconKind(
        listOf(
            Seg(6f, 4f, 18f, 4f), Seg(18f, 4f, 18f, 20f), Seg(18f, 20f, 6f, 20f), Seg(6f, 20f, 6f, 4f),
            Seg(12f, 4f, 12f, 9f), Seg(10f, 6f, 14f, 6f), Seg(10f, 8f, 14f, 8f),
            Seg(12f, 11f, 12f, 16f), Seg(9f, 13f, 12f, 16f), Seg(12f, 16f, 15f, 13f)
        )
    )
    val REFRESH = VbIconKind(
        listOf(
            ArcOp(5f, 5f, 14f, 14f, -60f, 300f),
            Seg(16f, 6f, 19f, 5f), Seg(16f, 6f, 17f, 10f)
        )
    )
    val TRASH = VbIconKind(
        listOf(
            Seg(5f, 7f, 19f, 7f), Seg(10f, 7f, 10f, 5f), Seg(10f, 5f, 14f, 5f), Seg(14f, 5f, 14f, 7f),
            Seg(7f, 7f, 8f, 20f), Seg(8f, 20f, 16f, 20f), Seg(16f, 20f, 17f, 7f),
            Seg(10f, 10f, 10f, 17f), Seg(14f, 10f, 14f, 17f)
        )
    )
    val BACK = VbIconKind(listOf(Seg(11f, 5f, 4f, 12f), Seg(4f, 12f, 11f, 19f), Seg(4f, 12f, 20f, 12f)))
    val PLUS = VbIconKind(listOf(Seg(12f, 5f, 12f, 19f), Seg(5f, 12f, 19f, 12f)))
}

@Composable
fun VbIconView(
    icon: VbIconKind,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 20.dp
) {
    val strokePx = with(LocalDensity.current) { (size * 0.09f).toPx() }
    Canvas(modifier.size(size)) {
        val s = this.size.width / 24f
        for (op in icon.ops) {
            when (op) {
                is Seg -> drawLine(
                    color,
                    Offset(op.x1 * s, op.y1 * s),
                    Offset(op.x2 * s, op.y2 * s),
                    strokePx,
                    StrokeCap.Round
                )
                is Dot -> drawCircle(color, op.r * s, Offset(op.x * s, op.y * s))
                is Ring -> drawCircle(
                    color, op.r * s, Offset(op.x * s, op.y * s),
                    style = androidx.compose.ui.graphics.Stroke(width = strokePx)
                )
                is ArcOp -> drawArc(
                    color, op.start, op.sweep, false,
                    topLeft = Offset(op.l * s, op.t * s),
                    size = Size(op.w * s, op.h * s),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
