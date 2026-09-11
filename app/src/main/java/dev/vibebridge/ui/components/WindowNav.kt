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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.WindowBorder

enum class VbTab(val label: String, val icon: VbIconKind) {
    CHAT("CHAT", VbIcon.BOT),
    FILES("FILES", VbIcon.FOLDER),
    LIBRARY("LIBRARY", VbIcon.BOOK)
}

@Composable
fun WindowNav(
    current: VbTab,
    onSelect: (VbTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Divider(color = WindowBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VbTab.entries.forEach { tab ->
                val sel = tab == current
                Column(
                    modifier = Modifier
                        .semantics { contentDescription = tab.label }
                        .clip(CircleShape)
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    VbIconView(icon = tab.icon, color = if (sel) Text else TextFaint, size = 19.dp)
                    Box(
                        Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(if (sel) Accent else Color.Transparent)
                    )
                }
            }
        }
    }
}
