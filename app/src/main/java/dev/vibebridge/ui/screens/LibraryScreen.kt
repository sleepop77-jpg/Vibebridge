package dev.vibebridge.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.core.PromptTemplates
import dev.vibebridge.core.VbClipboard
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbEmpty
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbRowTile
import dev.vibebridge.ui.components.VbSegmented
import dev.vibebridge.ui.components.VbTab
import dev.vibebridge.ui.components.VbTag
import dev.vibebridge.ui.theme.Accent
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Danger
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.ui.theme.Warning
import dev.vibebridge.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun ciColor(ci: String): Color = when (ci) {
    "success" -> Accent
    "failure" -> Danger
    else -> Warning
}

@Composable
fun LibraryScreen(vm: AppViewModel, goto: (VbTab) -> Unit) {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf("TEMPLATES") }
    var refreshKey by remember { mutableStateOf(0) }
    val saved = remember(refreshKey) { vm.history.templates() }
    val pushes = remember(refreshKey) { vm.history.pushes() }
    val dateFmt = remember { SimpleDateFormat("MMM dd HH:mm", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Stagger(0) {
            Text("LIBRARY", color = Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Stagger(1) { VbSegmented(options = listOf("TEMPLATES", "HISTORY"), selected = tab, onSelect = { tab = it }) }
        if (tab == "TEMPLATES") {
            Stagger(2) {
                VbPanel(title = "BUILTIN STARTERS") {
                    PromptTemplates.BUILTINS.forEach { b ->
                        VbRowTile(
                            icon = VbIcon.BOOK,
                            title = b.name,
                            subtitle = b.idea.take(52),
                            onClick = {
                                VbClipboard.copy(ctx, "vibe-template", PromptTemplates.compile(b.idea, vm.prefs.target))
                                Toast.makeText(ctx, "template prompt copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Stagger(3) {
                VbPanel(title = "SAVED TEMPLATES") {
                    if (saved.isEmpty()) {
                        VbEmpty(icon = VbIcon.BOOK, title = "No saved templates yet. Save one from a chat prompt.", actionLabel = "GO TO CHAT", onAction = { goto(VbTab.CHAT) })
                    } else {
                        saved.forEach { t ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t.name, color = Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(t.idea.take(52), color = TextDim, fontSize = 11.sp)
                                }
                                VbIconButton(
                                    icon = VbIcon.COPY,
                                    description = "Copy template prompt",
                                    onClick = {
                                        VbClipboard.copy(ctx, "vibe-template", PromptTemplates.compile(t.idea, t.target))
                                        Toast.makeText(ctx, "template prompt copied", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                VbIconButton(
                                    icon = VbIcon.TRASH,
                                    description = "Delete template",
                                    onClick = { vm.history.deleteTemplate(t.id); refreshKey++ }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        } else {
            Stagger(2) {
                VbPanel(title = "PUSH HISTORY") {
                    if (pushes.isEmpty()) {
                        VbEmpty(icon = VbIcon.PUSH, title = "No pushes recorded yet. Start a change in the chat.", actionLabel = "GO TO CHAT", onAction = { goto(VbTab.CHAT) })
                    } else {
                        pushes.forEach { p ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${dateFmt.format(java.util.Date(p.ts))}  ${p.repo}", color = Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${p.ops} ops  ${p.sha.take(7)}  ${p.message.take(32)}", color = TextDim, fontSize = 11.sp)
                                }
                                VbTag(p.ci.uppercase(), ciColor(p.ci))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
