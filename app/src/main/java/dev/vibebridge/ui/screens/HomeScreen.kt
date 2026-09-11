package dev.vibebridge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.components.AppMode
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbConfirmDialog
import dev.vibebridge.ui.components.VbErrorState
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbLoading
import dev.vibebridge.ui.components.VbMenuButton
import dev.vibebridge.ui.components.VbMenuItem
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.components.VbTab
import dev.vibebridge.ui.components.VbTag
import dev.vibebridge.ui.components.VbWordmark
import dev.vibebridge.ui.theme.GhAccent
import dev.vibebridge.ui.theme.GhAttention
import dev.vibebridge.ui.theme.GhCanvas
import dev.vibebridge.ui.theme.GhDangerBright
import dev.vibebridge.ui.theme.GhSuccess
import dev.vibebridge.ui.theme.GhTextDisabled
import dev.vibebridge.ui.theme.GhTextPrimary
import dev.vibebridge.ui.theme.GhTextSecondary
import dev.vibebridge.viewmodel.AppViewModel

private fun ciTag(ci: String?): Pair<String, Color> = when (ci) {
    "success" -> "PASSED" to GhSuccess
    "failure" -> "FAILED" to GhDangerBright
    "pending", "" -> "RUNNING" to GhAttention
    null -> "NONE" to GhTextDisabled
    else -> ci.uppercase() to GhTextDisabled
}

private fun humanBytes(b: Long): String = when {
    b < 1024 -> "$b B"
    b < 1024 * 1024 -> "${b / 1024} KB"
    else -> "${b / (1024 * 1024)} MB"
}

@Composable
fun HomeScreen(
    vm: AppViewModel,
    appMode: AppMode,
    goto: (VbTab) -> Unit,
    openSettings: () -> Unit
) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    var confirmDisconnect by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GhCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Stagger(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    VbWordmark(height = 26.dp)
                }
                VbMenuButton(
                    items = listOf(
                        VbMenuItem("REFRESH CI", VbIcon.REFRESH, action = vm::refresh),
                        VbMenuItem("SETTINGS", VbIcon.GEAR, action = openSettings),
                        VbMenuItem("DISCONNECT", VbIcon.TRASH, danger = true, action = { confirmDisconnect = true })
                    )
                )
            }
        }
        if (appMode == AppMode.WORK) {
            Stagger(1) {
                VbPanel(title = "WORKSPACE") {
                    VbStat("FILES", ui.files.toString())
                    VbStat("SIZE", humanBytes(ui.bytes))
                    VbStat("SAF MIRROR", if (ui.mirrorBound) "BOUND" else "OFF", if (ui.mirrorBound) GhSuccess else GhTextSecondary)
                }
            }
            Stagger(2) {
                VbPanel(title = "RECENT ACTIVITY") {
                    val last = ui.lastPush
                    if (last == null) {
                        Text("Nothing pushed yet. Compile a prompt or grab a payload to start.", style = MaterialTheme.typography.bodyMedium, color = GhTextSecondary)
                    } else {
                        Text(last.message, style = MaterialTheme.typography.bodyMedium, color = GhTextPrimary)
                        Text("${last.sha.take(7)} · ${last.ops} ops", style = MaterialTheme.typography.labelMedium, color = GhTextSecondary)
                    }
                }
            }
            Stagger(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VbButtonSecondary(text = "COMPILE PROMPT", onClick = { goto(VbTab.COMPILER) }, modifier = Modifier.weight(1f))
                    VbButtonSecondary(text = "GRAB CODE", onClick = { goto(VbTab.GRAB) }, modifier = Modifier.weight(1f))
                }
            }
        } else {
            Stagger(1) {
                VbPanel(title = "REPOSITORY") {
                    VbStat("REPO", vm.prefs.repo.ifBlank { "not set" })
                    VbStat("BRANCH", vm.prefs.branch)
                    VbStat("LOGIN", ui.login ?: "unverified", if (ui.login != null) GhSuccess else GhTextSecondary)
                }
            }
            Stagger(2) {
                VbPanel(
                    title = "LATEST PUSH",
                    actions = { ui.lastPush?.let { push -> VbTag(ciTag(push.ci).first, ciTag(push.ci).second) } }
                ) {
                    val last = ui.lastPush
                    if (last == null) {
                        Text("No pushes recorded on this device.", style = MaterialTheme.typography.bodyMedium, color = GhTextSecondary)
                    } else {
                        VbStat("MESSAGE", last.message)
                        VbStat("COMMIT", last.sha.take(7))
                    }
                }
            }
            Stagger(3) {
                VbPanel(title = "CI") {
                    when {
                        ui.refreshing -> VbLoading("fetching workflow runs")
                        ui.refreshError != null -> VbErrorState(ui.refreshError ?: "", retryLabel = "RETRY", onRetry = vm::refresh)
                        ui.lastRun == null -> Text("No workflow runs yet for this branch.", style = MaterialTheme.typography.bodyMedium, color = GhTextSecondary)
                        else -> {
                            val r = ui.lastRun!!
                            VbStat(
                                "STATE",
                                "${r.status} ${r.conclusion}",
                                if (r.conclusion == "success") GhSuccess else if (r.conclusion == "failure") GhDangerBright else GhTextSecondary
                            )
                            Text(
                                r.name,
                                color = GhAccent,
                                textDecoration = TextDecoration.Underline,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.url))) } }
                            )
                        }
                    }
                }
            }
            Stagger(4) {
                VbButtonSecondary(text = "REFRESH CI STATUS", onClick = vm::refresh, modifier = Modifier.fillMaxWidth(), enabled = !ui.refreshing)
            }
        }
    }

    if (confirmDisconnect) {
        VbConfirmDialog(
            title = "DISCONNECT TOKEN",
            body = "The PAT will be erased from this device. Push and CI features stop until a new token is saved.",
            confirmLabel = "ERASE",
            onConfirm = { vm.disconnect(); confirmDisconnect = false },
            onDismiss = { confirmDisconnect = false },
            danger = true
        )
    }
}
