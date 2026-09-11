package dev.vibebridge.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import dev.vibebridge.core.Prefs
import dev.vibebridge.core.SafMirror
import dev.vibebridge.core.VbResult
import dev.vibebridge.core.Workspace
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkspaceViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    val workspace = Workspace(File(app.filesDir, "workspace"))

    data class Ui(
        val tree: List<String> = emptyList(),
        val bytes: Long = 0L,
        val selected: String? = null,
        val content: String? = null,
        val mirrorMsg: String? = null,
        val zipMsg: String? = null,
        val confirmClear: Boolean = false,
        val mirrorBound: Boolean = false
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    init { refresh() }

    fun refresh() = _ui.update {
        it.copy(
            tree = workspace.tree(),
            bytes = workspace.totalBytes(),
            mirrorBound = prefs.mirrorUri.isNotBlank()
        )
    }

    fun open(path: String) = _ui.update { it.copy(selected = path, content = workspace.read(path)) }
    fun closeViewer() = _ui.update { it.copy(selected = null, content = null) }

    fun exportZip(ctx: Context) {
        val out = File(ctx.filesDir, "out/workspace.zip")
        val n = workspace.exportZip(out)
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".vbfiles", out)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(share, "Share workspace zip"))
        _ui.update { it.copy(zipMsg = "packed $n files into workspace.zip") }
    }

    fun bindResult(ctx: Context, uri: Uri) {
        SafMirror.takePersistable(ctx, uri)
        prefs.mirrorUri = uri.toString()
        mirrorNow(ctx)
    }

    fun mirrorNow(ctx: Context) {
        if (prefs.mirrorUri.isBlank()) {
            _ui.update { it.copy(mirrorMsg = "bind a folder first") }
            return
        }
        val r = SafMirror.mirror(ctx, Uri.parse(prefs.mirrorUri), workspace)
        when (r) {
            is VbResult.Ok -> _ui.update { it.copy(mirrorMsg = "mirrored ${r.value} files", mirrorBound = true) }
            is VbResult.Err -> _ui.update { it.copy(mirrorMsg = r.message) }
        }
    }

    fun unbind(ctx: Context) {
        if (prefs.mirrorUri.isNotBlank()) SafMirror.release(ctx, Uri.parse(prefs.mirrorUri))
        prefs.mirrorUri = ""
        _ui.update { it.copy(mirrorBound = false, mirrorMsg = "folder unbound") }
    }

    fun askClear() = _ui.update { it.copy(confirmClear = true) }
    fun cancelClear() = _ui.update { it.copy(confirmClear = false) }
    fun confirmClear() {
        workspace.clear()
        _ui.update { it.copy(confirmClear = false) }
        refresh()
    }
}
