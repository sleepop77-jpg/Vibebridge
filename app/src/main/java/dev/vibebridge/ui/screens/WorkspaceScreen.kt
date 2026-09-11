package dev.vibebridge.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.FileViewerSheet
import dev.vibebridge.ui.components.BannerKind
import dev.vibebridge.ui.components.ShimmerBlock
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbBanner
import dev.vibebridge.ui.components.VbButtonDanger
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbConfirmDialog
import dev.vibebridge.ui.components.VbEmpty
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbIconButton
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbRowTile
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.ui.theme.Text
import dev.vibebridge.viewmodel.WorkspaceViewModel

@Composable
fun WorkspaceScreen(vm: WorkspaceViewModel, gotoChat: () -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.bindResult(ctx, uri)
    }

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
                Text("FILES", color = Text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                VbIconButton(icon = VbIcon.REFRESH, description = "Refresh tree", onClick = vm::refresh)
            }
        }
        Stagger(1) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ui.pulling) {
                    ShimmerBlock(height = 46.dp)
                } else {
                    VbButtonSecondary(text = "PULL FROM GITHUB", onClick = vm::askPull, modifier = Modifier.fillMaxWidth())
                }
                ui.pullMsg?.let { VbBanner(kind = BannerKind.INFO, text = it) }
            }
        }
        Stagger(2) {
            VbPanel(title = "SANDBOX STORAGE") {
                VbStat("FILES", ui.tree.size.toString())
                VbStat("SIZE", if (ui.bytes < 1024) "${ui.bytes} B" else "${ui.bytes / 1024} KB")
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VbButtonSecondary(text = "EXPORT ZIP", onClick = { vm.exportZip(ctx) }, modifier = Modifier.weight(1f))
                    VbButtonDanger(text = "CLEAR", onClick = vm::askClear, modifier = Modifier.weight(1f))
                }
                ui.zipMsg?.let { Spacer(Modifier.height(8.dp)); VbBanner(kind = BannerKind.INFO, text = it) }
            }
        }
        Stagger(3) {
            VbPanel(title = "SAF MIRROR") {
                if (ui.mirrorBound) {
                    VbStat("FOLDER", "bound")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VbButtonSecondary(text = "MIRROR NOW", onClick = { vm.mirrorNow(ctx) }, modifier = Modifier.weight(1f))
                        VbButtonSecondary(text = "UNBIND", onClick = { vm.unbind(ctx) }, modifier = Modifier.weight(1f))
                    }
                } else {
                    VbButtonSecondary(text = "BIND FOLDER", onClick = { picker.launch(null) }, modifier = Modifier.fillMaxWidth())
                }
                ui.mirrorMsg?.let { Spacer(Modifier.height(8.dp)); VbBanner(kind = BannerKind.INFO, text = it) }
            }
        }
        Stagger(4) {
            VbPanel(title = "FILE TREE") {
                if (ui.tree.isEmpty()) {
                    VbEmpty(icon = VbIcon.FOLDER, title = "Sandbox is empty. Pull from GitHub or apply a chat push.", actionLabel = "GO TO CHAT", onAction = gotoChat)
                } else {
                    ui.tree.forEach { path ->
                        VbRowTile(
                            icon = VbIcon.DOC,
                            title = path.substringAfterLast('/'),
                            subtitle = path,
                            onClick = { vm.open(path) }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    if (ui.confirmClear) {
        VbConfirmDialog(
            title = "CLEAR SANDBOX",
            body = "All local sandbox files will be deleted. The GitHub repository is not touched.",
            confirmLabel = "DELETE",
            onConfirm = vm::confirmClear,
            onDismiss = vm::cancelClear,
            danger = true
        )
    }
    if (ui.confirmPull) {
        VbConfirmDialog(
            title = "PULL FROM GITHUB",
            body = "The sandbox will be replaced with the current contents of the connected branch.",
            confirmLabel = "PULL",
            onConfirm = vm::pull,
            onDismiss = vm::cancelPull
        )
    }
    ui.selected?.let { path ->
        FileViewerSheet(path = path, content = ui.content, onDismiss = vm::closeViewer)
    }
}
