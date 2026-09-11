package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.components.VbTag
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhAttention
import dev.vibebridge.ui.theme.GhCanvas
import dev.vibebridge.ui.theme.GhDangerBright
import dev.vibebridge.ui.theme.GhSuccess
import dev.vibebridge.ui.theme.GhTextPrimary
import dev.vibebridge.ui.theme.GhTextSecondary
import dev.vibebridge.viewmodel.GrabViewModel

private fun statusTag(s: OpStatus): Pair<String, Color> = when (s) {
    OpStatus.CREATE -> "CREATE" to GhSuccess
    OpStatus.OVERWRITE -> "OVERWRITE" to GhAccent
    OpStatus.EDIT -> "EDIT" to GhSuccess
    OpStatus.EDIT_PARTIAL -> "PARTIAL" to GhAttention
    OpStatus.DELETE -> "DELETE" to GhDangerBright
    OpStatus.MISSING -> "MISSING" to GhDangerBright
    OpStatus.NO_MATCH -> "NO MATCH" to GhDangerBright
}

@Composable
fun GrabScreen(vm: GrabViewModel, gotoPush: () -> Unit, gotoHome: () -> Unit) {
    val ui by vm.ui.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().background(GhCanvas).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VbIconButton(icon = VbIcon.BACK, description = "Back to home", onClick = gotoHome)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("GRAB", style = MaterialTheme.typography.displaySmall, color = GhTextPrimary)
                }
                Spacer(Modifier.width(40.dp))
            }
        }
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rep.path, style = MaterialTheme.typography.labelLarge, color = GhTextPrimary)
                                Text(rep.detail, style = MaterialTheme.typography.labelMedium, color = GhTextSecondary)
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
                VbButton(text = "APPLY LOCALLY", onClick = vm::apply, modifier = Modifier.weight(1f), enabled = ui.parsed != null && ui.parsed!!.ops.isNotEmpty(), disabledReason = "Parse a payload first.")
                VbButton(text = "CONTINUE TO PUSH", onClick = gotoPush, modifier = Modifier.weight(1f), enabled = ui.parsed != null && ui.parsed!!.ops.isNotEmpty(), disabledReason = "No operations staged.")
            }
        }
    }
}
