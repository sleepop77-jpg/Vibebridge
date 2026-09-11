package dev.vibebridge.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim

@Composable
fun VbConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = Surface,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                title,
                color = Text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = { Text(body, color = TextDim, fontSize = 13.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    color = if (danger) Danger else Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextDim, fontWeight = FontWeight.Medium)
            }
        }
    )
}
