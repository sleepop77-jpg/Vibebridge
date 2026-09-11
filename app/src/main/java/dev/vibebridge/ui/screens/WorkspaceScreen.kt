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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.vibebridge.ui.FileViewerSheet
import dev.vibebridge.ui.components.BannerKind
import dev.vibebridge.ui.components.Stagger
import dev.vibebridge.ui.components.VbBanner
import dev.vibebridge.ui.components.VbButtonDanger
import dev.vibebridge.ui.components.VbButtonSecondary
import dev.vibebridge.ui.components.VbConfirmDialog
import dev.vibebridge.ui.components.VbEmpty
import dev.vibebridge.ui.components.VbIcon
import dev.vibebridge.ui.components.VbPanel
import dev.vibebridge.ui.components.VbRowTile
import dev.vibebridge.ui.components.VbStat
import dev.vibebridge.ui.theme.Amber
import dev.vibebridge.ui.theme.Bg
import dev.vibebridge.viewmodel.WorkspaceViewModel

@Composable
fun WorkspaceScreen(vm: WorkspaceViewModel, gotoGrab: () -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.ui.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.bindResult(ctx, uri)
    }

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Stagger(0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("FILES", style = MaterialTheme.typography.displaySmall, color = Amber)
                Spacer(Modifier.weight(1f))
                VbButtonSecondary(text = "REFRESH", onClick = vm::refresh)
            }
        }
        Stagger(1) {
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
        Stagger(2) {
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
        Stagger(3) {
            VbPanel(title = "FILE TREE", modifier = Modifier.weight(1f)) {
                if (ui.tree.isEmpty()) {
                    VbEmpty(icon = VbIcon.FOLDER, title = "Workspace is empty. Apply a grab to populate the sandbox.", actionLabel = "GO TO GRAB", onAction = gotoGrab)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        items(ui.tree) { path ->
                            VbRowTile(icon = VbIcon.CODE, title = path.substringAfterLast('/'), subtitle = path, onClick = { vm.open(path) })
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    if (ui.confirmClear) {
        VbConfirmDialog(title = "CLEAR WORKSPACE", body = "All sandbox files will be deleted. The GitHub repository is not touched.", confirmLabel = "DELETE", onConfirm = vm::confirmClear, onDismiss = vm::cancelClear, danger = true)
    }
    ui.selected?.let { path -> FileViewerSheet(path = path, content = ui.content, onDismiss = vm::closeViewer) }
}
