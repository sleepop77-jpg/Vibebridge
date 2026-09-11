package dev.vibebridge.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.AmberHi
import dev.vibebridge.ui.theme.Card
import dev.vibebridge.ui.theme.RedHi
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
        containerColor = Card,
        shape = RoundedCornerShape(12.dp),
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (danger) RedHi else AmberHi,
                fontWeight = FontWeight.Black
            )
        },
        text = { Text(body, style = MaterialTheme.typography.bodyMedium, color = TextDim) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (danger) RedHi else AmberHi, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextDim, fontWeight = FontWeight.Bold)
            }
        }
    )
}