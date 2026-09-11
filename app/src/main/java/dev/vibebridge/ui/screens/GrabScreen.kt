package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.vibebridge.core.OpStatus
import dev.vibebridge.ui.components.BannerKind
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbBanner
import dev.vibebridge.ui.components.VbButton
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.components.VbTag
import dev.vibebridge.ui.theme.Amber
import dev.vibebridge.ui.theme.AmberLo
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Green
import dev.vibebridge.ui.theme.RedHi
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.viewmodel.GrabViewModel

private fun statusTag(s: OpStatus): Pair<String, Color> = when (s) {
    OpStatus.CREATE -> "CREATE" to Green
    OpStatus.OVERWRITE -> "OVERWRITE" to Amber
    OpStatus.EDIT -> "EDIT" to Green
    OpStatus.EDIT_PARTIAL -> "PARTIAL" to AmberLo
    OpStatus.DELETE -> "DELETE" to RedHi
    OpStatus.MISSING -> "MISSING" to RedHi
    OpStatus.NO_MATCH -> "NO MATCH" to RedHi
}

@Composable
fun GrabScreen(vm: GrabViewModel, gotoPush: () -> Unit) {
    val ui by vm.ui.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) { Text("GRAB", style = MaterialTheme.typography.displaySmall, color = Amber) }
        if (vm.strict) VbBanner(kind = BannerKind.WARN, text = "Strict mode is on. Payloads without the sentinel are rejected.")
        Stagger(1) { VbButtonSecondary(text = "READ CLIPBOARD", onClick = vm::readClipboard, modifier = Modifier.fillMaxWidth()) }
        Stagger(2) { VbField(label = "AI OUTPUT", value = ui.pasted, onValueChange = vm::setPasted, placeholder = "paste the bridge payload here", singleLine = false) }
        Stagger(3) { VbButton(text = "PARSE AND PLAN", onClick = vm::parse, enabled = ui.pasted.isNotBlank(), disabledReason = "Paste a payload first.") }
        ui.parsed?.let { parsed ->
            Stagger(4) {
                VbPanel(title = "PLAN") {
                    parsed.warnings.forEach { w -> VbBanner(kind = BannerKind.WARN, text = w); Spacer(Modifier.height(6.dp)) }
                    ui.plan.forEach { rep ->
                        val tag = statusTag(rep.status)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            VbTag(tag.first, tag.second)
                            Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp)) {
                                Text(rep.path, style = MaterialTheme.typography.labelLarge, color = Text)
                                Text(rep.detail, style = MaterialTheme.typography.labelMedium, color = TextDim)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        ui.applied?.let { rep ->
            Stagger(5) {
                VbPanel(title = "APPLIED TO SANDBOX") {
                    VbStat("CREATED", rep.created.toString())
                    VbStat("OVERWRITTEN", rep.overwritten.toString())
                    VbStat("EDITED", rep.edited.toString())
                    VbStat("DELETED", rep.deleted.toString())
                    rep.errors.forEach { e -> Spacer(Modifier.height(6.dp)); VbBanner(kind = BannerKind.ERROR, text = e) }
                }
            }
        }
        Stagger(6) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VbButton(text = "APPLY LOCALLY", onClick = vm::apply, modifier = Modifier.fillMaxWidth().padding(end = 5.dp), enabled = ui.parsed != null && ui.parsed!!.ops.isNotEmpty(), disabledReason = "Parse a payload first.")
                VbButtonSecondary(text = "CONTINUE TO PUSH", onClick = gotoPush, modifier = Modifier.fillMaxWidth().padding(start = 5.dp), enabled = ui.parsed != null && ui.parsed!!.ops.isNotEmpty(), disabledReason = "No operations staged.")
            }
        }
    }
}
