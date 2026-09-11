package dev.vibebridge.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.Warning

enum class BannerKind { INFO, WARN, ERROR }

@Composable
fun VbBanner(kind: BannerKind, text: String, modifier: Modifier = Modifier) {
    val (borderColor, tint, icon) = when (kind) {
        BannerKind.INFO -> Triple(Border, TextDim, VbIcon.CHECK)
        BannerKind.WARN -> Triple(Warning, Warning, VbIcon.WARN)
        BannerKind.ERROR -> Triple(Danger, Danger, VbIcon.CROSS)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VbIconView(icon = icon, color = tint, size = 16.dp)
        Text(text, color = Text, fontSize = 12.sp)
    }
}

@Composable
fun VbEmpty(
    icon: VbIconKind,
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VbIconView(icon = icon, color = TextFaint, size = 34.dp)
        Text(title, color = TextDim, fontSize = 13.sp, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            VbButtonSecondary(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun VbErrorState(
    message: String,
    modifier: Modifier = Modifier,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VbBanner(kind = BannerKind.ERROR, text = message)
        if (retryLabel != null && onRetry != null) {
            VbButtonSecondary(text = retryLabel, onClick = onRetry)
        }
    }
}

@Composable
fun VbLoading(label: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "angle"
    )
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(Modifier.size(28.dp)) {
            drawArc(
                color = Accent,
                startAngle = angle,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(3f, 3f),
                size = Size(size.width - 6f, size.height - 6f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }
        Text(label, color = TextDim, fontSize = 12.sp)
    }
}
