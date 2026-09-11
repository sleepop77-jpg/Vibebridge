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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.Amber
import dev.vibebridge.ui.theme.AmberLo
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.Card
import dev.vibebridge.ui.theme.Red
import dev.vibebridge.ui.theme.RedHi
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint

enum class BannerKind { INFO, WARN, ERROR }

@Composable
fun VbBanner(kind: BannerKind, text: String, modifier: Modifier = Modifier) {
    val (border, tint, icon) = when (kind) {
        BannerKind.INFO -> Triple(Border, TextDim, VbIcon.CHECK)
        BannerKind.WARN -> Triple(AmberLo, AmberLo, VbIcon.WARN)
        BannerKind.ERROR -> Triple(Red, RedHi, VbIcon.CROSS)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Card, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VbIconView(icon = icon, color = tint, size = 18.dp)
        Text(text, style = MaterialTheme.typography.labelLarge, color = Text)
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
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VbIconView(icon = icon, color = TextFaint, size = 36.dp)
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextDim,
            textAlign = TextAlign.Center
        )
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
            .padding(vertical = 24.dp, horizontal = 16.dp),
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
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
                Canvas(Modifier.size(32.dp)) {
            drawArc(
                color = Amber,
                startAngle = angle,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(4f, 4f),
                size = Size(size.width - 8f, size.height - 8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextDim)
    }
}
