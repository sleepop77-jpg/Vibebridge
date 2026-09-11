package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhBorder
import dev.vibebridge.ui.theme.GhInset
import dev.vibebridge.ui.theme.GhTextDisabled

enum class VbTab(val label: String, val icon: VbIconKind) {
    HOME("HOME", VbIcon.HOME),
    COMPILER("COMPILE", VbIcon.CODE),
    GRAB("GRAB", VbIcon.GRAB),
    WORKSPACE("FILES", VbIcon.FOLDER),
    LIBRARY("LIBRARY", VbIcon.BOOK)
}

@Composable
fun VbBottomBar(
    current: VbTab,
    onSelect: (VbTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Divider(color = GhBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(GhInset),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VbTab.entries.forEach { tab ->
                val sel = tab == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (sel) {
                        Box(Modifier.size(4.dp).clip(CircleShape).background(GhAccent))
                    } else {
                        Box(Modifier.size(4.dp))
                    }
                    VbIconView(icon = tab.icon, color = if (sel) GhAccent else GhTextDisabled, size = 18.dp)
                    Text(
                        tab.label,
                        fontSize = 9.sp,
                        fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (sel) GhAccent else GhTextDisabled,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
