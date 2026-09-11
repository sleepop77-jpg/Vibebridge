package dev.vibebridge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.vibebridge.core.Qr
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbButton
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbErrorState
import dev.vibebridge.ui.components.VbField
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbLoading
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.components.VbTag
import dev.vibebridge.ui.theme.AmberHi
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Green
import dev.vibebridge.ui.theme.RedHi
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.viewmodel.OpsHandoff
import dev.vibebridge.viewmodel.PushViewModel

@Composable
fun PushScreen(vm: PushViewModel, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val qr = remember(ui.runUrl) { ui.runUrl?.let { Qr.bitmap(it) } }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VbIconButton(icon = VbIcon.BACK, description = "Back to tabs", onClick = onBack)
                Text("PUSH", style = MaterialTheme.typography.displaySmall, color = AmberHi)
            }
        }
        Stagger(1) {
            VbPanel(title = "PAYLOAD") {
                VbStat("OPERATIONS", OpsHandoff.ops.size.toString())
                VbStat("SOURCE", OpsHandoff.label.ifBlank { "none" })
            }
        }
        Stagger(2) {
            VbPanel(title = "COMMIT") {
                VbField(label = "MESSAGE", value = ui.message, onValueChange = vm::setMessage, placeholder = "feat: settings screen (via VibeBridge)")
                Spacer(Modifier.height(12.dp))
                VbButton(text = "PUSH NOW", onClick = vm::push, enabled = vm.canPush, disabledReason = vm.pushDisabledReason().ifBlank { null })
            }
        }
        when (ui.phase) {
            PushViewModel.Phase.COMMITTING, PushViewModel.Phase.POLLING -> Stagger(3) { VbLoading(ui.status.ifBlank { "working" }) }
            PushViewModel.Phase.DONE -> Stagger(3) {
                VbPanel(title = "RESULT", actions = { VbTag(if (ui.conclusion == "success") "PASSED" else (ui.conclusion ?: "DONE").uppercase(), if (ui.conclusion == "success") Green else RedHi) }) {
                    VbStat("COMMIT", ui.commitSha?.take(7) ?: "-")
                    ui.artifactName?.let { VbStat("ARTIFACT", "$it (${ui.artifactBytes / 1024} KB)") }
                    Spacer(Modifier.height(12.dp))
                    if (qr != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Image(bitmap = qr.asImageBitmap(), contentDescription = "QR code linking to the workflow run", modifier = Modifier.size(180.dp), contentScale = ContentScale.Fit)
                            Text("scan opens the workflow run", style = MaterialTheme.typography.labelMedium, color = TextDim)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    VbButtonSecondary(text = "OPEN RUN IN BROWSER", onClick = { ui.runUrl?.let { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } } }, modifier = Modifier.fillMaxWidth(), enabled = ui.runUrl != null)
                }
            }
            PushViewModel.Phase.FAILED -> Stagger(3) { VbErrorState(ui.error ?: "push failed", retryLabel = "RETRY PUSH", onRetry = vm::push) }
            PushViewModel.Phase.IDLE -> Unit
        }
    }
}
