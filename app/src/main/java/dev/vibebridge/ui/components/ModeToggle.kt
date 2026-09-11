package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhBorderLight
import dev.vibebridge.ui.theme.GhCanvas
import dev.vibebridge.ui.theme.GhTextDisabled
import dev.vibebridge.ui.theme.GhTextPrimary
import dev.vibebridge.ui.theme.GhTextSecondary

enum class AppMode { WORK, GITHUB }

private val pillShape = RoundedCornerShape(20.dp)

@Composable
fun ModeToggle(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 8.dp)
            .height(40.dp)
            .clip(pillShape)
            .border(1.dp, GhBorderLight, pillShape)
            .background(GhTextDisabled),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ModeTab("WORK", currentMode == AppMode.WORK, { onModeChange(AppMode.WORK) }, Modifier.weight(1f))
        ModeTab("GITHUB", currentMode == AppMode.GITHUB, { onModeChange(AppMode.GITHUB) }, Modifier.weight(1f))
    }
}

@Composable
private fun ModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(pillShape)
            .background(if (selected) GhCanvas else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) GhTextPrimary else GhTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 1.sp
        )
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .width(56.dp)
                    .height(2.dp)
                    .background(GhAccent)
            )
        }
    }
}
