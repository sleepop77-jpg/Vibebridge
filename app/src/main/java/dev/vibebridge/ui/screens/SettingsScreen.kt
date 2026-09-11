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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vibebridge.BuildConfig
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbButtonDanger
import dev.vibebridge.ui.components.VbConfirmDialog
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbSegmented
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.components.VbToggle
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.viewmodel.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsState()
    var confirmDisconnect by remember { mutableStateOf(false) }
    var autoClip by remember { mutableStateOf(vm.prefs.autoClip) }
    var target by remember { mutableStateOf(vm.prefs.target) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Stagger(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VbIconButton(icon = VbIcon.BACK, description = "Back", onClick = onBack)
                Text("SETTINGS", color = Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Stagger(1) {
            VbPanel(title = "ACCOUNT") {
                VbStat("LOGIN", ui.login ?: "unverified")
                VbStat("REPO", vm.prefs.repo.ifBlank { "not set" })
                VbStat("BRANCH", vm.prefs.branch)
                VbStat("TOKEN STORAGE", if (vm.secure.secure) "encrypted keystore" else "fallback store")
                Spacer(Modifier.height(10.dp))
                VbButtonDanger(text = "DISCONNECT TOKEN", onClick = { confirmDisconnect = true }, modifier = Modifier.fillMaxWidth())
            }
        }
        Stagger(2) {
            VbPanel(title = "TARGET MODEL") {
                VbSegmented(
                    options = listOf("QWEN STUDIO", "CHATGPT", "GEMINI"),
                    selected = target,
                    onSelect = { target = it; vm.setTarget(it) }
                )
            }
        }
        Stagger(3) {
            VbPanel(title = "PARSER AND CLIPBOARD") {
                VbToggle(label = "STRICT MODE: reject payloads without sentinel", checked = ui.strict, onChange = vm::setStrict)
                VbToggle(
                    label = "AUTO-DETECT bridge payloads on clipboard",
                    checked = autoClip,
                    onChange = { autoClip = it; vm.prefs.autoClip = it }
                )
            }
        }
        Stagger(4) {
            VbPanel(title = "STORAGE") {
                VbStat("WORKSPACE FILES", ui.files.toString())
                VbStat("WORKSPACE SIZE", if (ui.bytes < 1024) "${ui.bytes} B" else "${ui.bytes / 1024} KB")
                VbStat("SAF MIRROR", if (ui.mirrorBound) "bound" else "not bound")
            }
        }
        Stagger(5) {
            VbPanel(title = "ABOUT") {
                VbStat("VERSION", BuildConfig.VERSION_NAME)
                VbStat("BUILD TYPE", BuildConfig.BUILD_TYPE)
                Text(
                    "VibeBridge moves code between free AI chats and your repository. No inference runs inside this app.",
                    color = TextDim,
                    fontSize = 12.sp
                )
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
