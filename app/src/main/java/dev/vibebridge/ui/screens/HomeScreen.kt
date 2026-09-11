package dev.vibebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbButton
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbErrorState
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbLoading
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.components.VbTab
import dev.vibebridge.ui.components.VbTag
import dev.vibebridge.ui.theme.Amber
import dev.vibebridge.ui.theme.AmberHi
import dev.vibebridge.ui.theme.AmberLo
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Green
import dev.vibebridge.ui.theme.RedHi
import dev.vibebridge.ui.theme.TextDim
import dev.vibebridge.viewmodel.AppViewModel

private fun ciTag(ci: String?): Pair<String, Color> = when (ci) {
    "success" -> "PASSED" to Green
    "failure" -> "FAILED" to RedHi
    "pending", "" -> "RUNNING" to Amber
    null -> "NONE" to AmberLo
    else -> ci.uppercase() to AmberLo
}

private fun humanBytes(b: Long): String = if (b < 1024) "$b B" else "${b / 1024} KB"

@Composable
fun HomeScreen(vm: AppViewModel, goto: (VbTab) -> Unit, openSettings: () -> Unit) {
    val ui by vm.ui.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("VIBEBRIDGE", style = MaterialTheme.typography.displaySmall, color = AmberHi)
                Spacer(Modifier.weight(1f))
                VbIconButton(icon = VbIcon.GEAR, description = "Open settings", onClick = openSettings)
            }
        }
        Stagger(1) {
            VbPanel(title = "WORKSPACE") {
                VbStat("FILES", ui.files.toString())
                VbStat("SIZE", humanBytes(ui.bytes))
            }
        }
        Stagger(2) {
            VbPanel(title = "REPOSITORY") {
                VbStat("REPO", vm.prefs.repo.ifBlank { "not set" })
                VbStat("BRANCH", vm.prefs.branch)
                VbStat("LOGIN", ui.login ?: "unverified", if (ui.login != null) AmberHi else TextDim)
            }
        }
        Stagger(3) {
            VbPanel(
                title = "LAST PUSH",
                actions = { ui.lastPush?.let { push -> VbTag(ciTag(push.ci).first, ciTag(push.ci).second) } }
            ) {
                VbStat("MESSAGE", ui.lastPush?.message ?: "none yet")
                VbStat("COMMIT", ui.lastPush?.sha?.take(7) ?: "-")
            }
        }
        Stagger(4) {
            VbPanel(title = "CI") {
                when {
                    ui.refreshing -> VbLoading("fetching workflow runs")
                    ui.refreshError != null -> VbErrorState(ui.refreshError ?: "", retryLabel = "RETRY", onRetry = vm::refresh)
                    else -> {
                        VbStat("RUN", ui.lastRun?.name ?: "no runs yet")
                        VbStat("STATE", ui.lastRun?.let { "${it.status} ${it.conclusion}" } ?: "-")
                    }
                }
            }
        }
        Stagger(5) {
            VbButtonSecondary(text = "REFRESH", onClick = vm::refresh, modifier = Modifier.fillMaxSize(), enabled = !ui.refreshing)
        }
        Stagger(6) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VbButton(text = "COMPILE PROMPT", onClick = { goto(VbTab.COMPILER) }, modifier = Modifier.weight(1f))
                VbButtonSecondary(text = "GRAB CODE", onClick = { goto(VbTab.GRAB) }, modifier = Modifier.weight(1f))
            }
        }
    }
}
