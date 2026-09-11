package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Amber
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.TextFaint

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
    Column(modifier = modifier.fillMaxWidth()) {
        Divider(color = Border, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Surface),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VbTab.entries.forEach { tab ->
                val sel = tab == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    VbIconView(icon = tab.icon, color = if (sel) Amber else TextFaint, size = 20.dp)
                    Text(
                        tab.label,
                        fontSize = 9.sp,
                        fontWeight = if (sel) FontWeight.Black else FontWeight.Medium,
                        color = if (sel) Amber else TextFaint,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}
