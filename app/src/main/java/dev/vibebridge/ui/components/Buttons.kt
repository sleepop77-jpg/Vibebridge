package dev.vibebridge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.Amber
import dev.vibebridge.ui.theme.BorderStrong
import dev.vibebridge.ui.theme.CardAlt
import dev.vibebridge.ui.theme.PureBlack
import dev.vibebridge.ui.theme.PureWhite
import dev.vibebridge.ui.theme.Red
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint

private val buttonShape = RoundedCornerShape(12.dp)

@Composable
fun VbButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledReason: String? = null
) {
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source)
    Column(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .height(52.dp)
                .pressScale(scale),
            shape = buttonShape,
            interactionSource = source,
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber,
                contentColor = PureBlack,
                disabledContainerColor = CardAlt,
                disabledContentColor = TextFaint
            )
        ) {
            Text(text, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp())
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = TextFaint,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun VbButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledReason: String? = null
) {
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source)
    Column(modifier = modifier) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .height(52.dp)
                .pressScale(scale)
                .border(BorderStroke(1.dp, BorderStrong), buttonShape),
            shape = buttonShape,
            interactionSource = source,
            colors = ButtonDefaults.buttonColors(
                containerColor = Surface,
                contentColor = Text,
                disabledContainerColor = Surface,
                disabledContentColor = TextFaint
            )
        ) {
            Text(text, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp())
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = TextFaint,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun VbButtonDanger(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val source = remember { MutableInteractionSource() }
    val scale = rememberPressScale(source)
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .pressScale(scale),
        shape = buttonShape,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(
            containerColor = Red,
            contentColor = PureWhite,
            disabledContainerColor = CardAlt,
            disabledContentColor = TextFaint
        )
    ) {
        Text(text, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp())
    }
}

@Composable
fun VbIconButton(
    icon: VbIconKind,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = TextDim
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        VbIconView(icon = icon, color = if (enabled) tint else TextFaint, size = 20.dp)
    }
}

private fun sp(v: Int): androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.sp(v)
private fun sp(v: Float): androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.sp(v)
