package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.BorderStrong
import dev.vibebridge.ui.theme.ButtonGreen
import dev.vibebridge.ui.theme.ButtonGreenPress
import dev.vibebridge.ui.theme.ButtonPrimary
import dev.vibebridge.ui.theme.ButtonPrimaryPress
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.DangerPress
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint

private val buttonShape = RoundedCornerShape(10.dp)

@Composable
fun VbButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledReason: String? = null
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = rememberPressScale(source)
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(46.dp)
                .pressScale(scale)
                .clip(buttonShape)
                .background(if (!enabled) Surface else if (pressed) ButtonPrimaryPress else ButtonPrimary)
                .border(1.dp, if (!enabled) Border else BorderStrong, buttonShape)
                .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) Text else TextFaint,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                color = TextFaint,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}

@Composable
fun VbButtonGreen(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledReason: String? = null
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = rememberPressScale(source)
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(46.dp)
                .pressScale(scale)
                .clip(buttonShape)
                .background(if (!enabled) Surface else if (pressed) ButtonGreenPress else ButtonGreen)
                .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                VbIconView(icon = VbIcon.PUSH, color = if (enabled) Text else TextFaint, size = 16.dp)
                Text(
                    text = text,
                    color = if (enabled) Text else TextFaint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                color = TextFaint,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
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
    val pressed by source.collectIsPressedAsState()
    val scale = rememberPressScale(source)
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(46.dp)
                .pressScale(scale)
                .clip(buttonShape)
                .background(Surface)
                .border(1.dp, if (pressed) BorderStrong else Border, buttonShape)
                .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) TextDim else TextFaint,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                color = TextFaint,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
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
    val pressed by source.collectIsPressedAsState()
    val scale = rememberPressScale(source)
    Box(
        modifier = modifier
            .height(46.dp)
            .pressScale(scale)
            .clip(buttonShape)
            .background(if (!enabled) Surface else if (pressed) DangerPress else Danger)
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Text else TextFaint,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VbIconButton(
    icon: VbIconKind,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = TextDim,
    accent: Boolean = false
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = rememberPressScale(source)
    Box(
        modifier = modifier
            .size(42.dp)
            .pressScale(scale)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .background(if (pressed) Surface else Color.Transparent)
            .border(
                width = if (pressed || accent) 1.5.dp else 1.dp,
                color = if (!enabled) Border else if (pressed || accent) Accent else Border,
                shape = CircleShape
            )
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        VbIconView(icon = icon, color = if (enabled) tint else TextFaint, size = 18.dp)
    }
}
