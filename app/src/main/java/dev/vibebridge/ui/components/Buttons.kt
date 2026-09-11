package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhBorderLight
import dev.vibebridge.ui.theme.GhBorderMuted
import dev.vibebridge.ui.theme.GhBtnGreen
import dev.vibebridge.ui.theme.GhBtnGreenPress
import dev.vibebridge.ui.theme.GhDanger
import dev.vibebridge.ui.theme.GhDangerPress
import dev.vibebridge.ui.theme.GhSurface
import dev.vibebridge.ui.theme.GhTextDisabled
import dev.vibebridge.ui.theme.GhTextSecondary
import dev.vibebridge.ui.theme.PureWhite

private val buttonShape = RoundedCornerShape(8.dp)

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
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .clip(buttonShape)
                .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
                .background(
                    if (!enabled) GhBorderMuted else if (pressed) GhBtnGreenPress else GhBtnGreen,
                    buttonShape
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) PureWhite else GhTextDisabled,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                color = GhTextSecondary,
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
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .clip(buttonShape)
                .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
                .background(GhSurface, buttonShape)
                .border(
                    1.dp,
                    if (!enabled) GhBorderMuted else if (pressed) GhAccent else GhBorderLight,
                    buttonShape
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) GhTextSecondary else GhTextDisabled,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
        if (!enabled && disabledReason != null) {
            Text(
                disabledReason,
                color = GhTextSecondary,
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
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(buttonShape)
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
            .background(
                if (!enabled) GhBorderMuted else if (pressed) GhDangerPress else GhDanger,
                buttonShape
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) PureWhite else GhTextDisabled,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
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
    tint: Color = GhTextSecondary
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
            .background(GhBorderMuted, CircleShape)
            .border(
                if (pressed) 2.dp else 1.dp,
                if (!enabled) GhBorderMuted else if (pressed) GhAccent else GhBorderLight,
                CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        VbIconView(icon = icon, color = if (enabled) tint else GhTextDisabled, size = 18.dp)
    }
}
