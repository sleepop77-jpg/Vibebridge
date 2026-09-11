package dev.vibebridge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import dev.vibebridge.ui.theme.Border
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.SurfaceHigh
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.VbMono

private val panelShape = RoundedCornerShape(14.dp)

@Composable
fun VbPanel(
    title: String? = null,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, panelShape)
            .border(1.dp, Border, panelShape)
            .padding(16.dp)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = VbMono.Label, color = TextDim)
                actions?.invoke(this)
            }
            Spacer(Modifier.height(12.dp))
        }
        content()
    }
}

@Composable
fun VbStat(label: String, value: String, valueColor: Color = Text) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = VbMono.Label, color = TextDim)
        Text(value, style = VbMono.Stat, color = valueColor)
    }
}

@Composable
fun VbRowTile(
    icon: VbIconKind,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextDim
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .background(SurfaceHigh, RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VbIconView(icon = icon, color = tint, size = 18.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = VbMono.Label, color = Text, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = VbMono.CodeSmall, color = TextDim)
        }
        VbIconView(icon = VbIcon.CHEVRON, color = TextDim, size = 14.dp)
    }
}

@Composable
fun VbChipTag(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = VbMono.Label, color = Text, fontSize = 10.sp)
    }
}
