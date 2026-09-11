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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.SurfaceHigh
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.PureBlack

@Composable
fun VbSegmented(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { opt ->
            val isSel = opt == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(if (isSel) SurfaceHigh else Color.Transparent, RoundedCornerShape(8.dp))
                    .border(1.dp, if (isSel) Border else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelect(opt) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    opt,
                    color = if (isSel) Text else TextDim,
                    fontSize = 12.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun VbTag(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = PureBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
