package dev.vibebridge.ui.screens

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
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
import dev.vibebridge.ui.theme.AmberHi
import dev.vibebridge.ui.theme.AmberLo
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Green
import dev.vibebridge.ui.theme.RedHi
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private fun ciColor(ci: String): Color = when (ci) { "success" -> Green; "failure" -> RedHi; else -> AmberLo }

@Composable
fun LibraryScreen(vm: AppViewModel, goto: (VbTab) -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    var tab by remember { mutableStateOf("TEMPLATES") }
    var refreshKey by remember { mutableStateOf(0) }
    val saved = remember(refreshKey) { vm.history.templates() }
    val pushes = remember(refreshKey) { vm.history.pushes() }
    val dateFmt = remember { SimpleDateFormat("MMM dd HH:mm", Locale.US) }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) { Text("LIBRARY", style = MaterialTheme.typography.displaySmall, color = AmberHi) }
        Stagger(1) { VbSegmented(options = listOf("TEMPLATES", "HISTORY"), selected = tab, onSelect = { tab = it }) }
        if (tab == "TEMPLATES") {
            Stagger(2) {
                VbPanel(title = "BUILTIN STARTERS") {
                    PromptTemplates.BUILTINS.forEach { b ->
                        VbRowTile(icon = VbIcon.BOOK, title = b.name, subtitle = b.idea.take(52), onClick = { VbClipboard.copy(ctx, "vibe-template", PromptTemplates.compile(b.idea, vm.prefs.target)); vm.banner("template prompt copied") })
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            Stagger(3) {
                VbPanel(title = "SAVED TEMPLATES") {
                    if (saved.isEmpty()) {
                        VbEmpty(icon = VbIcon.BOOK, title = "No saved templates. Save one from the COMPILE tab.", actionLabel = "GO TO COMPILE", onAction = { goto(VbTab.COMPILER) })
                    } else {
                        saved.forEach { t ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t.name, style = MaterialTheme.typography.labelLarge, color = Text)
                                    Text(t.idea.take(52), style = MaterialTheme.typography.labelMedium, color = TextDim)
                                }
                                VbIconButton(icon = VbIcon.COPY, description = "Copy template prompt", onClick = { VbClipboard.copy(ctx, "vibe-template", PromptTemplates.compile(t.idea, t.target)); vm.banner("template prompt copied") })
                                VbIconButton(icon = VbIcon.TRASH, description = "Delete template", onClick = { vm.history.deleteTemplate(t.id); refreshKey++ })
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
                        VbEmpty(icon = VbIcon.PUSH, title = "No pushes recorded yet. Stage and push a payload from GRAB.", actionLabel = "GO TO GRAB", onAction = { goto(VbTab.GRAB) })
                    } else {
                        pushes.forEach { p ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${dateFmt.format(java.util.Date(p.ts))}  ${p.repo}", style = MaterialTheme.typography.labelLarge, color = Text)
                                    Text("${p.ops} ops  ${p.sha.take(7)}  ${p.message.take(32)}", style = MaterialTheme.typography.labelMedium, color = TextDim)
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
