package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.vibebridge.core.PromptTemplates
import dev.vibebridge.core.VbClipboard
import dev.vibebridge.ui.components.BannerKind
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbBanner
import dev.vibebridge.ui.components.VbButton
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbEmpty
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbRowTile
import dev.vibebridge.ui.components.VbSegmented
import dev.vibebridge.ui.theme.AmberHi
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.GreenHi
import dev.vibebridge.viewmodel.AppViewModel

@Composable
fun CompilerScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    var idea by remember { mutableStateOf("") }
    var target by remember { mutableStateOf(vm.prefs.target) }
    val compiled = remember(idea, target) { if (idea.isBlank()) "" else PromptTemplates.compile(idea, target) }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) { Text("COMPILE", style = MaterialTheme.typography.displaySmall, color = AmberHi) }
        ui.infoBanner?.let { b -> VbBanner(kind = BannerKind.INFO, text = b, modifier = Modifier.fillMaxWidth()) }
        Stagger(1) {
            VbPanel(title = "IDEA") {
                VbField(label = "DESCRIBE THE CHANGE", value = idea, onValueChange = { idea = it }, placeholder = "add a settings screen with a strict parser toggle", singleLine = false)
            }
        }
        Stagger(2) {
            VbPanel(title = "TARGET MODEL") {
                VbSegmented(options = listOf("QWEN STUDIO", "CHATGPT", "GEMINI"), selected = target, onSelect = { target = it; vm.setTarget(it) })
            }
        }
        Stagger(3) {
            VbPanel(title = "COMPILED PROMPT") {
                if (compiled.isBlank()) {
                    VbEmpty(icon = VbIcon.CODE, title = "Describe the change to compile a prompt with the bridge contract injected.", actionLabel = "LOAD FIRST BUILTIN", onAction = { idea = PromptTemplates.BUILTINS.first().idea })
                } else {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                        Text(compiled, style = MaterialTheme.typography.labelMedium, color = GreenHi)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VbButton(text = "COPY PROMPT", onClick = { val ok = VbClipboard.copy(ctx, "vibe-prompt", compiled); vm.banner(if (ok) "prompt copied" else "clipboard unavailable") }, modifier = Modifier.fillMaxWidth().padding(end = 5.dp))
                        VbButtonSecondary(text = "SAVE TEMPLATE", onClick = { vm.history.addTemplate(idea.take(24), idea, target); vm.banner("template saved") }, modifier = Modifier.fillMaxWidth().padding(start = 5.dp))
                    }
                }
            }
        }
        Stagger(4) {
            VbPanel(title = "BUILTIN STARTERS") {
                PromptTemplates.BUILTINS.forEach { b ->
                    VbRowTile(icon = VbIcon.CODE, title = b.name, subtitle = b.idea.take(56), onClick = { idea = b.idea })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
