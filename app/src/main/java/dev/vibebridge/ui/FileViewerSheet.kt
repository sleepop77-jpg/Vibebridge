package dev.vibebridge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.theme.BorderStrong
import dev.vibebridge.ui.theme.Card
import dev.vibebridge.ui.theme.PureBlack
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.AmberHi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerSheet(path: String, content: String?, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Card,
        contentColor = Text,
        scrimColor = PureBlack,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderStrong) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .padding(16.dp)
        ) {
            Text(path, style = MaterialTheme.typography.titleMedium, color = AmberHi)
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(content ?: "binary or unreadable file", style = MaterialTheme.typography.labelMedium, color = TextDim)
            }
        }
    }
}