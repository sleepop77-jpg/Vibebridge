package dev.vibebridge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.ui.theme.GhDangerBright
import dev.vibebridge.ui.theme.GhSurface
import dev.vibebridge.ui.theme.GhTextPrimary
import dev.vibebridge.ui.theme.GhTextSecondary

data class VbMenuItem(
    val label: String,
    val icon: VbIconKind,
    val danger: Boolean = false,
    val enabled: Boolean = true,
    val action: () -> Unit
)

private val MenuDots = VbIconKind(
    listOf(
        Dot(12f, 6f, 1.5f),
        Dot(12f, 12f, 1.5f),
        Dot(12f, 18f, 1.5f)
    )
)

@Composable
fun VbMenuButton(
    items: List<VbMenuItem>,
    modifier: Modifier = Modifier,
    description: String = "More actions"
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        VbIconButton(icon = MenuDots, description = description, onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = GhSurface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.width(240.dp)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VbIconView(
                                icon = item.icon,
                                color = if (item.danger) GhDangerBright else GhTextSecondary,
                                size = 18.dp
                            )
                            Text(
                                item.label,
                                color = if (item.danger) GhDangerBright else GhTextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        item.action()
                    },
                    enabled = item.enabled
                )
            }
        }
    }
}
