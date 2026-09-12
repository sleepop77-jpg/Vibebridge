package dev.vibebridge.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.GitHubClient
import dev.vibebridge.core.RepoInfo
import dev.vibebridge.core.VbResult
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Surface
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.TextFaint
import dev.vibebridge.ui.theme.VbMono
import dev.vibebridge.ui.theme.WindowBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoPickerSheet(
    token: String,
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var repos by remember { mutableStateOf<List<RepoInfo>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        when (val r = GitHubClient().repos(token)) {
            is VbResult.Ok -> repos = r.value
            is VbResult.Err -> error = r.message
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        contentColor = Text,
        scrimColor = Bg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("CHOOSE REPOSITORY", style = VbMono.Label, color = TextDim)
            Spacer(Modifier.height(8.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("filter repos…", color = TextFaint, fontSize = 13.sp) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Text,
                    unfocusedTextColor = Text,
                    cursorColor = Text,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WindowBorder, RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.height(8.dp))
            when {
                error != null -> Text(error!!, color = Danger, fontSize = 12.sp)
                repos == null -> BounceDots()
                else -> {
                    val filtered = repos!!.filter { it.fullName.contains(query, ignoreCase = true) }
                    if (filtered.isEmpty()) {
                        Text("no repos match", color = TextFaint, fontSize = 12.sp)
                    } else {
                        LazyColumn {
                            items(filtered) { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onPick(r.fullName)
                                            onDismiss()
                                        }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            r.fullName,
                                            color = if (r.fullName == current) Accent else Text,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            if (r.isPrivate) "private" else "public",
                                            color = TextFaint,
                                            fontSize = 10.sp
                                        )
                                    }
                                    if (r.fullName == current) {
                                        Text("✓", color = Accent, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
