package dev.vibebridge.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.SurfaceHigh

@Composable
fun ShimmerBar(width: Dp = 160.dp, height: Dp = 12.dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shift"
    )
    Spacer(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceHigh)
            .offset(x = (shift / 6).dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Border, Color.Transparent)
                )
            )
    )
}

@Composable
fun ShimmerBlock(height: Dp = 72.dp) {
    val transition = rememberInfiniteTransition(label = "shimmerBlock")
    val shift by transition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "shiftBlock"
    )
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceHigh)
            .offset(x = (shift / 8).dp)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Border, Color.Transparent)))
    )
}
